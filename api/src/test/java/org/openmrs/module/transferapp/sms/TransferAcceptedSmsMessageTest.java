/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.transferapp.sms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransferAcceptedSmsMessageTest {

	@Test
	public void buildMatchesEtransferTemplate() {
		String message = TransferAcceptedSmsMessage.build(
				"Patience Ruberandinda",
				"Muhima District Hospital",
				"270114-0334-1150");
		assertEquals(
				"Patience Ruberandinda, Mwahawe transferi ijya ku Muhima District Hospital. nimero yawe ni 270114-0334-1150",
				message);
	}

	@Test
	public void normalizeRwandaMobileFormats() {
		assertEquals("250783095523", RwandaPhoneNumberNormalizer.normalize("250783095523"));
		assertEquals("250783095523", RwandaPhoneNumberNormalizer.normalize("0783095523"));
		assertEquals("250783095523", RwandaPhoneNumberNormalizer.normalize("783095523"));
		assertTrue(RwandaPhoneNumberNormalizer.looksLikeRwandaMobile("0783095523"));
		assertFalse(RwandaPhoneNumberNormalizer.looksLikeRwandaMobile("N/A"));
	}

	@Test
	public void parseSuccessBodyWithQuotedUuid() {
		String messageId = IntouchSmsResponseParser.parseMessageId(
				200, "Success \"c2e0d7c3-3217-431b-81d2-6fcf1368d56b\"");
		assertEquals("c2e0d7c3-3217-431b-81d2-6fcf1368d56b", messageId);
	}
}
