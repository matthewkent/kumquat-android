package com.kumquatcards.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardActivity";

	private String currentTranslation;
	private String currentDefinition;
	private int currentLevel;
	private int currentOrder;
	private boolean showingBack = false;

	private static final int CARD_STATE_NONE = 0;
	private static final int CARD_STATE_CORRECT = 1;
	private static final int CARD_STATE_INCORRECT = 2;

	private int[] cardStates;
	private int totalCount;
	private int correctCount = 0;

	public static class CardFrontFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_front, container, false);
			String text = ((FlashCardActivity) getActivity()).currentDefinition;
			if(text != null) {
				TextView cardFront = (TextView) view.findViewById(R.id.card_front_text);
				cardFront.setText(text);
			}
			return view;
		}
	}

	public static class CardBackFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_back, container, false);
			String text = ((FlashCardActivity) getActivity()).currentTranslation;
			if(text != null) {
				TextView cardBack = (TextView) view.findViewById(R.id.card_back_text);
				cardBack.setText(text);
			}
			return view;
		}
	}


	private Fragment frontFragment;
	private Fragment backFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		frontFragment = new CardFrontFragment();
		backFragment = new CardBackFragment();

		currentLevel = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		currentOrder = Integer.parseInt(getIntent().getData().getPathSegments().get(2));

		totalCount = HskContract.FlashCards.maxOrderForLevel(currentLevel);
		cardStates = new int[totalCount];

		setContentView(R.layout.activity_flash_card);
		getLoaderManager().initLoader(0, null, this);
		updateNav();

		if(savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.card_container, frontFragment).commit();
		}
	}

	public void checkTranslation(View view) {
		EditText translationText = (EditText) findViewById(R.id.card_translation);
		String input = translationText.getText().toString();

		boolean correct = input.equalsIgnoreCase(currentTranslation);
		String message = correct ? "RIGHT!" : "WRONG!";
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

		if(correct) {
			cardStates[currentOrder - 1] = CARD_STATE_CORRECT;
			correctCount += 1;

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(translationText.getWindowToken(), 0);
			translationText.setText("");
			moveNext(view);
		} else {
			cardStates[currentOrder - 1] = CARD_STATE_INCORRECT;
		}
		updateNav();
	}

	private void updateNav() {
		TextView numCorrectView = (TextView) findViewById(R.id.card_nav_num_correct);
		numCorrectView.setText(String.format("num correct: %s/%s", correctCount, totalCount));

		TextView currentNumView = (TextView) findViewById(R.id.card_nav_current_num);
		currentNumView.setText(String.format("current index: %s/%s", currentOrder, totalCount));
	}

	public void movePrev(View view) {
		if (currentOrder > 1) {
			currentOrder -= 1;
		}
		getLoaderManager().restartLoader(0, null, this);
		updateNav();
	}

	public void moveNext(View view) {
		if (currentOrder < HskContract.FlashCards.maxOrderForLevel(currentLevel)) {
			currentOrder += 1;
		}
		getLoaderManager().restartLoader(0, null, this);
		updateNav();
	}

	public void flipCard(View view) {
		if(showingBack) {
			showingBack = false;
			getFragmentManager().beginTransaction().
				setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out).
				replace(R.id.card_container, frontFragment).commit();
		} else {
			showingBack = true;
			getFragmentManager().beginTransaction().
				setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out).
				replace(R.id.card_container, backFragment).commit();
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = HskContract.FlashCards.buildFlashCardUri(currentLevel, currentOrder);
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToFirst()) {
			currentTranslation = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
			currentDefinition = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_DEFINITION)));

			if(showingBack) {
				TextView cardBack = (TextView) backFragment.getView().findViewById(R.id.card_back_text);
				cardBack.setText(currentTranslation);
			} else {
				TextView cardFront = (TextView) frontFragment.getView().findViewById(R.id.card_front_text);
				cardFront.setText(currentDefinition);
			}
		} else {
			Log.e(TAG, "oh noes");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
