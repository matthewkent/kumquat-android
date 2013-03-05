package com.kumquatcards;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String EXTRA_MESSAGE = "com.kumquatcards.message";
	private Map<String, String> translations = new HashMap<String, String>() {{
		put("one", "uno");
	}};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView card = (TextView) findViewById(R.id.display_card);
		card.setText("one");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean translate(String card, String input) {
		return translations.get(card).equalsIgnoreCase(input);
	}

	public void checkTranslation(View view) {
		TextView cardText = (TextView) findViewById(R.id.display_card);
		String card = cardText.getText().toString();
		EditText translationText = (EditText) findViewById(R.id.translate_card);
		String translation = translationText.getText().toString();
		
		String message = translate(card, translation) ? "correct!" : "wrong :(";
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}
}
