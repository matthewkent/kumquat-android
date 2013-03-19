package com.kumquatcards.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kumquatcards.R;

public class FlashCardFragment extends Fragment {
	public static final String TAG = "FlashCardFragment";

	public static final String ARG_DEFINITION = "definition";
	public static final String ARG_TRANSLATION = "translation";

	private CardFrontFragment frontFragment;
	private CardBackFragment backFragment;

	private String definition;
	private String translation;

	public static class CardFrontFragment extends Fragment {
		private String currentText;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			currentText = getArguments().getString(ARG_DEFINITION);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_front, container, false);
			TextView textView = (TextView) view.findViewById(R.id.card_front_text);
			textView.setText(currentText);
			return view;
		}
	}

	public static class CardBackFragment extends Fragment {
		private String currentText;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			currentText = getArguments().getString(ARG_TRANSLATION);
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_flash_card_back, container, false);
			TextView textView = (TextView) view.findViewById(R.id.card_back_text);
			textView.setText(currentText);
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

		definition = getArguments().getString(ARG_DEFINITION);
		translation = getArguments().getString(ARG_TRANSLATION);

		Bundle frontArgs = new Bundle();
		frontArgs.putString(ARG_DEFINITION, definition);
		frontFragment.setArguments(frontArgs);
		Bundle backArgs = new Bundle();
		backArgs.putString(ARG_TRANSLATION, translation);
		backFragment.setArguments(backArgs);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_flash_card, container, false);
		getChildFragmentManager().beginTransaction().add(R.id.card_container, frontFragment).commit();
//		Button flipButton = (Button) view.findViewById(R.id.button_flip);
//		flipButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				flipCard(v);
//			}
//		});
		return view;
	}

//	public void flipCard(View view) {
//		if(showingBack) {
//			showingBack = false;
//			getFragmentManager().beginTransaction().
//				setCustomAnimations(R.animator.card_flip_left_in, R.animator.card_flip_left_out).
//				replace(R.id.card_container, frontFragment).commit();
//		} else {
//			showingBack = true;
//			getFragmentManager().beginTransaction().
//				setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out).
//				replace(R.id.card_container, backFragment).commit();
//		}
//	}

}
