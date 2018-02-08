package com.metropolis.util;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metropolis.stocks.database.Database;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Cache {

    private static final String CACHE_TABLE = "metropolis_cache";

    private static final Database database = Database.getInstance();

    static {
        if (!database.doesTableExist(CACHE_TABLE)) {
            database.executeSQLQuery("create table " + CACHE_TABLE + " (cachekey varchar NOT NULL, cachevalue varchar NOT NULL, PRIMARY KEY (cachekey))");
        }
    }

    public static Optional<String> getString(final String key) {
        List results = database.executeSQLQuery("select cachevalue from " + CACHE_TABLE +
                                                " where cachekey='" + key + "'");
        if (results.size() <= 0) {
            return Optional.empty();
        }
        return Optional.of((String) ((Map) results.get(0)).get("cachevalue"));
    }

    public static void putString(final String key, final String value) {
        database.insertMapValues(CACHE_TABLE, ImmutableList.of(ImmutableMap.of("cachekey", key,
                                                                               "cachevalue", value)));
    }

    public static void delete(final String key) {
        database.executeSQLQuery("delete from " + CACHE_TABLE + " where cachekey='" + key + "'");
    }

}
