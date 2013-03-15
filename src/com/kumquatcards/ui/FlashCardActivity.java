package com.kumquatcards.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
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

	private String currentTranslation;
	private int currentLevel;
	private int currentOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		currentLevel = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		currentOrder = Integer.parseInt(getIntent().getData().getPathSegments().get(2));

		setContentView(R.layout.activity_flash_card);
		getLoaderManager().initLoader(0, null, this);
	}

	public void checkTranslation(View view) {
		EditText translationText = (EditText) findViewById(R.id.card_translation);
		String input = translationText.getText().toString();
		
		String message = input.equalsIgnoreCase(currentTranslation) ? "correct!" : "wrong :(";
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public void movePrev(View view) {
		if (currentOrder > 1) {
			currentOrder -= 1;
		}
		getLoaderManager().restartLoader(0, null, this);
	}

	public void moveNext(View view) {
		if (currentOrder < HskContract.FlashCards.maxOrderForLevel(currentLevel)) {
			currentOrder += 1;
		}
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = HskContract.FlashCards.buildFlashCardUri(currentLevel, currentOrder);
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		TextView card = (TextView) findViewById(R.id.card_text);
		if (cursor.moveToFirst()) {
			currentTranslation = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
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
