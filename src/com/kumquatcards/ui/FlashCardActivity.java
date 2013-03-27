package com.kumquatcards.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
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
import android.widget.TextView;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardActivity";

	private static final int LOADER_FLASH_CARDS = 1;
	private static final int LOADER_SCORES = 2;

	private int totalCount;
	private int currentLevel;
	private int currentIndex;

	private Set<Integer> cardScores = null;

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
			args.putInt(FlashCardFragment.ARG_INDEX, i + 1);
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

		currentLevel = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		totalCount = HskContract.FlashCards.maxOrderForLevel(currentLevel);

		pagerAdapter = new FlashCardPagerAdapter(getSupportFragmentManager(), null);
		ViewPager pager = (ViewPager) findViewById(R.id.flash_card_pager);
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				currentIndex = position + 1;
				updateNav();
			}
		});

		getSupportLoaderManager().initLoader(LOADER_FLASH_CARDS, null, this);
		getSupportLoaderManager().initLoader(LOADER_SCORES, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri;
		switch(id) {
		case LOADER_FLASH_CARDS:
			uri = HskContract.FlashCards.buildFlashCardsUri(currentLevel);
			return new CursorLoader(this, uri, null, null, null, null);
		case LOADER_SCORES:
			uri = HskContract.Scores.buildScoresUri(currentLevel);
			return new CursorLoader(this, uri, null, null, null, null);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()) {
		case LOADER_FLASH_CARDS:
			if(cursor.getCount() != totalCount) {
				throw new IllegalArgumentException("oh noes");
			}
			pagerAdapter.swapCursor(cursor);
			break;
		case LOADER_SCORES:
			if(cursor.moveToFirst()) {
				String scoreData = cursor.getString(cursor.getColumnIndex(HskContract.Scores.COLUMN_NAME_SCORE_DATA));
				cardScores = deserializeScoreData(scoreData);
			} else {
				cardScores = new HashSet<Integer>();
			}
			updateScores();
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	protected void onPause() {
		super.onPause();

		Uri uri = HskContract.Scores.buildScoresUri(currentLevel);
		ContentValues values = new ContentValues();
		values.put(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, currentLevel);
		values.put(HskContract.Scores.COLUMN_NAME_SCORE_DATA, serializeScoreData(cardScores));
		values.put(HskContract.Scores.COLUMN_NAME_CURRENT_CARD, currentIndex);
		getContentResolver().update(uri, values, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = " + currentLevel, null);
	}

	public void markCardCorrect(int index) {
		cardScores.add(index);
		updateScores();
	}

	private void updateNav() {
		updateScores();
		updateIndex();
	}

	private void updateScores() {
		TextView numCorrectView = (TextView) findViewById(R.id.card_nav_num_correct);
		numCorrectView.setText(String.format("num correct: %s/%s", cardScores.size(), totalCount));
	}

	private void updateIndex() {
		TextView currentNumView = (TextView) findViewById(R.id.card_nav_current_num);
		currentNumView.setText(String.format("current card: %s/%s", currentIndex, totalCount));
	}

	private Set<Integer> deserializeScoreData(String data) {
		Set<Integer> set = new HashSet<Integer>();
		for(String s: data.split(",")) {
			set.add(Integer.valueOf(s));
		}
		return set;
	}

	private String serializeScoreData(Set<Integer> data) {
		StringBuffer sb = new StringBuffer();
		for(Integer i: data) {
			sb.append(i).append(",");
		}
		return sb.toString();
	}
}
