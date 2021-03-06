package com.kumquatcards.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoreDbHelper extends SQLiteOpenHelper {

	private static String DB_NAME = "scores.db";

	public ScoreDbHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + HskContract.Scores.TABLE_NAME + " ("
				+ HskContract.Scores.COLUMN_NAME_ID + " integer primary key autoincrement, "
				+ HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " integer, "
				+ HskContract.Scores.COLUMN_NAME_SCORE_DATA + " text, "
				+ HskContract.Scores.COLUMN_NAME_CURRENT_CARD + " integer)");

		for(int i = 1; i <= 6; i++) {
			ContentValues values = new ContentValues();
			values.put(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, i);
			db.insert(HskContract.Scores.TABLE_NAME, null, values);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
