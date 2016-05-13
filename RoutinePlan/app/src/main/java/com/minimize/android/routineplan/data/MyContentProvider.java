package com.minimize.android.routineplan.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

public class MyContentProvider extends ContentProvider {

  static final int ROUTINES = 1;
  static final int ROUTINE_ID = 2;

  static final UriMatcher mUriMatcher;

  static {
    mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    mUriMatcher.addURI(DbContract.AUTHORITY, DbContract.PATH_ROUTINES_TABLE, ROUTINES);
    mUriMatcher.addURI(DbContract.AUTHORITY, DbContract.PATH_ROUTINES_TABLE + "/#", ROUTINE_ID);
  }

  SQLiteDatabase db;

  @Override public boolean onCreate() {
    db = new MyDatabaseHelper(getContext()).getWritableDatabase();
    return db != null;
  }

  @Nullable @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    Cursor c = null;

    int match = mUriMatcher.match(uri);

    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables(DbContract.Routine.TABLE_NAME);
    switch (match) {
      case ROUTINES:
        c = builder.query(db, null, null, null, null, null, null);
        break;
      case ROUTINE_ID:
        builder.appendWhere(DbContract.Routine._ID + "=" + uri.getLastPathSegment());
        c = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        break;
    }
    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Nullable @Override public String getType(Uri uri) {
    return null;
  }

  @Nullable @Override public Uri insert(Uri uri, ContentValues values) {
    Uri newUri = null;
    int match = mUriMatcher.match(uri);

    switch (match) {
      case ROUTINES:
        long id = db.insert(DbContract.Routine.TABLE_NAME, null, values);
        newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        break;
    }
    return newUri;
  }

  @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
    int id = db.delete(DbContract.Routine.TABLE_NAME, selection, selectionArgs);

    getContext().getContentResolver().notifyChange(uri, null);
    return id;
  }

  @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int update = db.update(DbContract.Routine.TABLE_NAME, values, selection, selectionArgs);
    getContext().getContentResolver().notifyChange(uri, null);
    return update;
  }
}