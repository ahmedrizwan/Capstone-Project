package com.minimize.android.routineplan.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class DbContract {
  public static final String AUTHORITY = "com.minimize.android.routineplan";
  public static final String PATH_ROUTINES_TABLE = "routines";
  public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

  public static class Routine implements BaseColumns {
    public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_ROUTINES_TABLE).build();
    public static final String TABLE_NAME = "routine";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIME = "time";

    //Create Table University
    public static final String CREATE_ROUTINE_TABLE = "CREATE TABLE " + TABLE_NAME+ " ("
        + Routine._ID + " INTEGER PRIMAYR KEY,"
        + Routine.COLUMN_NAME + " TEXT,"
        + Routine.COLUMN_TIME + " TEXT,"
        + " UNIQUE (" + Routine.COLUMN_NAME + ") ON CONFLICT REPLACE"
        + " );";

    public static final String DROP_ROUTINE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
  }
}