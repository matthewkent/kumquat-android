package com.kumquatcards.provider.test;

import junit.framework.Assert;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.kumquatcards.provider.HskContract;
import com.kumquatcards.provider.HskProvider;

public class HskProviderTest extends ProviderTestCase2<HskProvider> {

	private MockContentResolver mockResolver;

	public HskProviderTest() {
		super(HskProvider.class, HskContract.AUTHORITY);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mockResolver = getMockContentResolver();
	}

	public void testQueryFlashCard() {
		Cursor c;

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(1), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_1_MAX, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(2), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_2_MAX, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(3), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_3_MAX, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(4), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_4_MAX, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(5), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_5_MAX, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(6), null, null, null, null);
		assertEquals(HskContract.FlashCards.LEVEL_6_MAX, c.getCount());
	}

	public void testQueryInvalidFlashCardId() {
		Cursor c;

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(0), null, null, null, null);
		assertEquals(0, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(7), null, null, null, null);
		assertEquals(0, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardsUri(123), null, null, null, null);
		assertEquals(0, c.getCount());
	}

	public void testQueryInvalidFlashCardUri() {
		try {
			mockResolver.query(Uri.parse("content://" + HskContract.AUTHORITY + "/garbage"), null, null, null, null);
			Assert.fail("Expected IllegalArgumentException but none was thrown");
		} catch(IllegalArgumentException e) {
			// success
		}
	}

	public void testInsertAndQueryScores() {
		Uri uri = HskContract.Scores.buildScoresUri(1);
		ContentValues values = new ContentValues();
		values.put(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, 1);
		values.put(HskContract.Scores.COLUMN_NAME_SCORE_DATA, "scoredata");
		values.put(HskContract.Scores.COLUMN_NAME_CURRENT_CARD, 123);
		mockResolver.update(uri, values, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = 1", null);
		Cursor c;
		c = mockResolver.query(uri, null, null, null, null);
		assertEquals(1, c.getCount());
	}

	public void testUpdateAndQueryScores() {
		Uri uri = HskContract.Scores.buildScoresUri(1);
		ContentValues values = new ContentValues();
		values.put(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER, 1);
		values.put(HskContract.Scores.COLUMN_NAME_SCORE_DATA, "scoredata");
		values.put(HskContract.Scores.COLUMN_NAME_CURRENT_CARD, 123);
		mockResolver.update(uri, values, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = 1", null);

		values.put(HskContract.Scores.COLUMN_NAME_SCORE_DATA, "newdata");
		mockResolver.update(uri, values, HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER + " = 1", null);
		Cursor c;
		c = mockResolver.query(uri, null, null, null, null);
		assertEquals(1, c.getCount());
		c.moveToFirst();
		assertEquals("newdata", c.getString(c.getColumnIndex(HskContract.Scores.COLUMN_NAME_SCORE_DATA)));
	}

	public void testUpdateInvalidScoreUri() {
		try {
			mockResolver.update(Uri.parse("content://" + HskContract.AUTHORITY + "/garbage"), null, null, null);
			Assert.fail("Expected IllegalArgumentException but none was thrown");
		} catch(IllegalArgumentException e) {
			// success
		}
	}

	public void testQueryAllScores() {
		Cursor c;
		c = mockResolver.query(HskContract.Scores.buildAllScoresUri(), null, null, null, null);
		assertEquals(6, c.getCount());
		c.moveToFirst();
		assertEquals(1, c.getInt(c.getColumnIndex(HskContract.Scores.COLUMN_NAME_LEVEL_NUMBER)));
		assertEquals(0, c.getInt(c.getColumnIndex(HskContract.Scores.COLUMN_NAME_CURRENT_CARD)));
		assertNull(c.getString(c.getColumnIndex(HskContract.Scores.COLUMN_NAME_SCORE_DATA)));
	}
}
