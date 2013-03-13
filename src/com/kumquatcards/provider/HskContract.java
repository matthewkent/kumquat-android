package com.kumquatcards.provider;

import android.net.Uri;

public final class HskContract {
	public static final String AUTHORITY = "com.kumquatcards.provider.HskProvider";

	private HskContract(){}

	public static final class FlashCards {
		public static final String CONTENT_TYPE = "vnd.kumquatcards.hsk/vnd.kumquatcards.flashcards";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/flashcards");

		public static final String TABLE_NAME = "translations";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_SIMPLIFIED = "simplified";
		public static final String COLUMN_NAME_PINYIN = "pinyin";
		public static final String COLUMN_NAME_DEFINITION = "definition";

		public static Uri buildFlashCardUri(int hskLevel, int orderNumber) {
			return CONTENT_URI.buildUpon().appendPath(String.valueOf(hskLevel)).appendPath(String.valueOf(orderNumber)).build();
		}
	}

	public static final class HskLists {
		public static final String CONTENT_TYPE = "vnd.kumquatcards.hsk/vnd.kumquatcards.hsk_list";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/hsk_lists");

		public static final String TABLE_NAME = "hsk_lists";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_TRANSLATION_ID = "translation_id";
		public static final String COLUMN_NAME_LEVEL_NUMBER = "level_number";
		public static final String COLUMN_NAME_ORDER_NUMBER = "order_number";
	}
}
