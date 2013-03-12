package com.kumquatcards.test;

import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.kumquatcards.HskContract;
import com.kumquatcards.HskProvider;

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

	public void testQueryTranslations() {
		Cursor c = mockResolver.query(HskContract.Translations.CONTENT_URI, null, null, null, null);
		assertEquals(1, c.getCount());
	}
}
