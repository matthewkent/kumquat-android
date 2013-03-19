package com.kumquatcards.provider.test;

import junit.framework.Assert;
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
}
