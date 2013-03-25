package com.kumquatcards.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class MainMenuActivity extends Activity {

	public static final String TAG = "MainMenuActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		String[] levels = new String[] {"HSK 1", "HSK 2", "HSK 3", "HSK 4", "HSK 5", "HSK 6"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, levels);
		ListView list = (ListView) findViewById(R.id.main_menu_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startQuiz(position + 1);
			}
		});
	}

	public void startQuiz(int level) {
		Log.i(TAG, "starting quiz level: " + level);

		Uri uri = HskContract.FlashCards.buildFlashCardsUri(level);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, FlashCardActivity.class);
		startActivity(intent);
	}
}
