package com.kumquatcards.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardActivity";

	private String simplified;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_card);
		getLoaderManager().initLoader(0, null, this);
	}

	public void checkTranslation(View view) {
		EditText translationText = (EditText) findViewById(R.id.card_translation);
		String input = translationText.getText().toString();
		
		String message = input.equalsIgnoreCase(simplified) ? "correct!" : "wrong :(";
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void movePrev(View view) {
		int level = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		int order = Integer.parseInt(getIntent().getData().getPathSegments().get(2));
		if (order > 1) {
			order -= 1;
		}
		Uri uri = HskContract.FlashCards.buildFlashCardUri(level, order);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, FlashCardActivity.class);
		startActivity(intent);
	}

	public void moveNext(View view) {
		int level = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		int order = Integer.parseInt(getIntent().getData().getPathSegments().get(2));
		if (order < HskContract.FlashCards.maxOrderForLevel(level)) {
			order += 1;
		}
		Uri uri = HskContract.FlashCards.buildFlashCardUri(level, order);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, FlashCardActivity.class);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, getIntent().getData(), null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		TextView card = (TextView) findViewById(R.id.card_text);
		if (cursor.moveToFirst()) {
			simplified = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
			String text = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_DEFINITION)));
			card.setText(text);
		} else {
			Log.e(TAG, "oh noes");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
