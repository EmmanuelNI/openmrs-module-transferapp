/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.transferapp.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransferFormKindTest {

	@Test
	public void fromCodeOrLabelResolvesExternalAliases() {
		assertEquals(TransferFormKind.GENERAL, TransferFormKind.fromCodeOrLabel("external"));
		assertEquals(TransferFormKind.GENERAL, TransferFormKind.fromCodeOrLabel("GENERAL"));
		assertEquals(TransferFormKind.GENERAL, TransferFormKind.fromCodeOrLabel("External transfer form"));
		assertEquals(TransferFormKind.GENERAL, TransferFormKind.fromCodeOrLabel(null));
	}

	@Test
	public void fromCodeOrLabelResolvesMaternityAndNeonatal() {
		assertEquals(TransferFormKind.MATERNITY, TransferFormKind.fromCodeOrLabel("maternity"));
		assertEquals(TransferFormKind.NEONATAL, TransferFormKind.fromCodeOrLabel("neonatal"));
	}

	@Test
	public void externalFormHasExpectedFhirCodeAndDisplay() {
		assertEquals("external", TransferFormKind.GENERAL.getCode());
		assertTrue(TransferFormKind.GENERAL.getDisplay().toLowerCase().contains("external"));
		assertTrue(TransferFormKind.GENERAL.isExternalTransferForm());
	}
}
