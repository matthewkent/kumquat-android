package com.kumquatcards.provider;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HskDbHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "hsk.db";

    private Context context;
    private SQLiteDatabase db;

	public HskDbHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private boolean dbExists() {
		return context.getDatabasePath(DB_NAME).exists();
	}

	private void createDb() throws IOException {
		InputStream input = null;
		try {
			input = context.getResources().getAssets().open(DB_NAME);
			FileUtils.copyInputStreamToFile(input, context.getDatabasePath(DB_NAME));
		} finally {
			if(input != null) {
				input.close();
			}
		}
	}

	public SQLiteDatabase getDatabase() throws IOException {
		if(!dbExists()) {
			createDb();
		}
		if(db == null) {
			db = SQLiteDatabase.openDatabase(context.getDatabasePath(DB_NAME).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
		}
		return db;
	}

	@Override
	public synchronized void close() {
		super.close();
		if(db != null) {
			db.close();
		}
	}
}
