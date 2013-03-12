package com.kumquatcards;

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

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
		return new CursorLoader(this, HskContract.Translations.CONTENT_URI, new String[] {HskContract.Translations.COLUMN_NAME_ID, HskContract.Translations.COLUMN_NAME_SIMPLIFIED}, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		TextView card = (TextView) findViewById(R.id.card_text);
		if (cursor.moveToFirst()) {
			String text = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.Translations.COLUMN_NAME_SIMPLIFIED)));
			card.setText(text);
		} else {
			Log.e(TAG, "oh noes");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
