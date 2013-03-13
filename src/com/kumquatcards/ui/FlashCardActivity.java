package com.kumquatcards.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flash_card);
		getLoaderManager().initLoader(0, null, this);
	}

	private boolean translate(String card, String input) {
		//return translations.get(card).equalsIgnoreCase(input);
		return true;
	}

	public void checkTranslation(View view) {
		TextView cardText = (TextView) findViewById(R.id.card_text);
		String card = cardText.getText().toString();
		EditText translationText = (EditText) findViewById(R.id.card_translation);
		String translation = translationText.getText().toString();
		
		String message = translate(card, translation) ? "correct!" : "wrong :(";
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, HskContract.FlashCards.CONTENT_URI, new String[] {HskContract.FlashCards.COLUMN_NAME_ID, HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		TextView card = (TextView) findViewById(R.id.card_text);
		if (cursor.moveToFirst()) {
			String text = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
			card.setText(text);
		} else {
			Log.e(TAG, "oh noes");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
