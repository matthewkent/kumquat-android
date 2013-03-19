package com.kumquatcards.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardActivity";

//	private static final int CARD_STATE_NONE = 0;
//	private static final int CARD_STATE_CORRECT = 1;
//	private static final int CARD_STATE_INCORRECT = 2;
//
//	private int[] cardStates;
	private int totalCount;
	private int correctCount = 0;
	private int currentLevel;
	private int currentIndex;

	private FlashCardPagerAdapter pagerAdapter;

	public class FlashCardPagerAdapter extends FragmentStatePagerAdapter {
		private Cursor cursor;

		public FlashCardPagerAdapter(FragmentManager manager, Cursor cursor) {
			super(manager);
			this.cursor = cursor;
		}

		@Override
		public android.support.v4.app.Fragment getItem(int i) {
			Fragment fragment = new FlashCardFragment();
			Bundle args = new Bundle();
			cursor.moveToPosition(i);
			String definition = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_DEFINITION)));
			String translation = new String(cursor.getBlob(cursor.getColumnIndex(HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED)));
			args.putString(FlashCardFragment.ARG_DEFINITION, definition);
			args.putString(FlashCardFragment.ARG_TRANSLATION, translation);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			if(cursor == null) {
				return 0;
			} else {
				return cursor.getCount();
			}
		}

		public void swapCursor(Cursor c) {
			if(this.cursor == c) {
				return;
			}
			this.cursor = c;
			notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flash_card);

		Button checkButton = (Button) findViewById(R.id.button_translate);
		checkButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				checkTranslation(v);
			};
		});

		currentLevel = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		totalCount = HskContract.FlashCards.maxOrderForLevel(currentLevel);
//		cardStates = new int[totalCount];

		pagerAdapter = new FlashCardPagerAdapter(getSupportFragmentManager(), null);
		ViewPager pager = (ViewPager) findViewById(R.id.flash_card_pager);
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				currentIndex = position;
				updateNav();
			}
		});

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = HskContract.FlashCards.buildFlashCardsUri(currentLevel);
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(cursor.getCount() != totalCount) {
			throw new IllegalArgumentException("oh noes");
		}
		pagerAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public void checkTranslation(View view) {
		EditText translationText = (EditText) findViewById(R.id.card_translation);
		String input = translationText.getText().toString();

		ViewPager pager = (ViewPager) findViewById(R.id.flash_card_pager);
		int index = pager.getCurrentItem();
		FlashCardFragment fragment = (FlashCardFragment) pagerAdapter.getItem(index);
		String translation = fragment.getArguments().getString(FlashCardFragment.ARG_TRANSLATION);

		boolean correct = input.equalsIgnoreCase(translation);
		String message = correct ? "RIGHT!" : "WRONG!";
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

		if(correct) {
//			cardStates[currentOrder - 1] = CARD_STATE_CORRECT;
			correctCount += 1;

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(translationText.getWindowToken(), 0);
			translationText.setText("");
		} else {
//			cardStates[currentOrder - 1] = CARD_STATE_INCORRECT;
		}
		updateScores();
	}

	private void updateNav() {
		updateScores();
		updateIndex();
	}

	private void updateScores() {
		TextView numCorrectView = (TextView) findViewById(R.id.card_nav_num_correct);
		numCorrectView.setText(String.format("num correct: %s/%s", correctCount, totalCount));
	}

	private void updateIndex() {
		TextView currentNumView = (TextView) findViewById(R.id.card_nav_current_num);
		currentNumView.setText(String.format("current card: %s/%s", currentIndex + 1, totalCount));
	}
}
