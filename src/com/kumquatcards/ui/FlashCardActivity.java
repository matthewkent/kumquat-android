package com.kumquatcards.ui;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class FlashCardActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "FlashCardActivity";

	private static final int LOADER_FLASH_CARDS = 1;
	private static final int LOADER_SCORES = 2;

	private static final String PREF_KEY_CHARSET = "character_set";
	private static final int PREF_CHARSET_SIMPLIFIED = 1;
	private static final int PREF_CHARSET_TRADITIONAL = 2;

	private int totalCount;
	private int currentLevel;
	private int currentIndex = 0;
	private int currentCharset = PREF_CHARSET_SIMPLIFIED;
	private int savedIndex = -1;

	private Set<Integer> cardScores = null;

	private FlashCardPagerAdapter pagerAdapter;
	private ViewPager pager;
	private Menu menu;
	private ViewGroup statusView;

	public class FlashCardPagerAdapter extends FragmentStatePagerAdapter {
		private Cursor cursor;

		public FlashCardPagerAdapter(FragmentManager manager, Cursor cursor) {
			super(manager);
			this.cursor = cursor;
		}

		private String getString(Cursor cursor, String columnName) {
			int columnIndex = cursor.getColumnIndex(columnName);
			int type = cursor.getType(columnIndex);
			switch(type) {
			case Cursor.FIELD_TYPE_BLOB:
				return new String(cursor.getBlob(columnIndex));
			case Cursor.FIELD_TYPE_STRING:
				return cursor.getString(columnIndex);
			default:
				throw new IllegalArgumentException("Something has gone terribly wrong, column " + columnName+ " was returned as data type " + type);
			}
		}

		@Override
		public android.support.v4.app.Fragment getItem(int i) {
			if(getCount() == 0) {
				return null;
			}
			Fragment fragment = new FlashCardFragment();
			Bundle args = new Bundle();
			cursor.moveToPosition(i);
			int index = i + 1;
			String definition = getString(cursor, HskContract.FlashCards.COLUMN_NAME_DEFINITION);
			String translation = null;
			switch(currentCharset) {
			case PREF_CHARSET_SIMPLIFIED:
				translation = getString(cursor, HskContract.FlashCards.COLUMN_NAME_SIMPLIFIED);
				break;
			case PREF_CHARSET_TRADITIONAL:
				translation = getString(cursor, HskContract.FlashCards.COLUMN_NAME_TRADITIONAL);
				break;
			default:
				break;
			}
			String pinyin = getString(cursor, HskContract.FlashCards.COLUMN_NAME_PINYIN);
			args.putString(FlashCardFragment.ARG_DEFINITION, definition);
			args.putString(FlashCardFragment.ARG_TRANSLATION, translation);
			args.putString(FlashCardFragment.ARG_PINYIN, pinyin);
			args.putInt(FlashCardFragment.ARG_INDEX, index);
			args.putBoolean(FlashCardFragment.ARG_SHOW_FRONT, !cardScores.contains(index));
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

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flash_card);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		statusView=(ViewGroup)inflater.inflate(R.layout.flash_card_actionbar_text, null);
		getActionBar().setCustomView(statusView);
		getActionBar().setDisplayShowCustomEnabled(true);

		if(savedInstanceState != null) {
			savedIndex = savedInstanceState.getInt("savedIndex");
		}
		currentLevel = Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		totalCount = HskContract.FlashCards.maxOrderForLevel(currentLevel);
		currentCharset = getPreferences(MODE_PRIVATE).getInt(PREF_KEY_CHARSET, PREF_CHARSET_SIMPLIFIED);
		setTitle("HSK " + currentLevel);
		pagerAdapter = new FlashCardPagerAdapter(getSupportFragmentManager(), null);
		pager = (ViewPager) findViewById(R.id.flash_card_pager);
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				currentIndex = position + 1;
				updateIndex();
			}
		});

		getSupportLoaderManager().initLoader(LOADER_FLASH_CARDS, null, this);
		getSupportLoaderManager().initLoader(LOADER_SCORES, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("savedIndex", currentIndex);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.flash_card, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		switch(currentCharset) {
		case PREF_CHARSET_SIMPLIFIED:
			menu.findItem(R.id.option_charset_simplified).setChecked(true);
			break;
		case PREF_CHARSET_TRADITIONAL:
			menu.findItem(R.id.option_charset_traditional).setChecked(true);
			break;
		default:
			break;
		}
		postOnLoadFinished();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_start_over:
			cardScores = new HashSet<Integer>();
			currentIndex = 1;
			pager.setCurrentItem(0, false);
			updateNav();
			return true;
		case R.id.option_charset_simplified:
			switchCharset(item, PREF_CHARSET_SIMPLIFIED);
			return true;
		case R.id.option_charset_traditional:
			switchCharset(item, PREF_CHARSET_TRADITIONAL);
			return true;
		case android.R.id.home:
            // This is called when the Home (Up) button is pressed
            // in the Action Bar.
            Intent parentActivityIntent = new Intent(this, MainMenuActivity.class);
            parentActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void switchCharset(MenuItem item, int charset) {
		if(currentCharset == charset) {
			return;
		}
		currentCharset = charset;
		SharedPreferences.Editor prefs = getPreferences(MODE_PRIVATE).edit();
		prefs.putInt(PREF_KEY_CHARSET, charset);
		prefs.commit();
		item.setChecked(true);
		getSupportLoaderManager().restartLoader(LOADER_FLASH_CARDS, null, this);
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

	private Cursor cardCursor;

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()) {
		case LOADER_FLASH_CARDS:
			if(cursor.getCount() != totalCount) {
				throw new IllegalArgumentException("oh noes");
			}
			cardCursor = cursor;
			break;
		case LOADER_SCORES:
			if(cursor.moveToFirst()) {
				String scoreData = cursor.getString(cursor.getColumnIndex(HskContract.Scores.COLUMN_NAME_SCORE_DATA));
				cardScores = deserializeScoreData(scoreData);
				if(savedIndex > 0) {
					currentIndex = savedIndex;
				} else {
					currentIndex = cursor.getInt(cursor.getColumnIndex(HskContract.Scores.COLUMN_NAME_CURRENT_CARD));
				}
			} else {
				cardScores = new HashSet<Integer>();
			}
			if(currentIndex < 1) {
				currentIndex = 1;
			}
			break;
		}
		postOnLoadFinished();
	}

	private synchronized void postOnLoadFinished() {
		if(cardCursor != null && cardScores != null && menu != null) {
			pagerAdapter.swapCursor(cardCursor);
			pager.setCurrentItem(currentIndex - 1, false);
			updateNav();
			cardCursor = null;
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
		if(statusView != null) {
			TextView text = (TextView) statusView.findViewById(R.id.card_correct);
			text.setText(String.format("%s", cardScores.size()));
		}
	}

	private void updateIndex() {
		if(statusView != null) {
			TextView text = (TextView) statusView.findViewById(R.id.card_index);
			text.setText(String.format("#%s/%s", currentIndex, totalCount));
		}
	}

	private Set<Integer> deserializeScoreData(String data) {
		Set<Integer> set = new HashSet<Integer>();
		if(data != null && data.length() > 0) {
			for(String s: data.split(",")) {
				set.add(Integer.valueOf(s));
			}
		}
		return set;
	}

	private String serializeScoreData(Set<Integer> data) {
		StringBuffer sb = new StringBuffer();
		if(data.size() > 0) {
			for(Integer i: data) {
				sb.append(i).append(",");
			}
		}
		return sb.toString();
	}
}
