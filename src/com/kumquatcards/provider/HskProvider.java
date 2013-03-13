package com.kumquatcards.provider;

import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class HskProvider extends ContentProvider {

	private static final String TAG = "HskProvider";

	private HskDbHelper dbHelper;

	private static UriMatcher uriMatcher;

	private static final int URI_FLASH_CARD = 1;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(HskContract.AUTHORITY, "flashcards/#/#", URI_FLASH_CARD);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new HskDbHelper(getContext());
		return true;
	}

	private int getFlashCardId(SQLiteDatabase db, int levelNumber, int orderNumber) {
		String selection = HskContract.HskLists.COLUMN_NAME_LEVEL_NUMBER + " = " + levelNumber + " AND " + HskContract.HskLists.COLUMN_NAME_ORDER_NUMBER + " = " + orderNumber;
		Cursor c = db.query(HskContract.HskLists.TABLE_NAME, new String[] {HskContract.HskLists.COLUMN_NAME_TRANSLATION_ID}, selection, null, null, null, null);
		int cardId = 0;
		if(c.moveToNext()) {
			cardId = c.getInt(0);
		}
		c.close();
		return cardId;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			switch(uriMatcher.match(uri)) {
			case URI_FLASH_CARD:
				SQLiteDatabase db = dbHelper.getDatabase();
				int levelNumber = Integer.parseInt(uri.getPathSegments().get(1));
				int orderNumber = Integer.parseInt(uri.getPathSegments().get(2));
				int cardId = getFlashCardId(db, levelNumber, orderNumber);
				Cursor c = null;
				if (cardId > 0) {
					c = db.query(HskContract.FlashCards.TABLE_NAME, projection, HskContract.FlashCards.COLUMN_NAME_ID + " = " + cardId, null, null, null, null);
				} else {
					c = new MatrixCursor(new String[] {}, 0);
				}
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
