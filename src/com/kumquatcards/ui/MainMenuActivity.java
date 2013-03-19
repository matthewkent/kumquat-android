package com.kumquatcards.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kumquatcards.R;
import com.kumquatcards.provider.HskContract;

public class MainMenuActivity extends Activity {

	public static final String TAG = "MainMenuActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
	}

	public void startQuiz(View view) {
		int level = 0;
		switch(view.getId()) {
		case R.id.button_level1:
			level = 1;
			break;
		case R.id.button_level2:
			level = 2;
			break;
		case R.id.button_level3:
			level = 3;
			break;
		case R.id.button_level4:
			level = 4;
			break;
		case R.id.button_level5:
			level = 5;
			break;
		case R.id.button_level6:
			level = 6;
			break;
		default:
			break;
		}

		Log.i(TAG, "starting quiz level: " + level);

		Uri uri = HskContract.FlashCards.buildFlashCardsUri(level);

		Intent intent = new Intent(Intent.ACTION_VIEW, uri, this, FlashCardActivity.class);
		startActivity(intent);
	}
}
