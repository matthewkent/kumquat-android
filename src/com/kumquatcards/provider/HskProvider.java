package com.kumquatcards.provider;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class HskProvider extends ContentProvider {

	private static final String TAG = "HskProvider";

	private HskDbHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new HskDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			SQLiteDatabase db = dbHelper.getDatabase();
			// just get the first one for testing
			Cursor c = db.query(HskContract.HskLists.TABLE_NAME, new String[] {HskContract.HskLists.COLUMN_NAME_TRANSLATION_ID}, HskContract.HskLists.COLUMN_NAME_LEVEL_NUMBER + " = 1 AND " + HskContract.HskLists.COLUMN_NAME_ORDER_NUMBER + " = 1", null, null, null, null);
			c.moveToNext();
			int translationId = c.getInt(0);
			c.close();
			c = db.query(HskContract.Translations.TABLE_NAME, new String[] {HskContract.Translations.COLUMN_NAME_ID, HskContract.Translations.COLUMN_NAME_SIMPLIFIED}, HskContract.Translations.COLUMN_NAME_ID + " = " + translationId, null, null, null, null);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		} catch (IOException e) {
			Log.e(TAG, "Error opening database", e);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return HskContract.Translations.CONTENT_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
