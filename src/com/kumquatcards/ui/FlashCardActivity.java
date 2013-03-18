package com.kumquatcards.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kumquatcards.R;

public class FlashCardActivity extends Activity {
	public static final String TAG = "FlashCardActivity";

	public static class FlashCardPagerAdapter extends FragmentStatePagerAdapter {
		private int count;

		public FlashCardPagerAdapter(FragmentManager manager, int count) {
			super(manager);
			this.count = count;
		}

		@Override
		public android.support.v4.app.Fragment getItem(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			return count;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flash_card);

		int level= Integer.parseInt(getIntent().getData().getPathSegments().get(1));
		int order = Integer.parseInt(getIntent().getData().getPathSegments().get(2));

		Fragment fragment = new FlashCardFragment();
		Bundle args = new Bundle();
		args.putInt(FlashCardFragment.ARG_LEVEL, level);
		args.putInt(FlashCardFragment.ARG_ORDER, order);
		fragment.setArguments(args);
		getFragmentManager().beginTransaction().add(R.id.flash_card_fragment_container, fragment).commit();
	}

}
