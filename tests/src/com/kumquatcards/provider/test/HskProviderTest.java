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

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(1, 1), null, null, null, null);
		assertEquals(1, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(2, 3), null, null, null, null);
		assertEquals(1, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(3, 250), null, null, null, null);
		assertEquals(1, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(4, 999), null, null, null, null);
		assertEquals(1, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(5, 1234), null, null, null, null);
		assertEquals(1, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(6, 4567), null, null, null, null);
		assertEquals(1, c.getCount());
	}

	public void testQueryInvalidFlashCardId() {
		Cursor c;

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(1, 1234), null, null, null, null);
		assertEquals(0, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(7, 1), null, null, null, null);
		assertEquals(0, c.getCount());

		c = mockResolver.query(HskContract.FlashCards.buildFlashCardUri(123, 456), null, null, null, null);
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
