package com.kumquatcards.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.kumquatcards.R;

public class FlashCardFragment extends Fragment {
	public static final String TAG = "FlashCardFragment";

	public static final String ARG_DEFINITION = "definition";
	public static final String ARG_TRANSLATION = "translation";
	public static final String ARG_PINYIN = "pinyin";
	public static final String ARG_INDEX = "index";
	public static final String ARG_SHOW_FRONT = "show_front";

	private View cardContainer;
	private View cardFront;
	private View cardBack;
	private EditText inputText;

	private String definition;
	private String translation;
	private String pinyin;
	private int index;

	private boolean showingFront = true;

	public FlashCardFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		definition = getArguments().getString(ARG_DEFINITION);
		translation = getArguments().getString(ARG_TRANSLATION);
		pinyin = getArguments().getString(ARG_PINYIN);
		index = getArguments().getInt(ARG_INDEX);
		showingFront = getArguments().getBoolean(ARG_SHOW_FRONT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_flash_card, container, false);
		cardContainer = view.findViewById(R.id.card_container);
		cardFront = view.findViewById(R.id.card_front);
		TextView cardFrontText = (TextView) view.findViewById(R.id.card_front_text);
		cardFrontText.setText(definition);
		cardBack = view.findViewById(R.id.card_back);
		TextView cardBackTranslation = (TextView) view.findViewById(R.id.card_back_translation);
		cardBackTranslation.setText(translation);
		TextView cardBackPinyin = (TextView) view.findViewById(R.id.card_back_pinyin);
		cardBackPinyin.setText(pinyin);
		TextView cardBackDefinition = (TextView) view.findViewById(R.id.card_back_definition);
		cardBackDefinition.setText(definition);
		inputText = (EditText) view.findViewById(R.id.card_input);

		ImageButton flipFront = (ImageButton) view.findViewById(R.id.button_flip_front);
		flipFront.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				flipCard(v);
			}
		});
		ImageButton flipBack = (ImageButton) view.findViewById(R.id.button_flip_back);
		flipBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				flipCard(v);
			}
		});

		inputText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEND) {
					checkTranslation(v);
					return true;
				}
				return false;
			}
		});

		if(!showingFront) {
            cardFront.setVisibility(View.GONE);
            cardBack.setVisibility(View.VISIBLE);
            cardBack.requestFocus();
		}

		return view;
	}

	public void flipCard(View view) {
		if(showingFront) {
			showingFront = false;
			applyRotation(true, 0, 90);
		} else {
			showingFront = true;
			applyRotation(false, 0, -90);
		}
	}

	public void checkTranslation(View view) {
		String input = inputText.getText().toString();
		String translation = getArguments().getString(FlashCardFragment.ARG_TRANSLATION);

		boolean correct = input.equalsIgnoreCase(translation);

		flashBackground(correct);

		if(correct) {
			((FlashCardActivity) getActivity()).markCardCorrect(index);
		}
	}

	private void flashBackground(final boolean correct) {
		Integer colorFrom = getResources().getColor(R.color.card_border);
		int id = correct ? R.color.correct_answer : R.color.incorrect_answer;
		Integer colorTo = getResources().getColor(id);
		final int widthFrom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		final int widthTo = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());
		AnimatorUpdateListener strokeListener = new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				GradientDrawable d = (GradientDrawable) cardContainer.getBackground();
				Integer width = (Integer) animation.getAnimatedValue("width");
				Integer color = (Integer) animation.getAnimatedValue("color");
				d.setStroke(width, color);
				d.invalidateSelf();
			}
		};
		PropertyValuesHolder fadeInColor = PropertyValuesHolder.ofObject("color", new ArgbEvaluator(), colorFrom, colorTo);
		PropertyValuesHolder fadeInWidth = PropertyValuesHolder.ofInt("width", widthFrom, widthTo);
		PropertyValuesHolder fadeOutColor = PropertyValuesHolder.ofObject("color", new ArgbEvaluator(), colorTo, colorFrom);
		PropertyValuesHolder fadeOutWidth = PropertyValuesHolder.ofInt("width", widthTo, widthFrom);
		
		ValueAnimator fadeIn = ValueAnimator.ofPropertyValuesHolder(fadeInColor, fadeInWidth);
		fadeIn.setDuration(250);
		fadeIn.setInterpolator(new AccelerateInterpolator());
		fadeIn.addUpdateListener(strokeListener);

		ValueAnimator fadeOut = ValueAnimator.ofPropertyValuesHolder(fadeOutColor, fadeOutWidth);
		fadeOut.setDuration(250);
		fadeOut.setInterpolator(new DecelerateInterpolator());
		fadeOut.addUpdateListener(strokeListener);

		fadeOut.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if(correct) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(inputText.getWindowToken(), 0, new ResultReceiver(null) {
						@Override
						protected void onReceiveResult(int resultCode,
								Bundle resultData) {
							cardContainer.postDelayed(new Runnable() {
								@Override
								public void run() {
									inputText.setText("");
									flipCard(null);
								}
							}, 250);
						}
					});
				}
			}
		});

		AnimatorSet flash = new AnimatorSet();
		flash.playSequentially(fadeIn, fadeOut);
		flash.start();
	}

	/*
	 * The following methods were originally copied from com.example.android.apis.animation.Transition3d
	 * in the ApiDemos project and modified as necessary.
	 */

	/**
     * Setup a new 3D rotation on the container view.
     *
     * @param position the item that was clicked to show a picture, or -1 to show the list
     * @param start the start angle at which the rotation must begin
     * @param end the end angle of the rotation
     */
    private void applyRotation(boolean flipToBack, float start, float end) {
        // Find the center of the container
        final float centerX = cardContainer.getWidth() / 2.0f;
        final float centerY = cardContainer.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(start, end, centerX, centerY, 450.0f, true);
        rotation.setDuration(200);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(flipToBack));

        cardContainer.startAnimation(rotation);
    }

    /**
     * This class listens for the end of the first half of the animation.
     * It then posts a new action that effectively swaps the views when the container
     * is rotated 90 degrees and thus invisible.
     */
    private final class DisplayNextView implements Animation.AnimationListener {
        private final boolean flipToBack;

        private DisplayNextView(boolean flipToBack) {
            this.flipToBack = flipToBack;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            cardContainer.post(new SwapViews(flipToBack));
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * This class is responsible for swapping the views and start the second
     * half of the animation.
     */
    private final class SwapViews implements Runnable {
        private final boolean flipToBack;

        public SwapViews(boolean flipToBack) {
            this.flipToBack = flipToBack;
        }

        public void run() {
            final float centerX = cardContainer.getWidth() / 2.0f;
            final float centerY = cardContainer.getHeight() / 2.0f;
            Rotate3dAnimation rotation;

            if (flipToBack) {
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.VISIBLE);
                cardBack.requestFocus();

                rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 450.0f, false);
            } else {
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.requestFocus();

                rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 450.0f, false);
            }

            rotation.setDuration(200);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            cardContainer.startAnimation(rotation);
        }
    }
}
