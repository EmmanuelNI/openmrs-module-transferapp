package org.openmrs.module.transferapp.api;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.transferapp.TransferAppConstants;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class PendingTransferPatientStatusResolverTest {

	@Test
	public void shouldAddStatusUsingExactUpidMatchesAndCacheDuplicateUpids() {
		PatientServiceHandler patientServiceHandler = new PatientServiceHandler("existing-upid");
		List<Map<String, Object>> transfers = Arrays.asList(
				transfer(" existing-upid ", null),
				transfer(null, "existing-upid"),
				transfer("new-upid", null),
				transfer(null, "new-upid"),
				transfer(" ", null));

		List<Map<String, Object>> resolved = PendingTransferPatientStatusResolver.addPatientStatus(
				transfers, patientServiceHandler.createProxy());

		assertTrue(existingPatient(resolved.get(0)));
		assertTrue(existingPatient(resolved.get(1)));
		assertFalse(existingPatient(resolved.get(2)));
		assertFalse(existingPatient(resolved.get(3)));
		assertFalse(existingPatient(resolved.get(4)));
		assertEquals("existing-patient-uuid", patientReference(resolved.get(0)));
		assertEquals("existing-patient-uuid", patientReference(resolved.get(1)));
		assertEquals("", patientReference(resolved.get(2)));
		assertEquals("", patientReference(resolved.get(3)));
		assertEquals(Arrays.asList("existing-upid", "new-upid"), patientServiceHandler.lookedUpIdentifiers);
		assertEquals(2, patientServiceHandler.matchExactlyValues.size());
		assertTrue(patientServiceHandler.matchExactlyValues.get(0));
		assertTrue(patientServiceHandler.matchExactlyValues.get(1));
		assertNotSame(transfers.get(0), resolved.get(0));
		assertFalse(transfers.get(0).containsKey(TransferAppConstants.PENDING_TRANSFER_EXISTING_PATIENT_KEY));
	}

	private static Map<String, Object> transfer(String upid, String subject) {
		Map<String, Object> transfer = new HashMap<String, Object>();
		transfer.put("upid", upid);
		transfer.put("subject", subject);
		return transfer;
	}

	private static boolean existingPatient(Map<String, Object> transfer) {
		return Boolean.TRUE.equals(transfer.get(TransferAppConstants.PENDING_TRANSFER_EXISTING_PATIENT_KEY));
	}

	private static String patientReference(Map<String, Object> transfer) {
		return String.valueOf(transfer.get(TransferAppConstants.PENDING_TRANSFER_PATIENT_REFERENCE_KEY));
	}

	private static class PatientServiceHandler implements InvocationHandler {

		private final String existingUpid;
		private final PatientIdentifierType upidType = new PatientIdentifierType();
		private final List<String> lookedUpIdentifiers = new ArrayList<String>();
		private final List<Boolean> matchExactlyValues = new ArrayList<Boolean>();

		private PatientServiceHandler(String existingUpid) {
			this.existingUpid = existingUpid;
			upidType.setName(TransferAppConstants.UPID_IDENTIFIER_TYPE_NAME);
		}

		private PatientService createProxy() {
			return (PatientService) Proxy.newProxyInstance(
					PatientService.class.getClassLoader(), new Class<?>[] { PatientService.class }, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("getPatientIdentifierTypeByName".equals(method.getName())) {
				return upidType;
			}
			if ("getPatients".equals(method.getName()) && args != null && args.length == 4) {
				String identifier = (String) args[1];
				lookedUpIdentifiers.add(identifier);
				matchExactlyValues.add((Boolean) args[3]);
				@SuppressWarnings("unchecked")
				List<PatientIdentifierType> identifierTypes = (List<PatientIdentifierType>) args[2];
				assertEquals(Collections.singletonList(upidType), identifierTypes);
				if (existingUpid.equals(identifier)) {
					Patient patient = new Patient();
					patient.setUuid("existing-patient-uuid");
					return Collections.singletonList(patient);
				}
				return Collections.emptyList();
			}
			return null;
		}
	}
}
