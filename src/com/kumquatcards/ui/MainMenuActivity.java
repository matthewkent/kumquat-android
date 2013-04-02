package com.kumquatcards.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class MainMenuActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = "MainMenuActivity";

	private class MainMenuCursorAdapter extends SimpleCursorAdapter {

		public MainMenuCursorAdapter(Context context) {
			super(context, R.layout.list_item_main_menu, null, new String[] {HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, HskContract.Scores.COLUMN_NAME_CURRENT_CARD}, null, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			int level = cursor.getInt(cursor.getColumnIndex(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER));
			TextView itemLabel = (TextView) view.findViewById(R.id.list_item_title);
			itemLabel.setText(String.format("HSK %s", level));
			int index = cursor.getInt(cursor.getColumnIndex(HskContract.Scores.COLUMN_NAME_CURRENT_CARD));
			if(index > 0) {
				TextView itemIndex = (TextView) view.findViewById(R.id.list_item_index);
				itemIndex.setText(String.format("%s/%s", index, HskContract.FlashCards.maxOrderForLevel(level)));
			}
		}
	}

	private SimpleCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		adapter = new MainMenuCursorAdapter(this);
		ListView list = (ListView) findViewById(R.id.main_menu_list);
		// the header view will occupy position 0 in the list
		list.addHeaderView(getLayoutInflater().inflate(R.layout.list_header_main_menu, list, false), null, false);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startQuiz(position);
			}
		});

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_credits:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.credits_content).setTitle(R.string.credits_title);
			builder.create().show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void startQuiz(int level) {
		Uri uri = HskContract.FlashCards.buildFlashCardsUri(level);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, FlashCardActivity.class);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = HskContract.Scores.buildAllScoresUri();
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
