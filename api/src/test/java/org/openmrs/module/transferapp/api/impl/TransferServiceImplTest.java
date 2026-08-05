package org.openmrs.module.transferapp.api.impl;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.rwandaemr.queue.QueueService;
import org.openmrs.module.rwandaemr.queue.model.QueueEntry;
import org.openmrs.module.transferapp.model.Transfer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TransferServiceImplTest {

	@Test
	public void shouldMarkActiveQueueEntryTransferredWithTransferUuid() {
		Patient patient = new Patient();
		QueueEntry queueEntry = new QueueEntry();
		Transfer transfer = new Transfer();
		transfer.setUuid("transfer-uuid");
		Date transferDate = new Date();
		QueueServiceHandler queueHandler = new QueueServiceHandler(queueEntry);
		TestTransferService transferService = new TestTransferService();
		transferService.setQueueService(queueHandler.createProxy());

		transferService.markQueueEntryTransferred(patient, transfer, transferDate);

		assertTrue(queueHandler.markTransferredCalled);
		assertSame(patient, queueHandler.patient);
		assertSame(transferDate, queueHandler.date);
		assertSame(queueEntry, queueHandler.queueEntry);
		assertEquals("External transfer created: transfer-uuid", queueHandler.reason);
	}

	@Test
	public void shouldIgnorePatientWithoutActiveQueueEntry() {
		Patient patient = new Patient();
		Transfer transfer = new Transfer();
		transfer.setUuid("transfer-uuid");
		QueueServiceHandler queueHandler = new QueueServiceHandler(null);
		TestTransferService transferService = new TestTransferService();
		transferService.setQueueService(queueHandler.createProxy());

		transferService.markQueueEntryTransferred(patient, transfer, new Date());

		assertFalse(queueHandler.markTransferredCalled);
	}

	private static class TestTransferService extends TransferServiceImpl {

		private void markQueueEntryTransferred(Patient patient, Transfer transfer, Date transferDate) {
			markActiveQueueEntryTransferred(patient, transfer, transferDate);
		}
	}

	private static class QueueServiceHandler implements InvocationHandler {

		private final QueueEntry activeQueueEntry;
		private Patient patient;
		private Date date;
		private QueueEntry queueEntry;
		private String reason;
		private boolean markTransferredCalled;

		private QueueServiceHandler(QueueEntry activeQueueEntry) {
			this.activeQueueEntry = activeQueueEntry;
		}

		private QueueService createProxy() {
			return (QueueService) Proxy.newProxyInstance(
					QueueService.class.getClassLoader(),
					new Class<?>[] { QueueService.class },
					this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			if ("getActiveQueueEntry".equals(method.getName()) && args.length == 2) {
				patient = (Patient) args[0];
				date = (Date) args[1];
				return activeQueueEntry;
			}
			if ("markPatientTransferred".equals(method.getName())) {
				markTransferredCalled = true;
				queueEntry = (QueueEntry) args[0];
				reason = (String) args[1];
				return queueEntry;
			}
			return null;
		}
	}
}
