package com.kumquatcards.provider;

import android.net.Uri;

public final class HskContract {
	public static final String AUTHORITY = "com.kumquatcards.provider.HskProvider";

	private HskContract(){}

	public static final class FlashCards {
		public static final String CONTENT_TYPE = "vnd.kumquatcards.hsk/vnd.kumquatcards.flashcards";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/flashcards");

		public static final int LEVEL_1_MAX = 153;
		public static final int LEVEL_2_MAX = 303;
		public static final int LEVEL_3_MAX = 603;
		public static final int LEVEL_4_MAX = 1203;
		public static final int LEVEL_5_MAX = 2503;
		public static final int LEVEL_6_MAX = 5013;

		public static int maxOrderForLevel(int level) {
			switch(level) {
			case 1:
				return LEVEL_1_MAX;
			case 2:
				return LEVEL_2_MAX;
			case 3:
				return LEVEL_3_MAX;
			case 4:
				return LEVEL_4_MAX;
			case 5:
				return LEVEL_5_MAX;
			case 6:
				return LEVEL_6_MAX;
			default:
				return 0;
			}
		}
		public static final String TABLE_NAME = "translations";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_SIMPLIFIED = "simplified";
		public static final String COLUMN_NAME_PINYIN = "pinyin";
		public static final String COLUMN_NAME_DEFINITION = "definition";

		public static Uri buildFlashCardsUri(int hskLevel) {
			return CONTENT_URI.buildUpon().appendPath(String.valueOf(hskLevel)).build();
		}
	}

	public static final class Scores {
		public static final String CONTENT_TYPE = "vnd.kumquatcards.hsk/vnd.kumquatcards.score";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/scores");

		public static final String TABLE_NAME = "scores";
		public static final String COLUMN_NAME_ID = "_id";
		public static final String COLUMN_NAME_LEVEL_NUMBER = "level_number";
		public static final String COLUMN_NAME_SCORE_DATA = "score_data";
		public static final String COLUMN_NAME_CURRENT_CARD = "current_card";

		public static Uri buildScoresUri(int hskLevel) {
			return CONTENT_URI.buildUpon().appendPath(String.valueOf(hskLevel)).build();
		}

		public static Uri buildAllScoresUri() {
			return CONTENT_URI;
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
