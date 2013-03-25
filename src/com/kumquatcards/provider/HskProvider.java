package com.kumquatcards.provider;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class HskProvider extends ContentProvider {

	private static final String TAG = "HskProvider";

	private HskDbHelper dbHelper;

	private static UriMatcher uriMatcher;

	private static final int URI_FLASH_CARDS = 1;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(HskContract.AUTHORITY, "flashcards/#", URI_FLASH_CARDS);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new HskDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			switch(uriMatcher.match(uri)) {
			case URI_FLASH_CARDS:
				SQLiteDatabase db = dbHelper.getDatabase();
				int level = Integer.parseInt(uri.getPathSegments().get(1));
				String tables = "hsk_lists INNER JOIN translations ON hsk_lists.translation_id = translations._id";
				String selection2 = "hsk_lists.level_number = " + level;
				Cursor c = null;
				c = db.query(tables, projection, selection2, null, null, null, null);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} catch (IOException e) {
			Log.e(TAG, "Error opening HSK database", e);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return HskContract.FlashCards.CONTENT_TYPE;
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
