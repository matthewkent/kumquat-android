package com.kumquatcards.ui;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardFragment";

	public static final String ARG_LEVEL = "level";
	public static final String ARG_ORDER = "order";
	public static final String ARG_DEFINITION = "definition";
	public static final String ARG_TRANSLATION = "translation";

	private CardFrontFragment frontFragment;
	private CardBackFragment backFragment;

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
		private String currentText;
		public void setCurrentText(String currentText) {
			this.currentText = currentText;
			View view = getView();
			if(view != null) {
				setCardText(view, currentText);
			}
		}
		public void setCardText(View view, String text) {
			TextView textView = (TextView) view.findViewById(R.id.card_front_text);
			if(textView != null) {
				textView.setText(text);
			}
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_front, container, false);
			if(currentText != null) {
				setCardText(view, currentText);
			}
			return view;
		}
	}

	public static class CardBackFragment extends Fragment {
		private String currentText;
		public void setCurrentText(String currentText) {
			this.currentText = currentText;
		}
		public void setCardText(View view, String text) {
			TextView textView = (TextView) view.findViewById(R.id.card_back_text);
			if(textView != null) {
				textView.setText(text);
			}
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_back, container, false);
			if(currentText != null) {
				setCardText(view, currentText);
			}
			return view;
		}
	}

	public FlashCardFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		frontFragment = new CardFrontFragment();
		backFragment = new CardBackFragment();

		currentLevel = getArguments().getInt(ARG_LEVEL);
		currentOrder = getArguments().getInt(ARG_ORDER);

		totalCount = HskContract.FlashCards.maxOrderForLevel(currentLevel);
		cardStates = new int[totalCount];

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_flash_card, container, false);
		Button nextButton = (Button) view.findViewById(R.id.button_next);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				moveNext(v);
			}
		});
		Button prevButton = (Button) view.findViewById(R.id.button_prev);
		prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				movePrev(v);
			}
		});
		Button flipButton = (Button) view.findViewById(R.id.button_flip);
		flipButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				flipCard(v);
			}
		});
		Button checkButton = (Button) view.findViewById(R.id.button_translate);
		checkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				checkTranslation(v);
			};
		});
		if(savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.card_container, frontFragment).commit();
		}
		updateNav(view);
		return view;
	}

	public void checkTranslation(View view) {
		EditText translationText = (EditText) getActivity().findViewById(R.id.card_translation);
		String input = translationText.getText().toString();

		String translation = backFragment.currentText;
		boolean correct = input.equalsIgnoreCase(translation);
		String message = correct ? "RIGHT!" : "WRONG!";
		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

		if(correct) {
			cardStates[currentOrder - 1] = CARD_STATE_CORRECT;
			correctCount += 1;

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(translationText.getWindowToken(), 0);
			translationText.setText("");
			moveNext(view);
		} else {
			cardStates[currentOrder - 1] = CARD_STATE_INCORRECT;
		}
		updateNav();
	}

	private void updateNav() {
		updateNav(getView());
	}

	private void updateNav(View view) {
		TextView numCorrectView = (TextView) view.findViewById(R.id.card_nav_num_correct);
		numCorrectView.setText(String.format("num correct: %s/%s", correctCount, totalCount));

		TextView currentNumView = (TextView) view.findViewById(R.id.card_nav_current_num);
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
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToFirst()) {
			String translation = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
			backFragment.setCurrentText(translation);
			String definition = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_DEFINITION)));
			frontFragment.setCurrentText(definition);
		} else {
			Log.e(TAG, "oh noes");
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
