package com.minimize.android.routineplan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ahmedrizwan on 24/01/2016.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
  // Database Version
  private static final int DATABASE_VERSION = 2;

  // Database Name
  private static final String DATABASE_NAME = "routines";


  public MyDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL(DbContract.Routine.CREATE_ROUTINE_TABLE);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // on upgrade drop older tables
    db.execSQL(DbContract.Routine.DROP_ROUTINE_TABLE);
    // create new tables
    onCreate(db);
  }
}
