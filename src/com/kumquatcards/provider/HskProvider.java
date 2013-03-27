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

	private HskDbHelper hskDbHelper;
	private ScoreDbHelper scoreDbHelper;

	private static UriMatcher uriMatcher;

	private static final int URI_FLASH_CARDS = 1;
	private static final int URI_SCORES = 2;
	private static final int URI_ALL_SCORES= 3;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(HskContract.AUTHORITY, "flashcards/#", URI_FLASH_CARDS);
		uriMatcher.addURI(HskContract.AUTHORITY, "scores/#", URI_SCORES);
		uriMatcher.addURI(HskContract.AUTHORITY, "scores", URI_ALL_SCORES);
	}

	@Override
	public boolean onCreate() {
		hskDbHelper = new HskDbHelper(getContext());
		scoreDbHelper = new ScoreDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			SQLiteDatabase db;
			int level;
			Cursor c;
			switch(uriMatcher.match(uri)) {
			case URI_FLASH_CARDS:
				db = hskDbHelper.getDatabase();
				level = Integer.parseInt(uri.getPathSegments().get(1));
				String tables = "hsk_lists INNER JOIN translations ON hsk_lists.translation_id = translations._id";
				String selection2 = "hsk_lists.level_number = " + level;
				c = db.query(tables, projection, selection2, null, null, null, null);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			case URI_SCORES:
				db = scoreDbHelper.getReadableDatabase();
				level = Integer.parseInt(uri.getPathSegments().get(1));
				c = db.query(HskContract.Scores.TABLE_NAME, null, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = " + level, null, null, null, null);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			case URI_ALL_SCORES:
				db = scoreDbHelper.getReadableDatabase();
				c = db.query(HskContract.Scores.TABLE_NAME, null, null, null, null, null, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " ASC");
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
		switch(uriMatcher.match(uri)) {
		case URI_SCORES:
			int level = Integer.parseInt(uri.getPathSegments().get(1));
			SQLiteDatabase db = scoreDbHelper.getWritableDatabase();
			int count = db.update(HskContract.Scores.TABLE_NAME, values, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = " + level, null);
			if(count == 0) {
				values.put(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, level);
				db.insert(HskContract.Scores.TABLE_NAME, null, values);
			}
			getContext().getContentResolver().notifyChange(uri, null);
			getContext().getContentResolver().notifyChange(HskContract.Scores.buildAllScoresUri(), null);
			return count;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

}
