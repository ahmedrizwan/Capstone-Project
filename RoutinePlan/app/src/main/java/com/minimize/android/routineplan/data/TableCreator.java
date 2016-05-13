package com.minimize.android.routineplan.data;

class TableCreator {
    String query = "";

    public TableCreator(String table_Name) {
      query = "Create Table " + table_Name + " (";
    }

    public TableCreator addTextColumn(String columnName) {
      query += query.endsWith("(") ? columnName + " TEXT" : "," + columnName + " TEXT";
      return this;
    }

    public TableCreator addIntColumn(String columnName) {
      String integer = " INTEGER";
      query += query.endsWith("(") ? columnName + integer : "," + columnName + integer;
      return this;
    }

    public TableCreator addRealColumn(String columnName) {
      String integer = " REAL";
      query += query.endsWith("(") ? columnName + integer : "," + columnName + integer;
      return this;
    }

    public TableCreator addPrimaryKey(String columnName) {
      query += query.endsWith("(") ? columnName + " INTEGER PRIMARY KEY AUTOINCREMENT" : "," + columnName + " INTEGER PRIMARY KEY";
      return this;
    }

    public String build() {
      return query + ");";
    }
  }