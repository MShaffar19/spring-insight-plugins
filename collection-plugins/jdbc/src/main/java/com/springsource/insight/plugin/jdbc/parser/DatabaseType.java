/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc.parser;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.springsource.insight.plugin.jdbc.parser.parsers.HsqlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.MssqlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.MySqlParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.OracleRACParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.SqlFireParser;
import com.springsource.insight.plugin.jdbc.parser.parsers.SqlFirePeerParser;
import com.springsource.insight.util.StringUtil;

public enum DatabaseType {
    MYSQL("mysql", new MySqlParser()), 
    ORACLE("oracle", new OracleParser(), new OracleRACParser()), 
    HSQLDB("hsqldb", new HsqlParser()), 
    MSSQL("microsoft", new MssqlParser()),
    SQLFIRE("sqlfire", new SqlFireParser(), new SqlFirePeerParser());

    private static final Map<String, DatabaseType> map=new TreeMap<String, DatabaseType>(String.CASE_INSENSITIVE_ORDER);

    static {
        for (DatabaseType type : DatabaseType.values()) {
            map.put(type.vendorName, type);
        }
    }

    private final String vendorName;
    private final JdbcUrlParser[] parsers;

    @SuppressWarnings("hiding")
    private DatabaseType(final String vendorName, final JdbcUrlParser... parsers) {
        this.vendorName = vendorName;
        this.parsers = parsers;
    }

    public String getVendorName() {
        return vendorName;
    }

    public static DatabaseType findByDatabaseName(final String databaseName) {
        return StringUtil.isEmpty(databaseName) ? null : map.get(databaseName);
    }

    public static List<JdbcUrlMetaData> parse(final String connectionUrl) {
        if (StringUtil.isEmpty(connectionUrl)) {
        	return null;
        }

        String[] 	 parts=connectionUrl.split("[:]");
        DatabaseType type=(parts.length >= 2) ? findByDatabaseName(parts[1]) : null;
        if (type == null) {	// cannot determine type
        	return null;
        }

        for (JdbcUrlParser parser : type.parsers) {
            List<JdbcUrlMetaData> res=parser.parse(connectionUrl, type.vendorName);
            if (res != null) {
            	return res;
            }
        }

        return null;	// no successful parsing
    }

}
