package com.metropolis.stocks.database;


import com.metropolis.stocks.database.impl.LocalSQLiteDatabase;

import java.util.List;
import java.util.Map;

public abstract class Database {

    private static Database instance;

    public static Database getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new LocalSQLiteDatabase();
        return instance;
    }

    public abstract void startDatabase();

    public abstract void stopDatabase();

    public abstract void migrateSnapshot();

    public abstract void insertMapValues(final String tableName, final List<Map<String, Object>> dataMap);

    public abstract boolean doesTableExist(final String tableName);

    public abstract List executeSQLQuery(final String query);

}
