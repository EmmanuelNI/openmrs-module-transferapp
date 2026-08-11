(function(global) {
	"use strict";

	function escTransferPreview(value) {
		if (value === null || value === undefined) {
			return "";
		}
		return String(value)
			.replace(/&/g, "&amp;")
			.replace(/</g, "&lt;")
			.replace(/>/g, "&gt;");
	}

	function yesNoCircle(flag) {
		return flag ? "&#9679;" : "&#9711;";
	}

	function line(value, minWidth) {
		var safe = escTransferPreview(value || "");
		var width = minWidth || 180;
		return "<span class='tf-line' style='min-width:" + width + "px;'>" + safe + "</span>";
	}

	function truthy(value) {
		return value === true || value === "true";
	}

	function isValidVerificationUuid(value) {
		if (value === null || value === undefined) {
			return false;
		}
		var normalized = String(value).trim();
		if (normalized.charAt(0) === "{" && normalized.charAt(normalized.length - 1) === "}") {
			normalized = normalized.substring(1, normalized.length - 1).trim();
		}
		return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(normalized);
	}

	function resolveVerificationTransferId(normalized) {
		var candidates = [
			normalized.verificationTransferId,
			normalized.hieTransferId,
			normalized.uuid,
			normalized.id
		];
		for (var i = 0; i < candidates.length; i++) {
			if (isValidVerificationUuid(candidates[i])) {
				return String(candidates[i]).trim();
			}
		}
		return "";
	}

	function buildVerifyQrFormUrl(transferId) {
		if (!isValidVerificationUuid(transferId)) {
			return "";
		}
		return "/module/transferapp/transfer/verifyQr.form?transferId="
			+ encodeURIComponent(String(transferId).trim());
	}

	function normalizeTransferPreviewItem(item) {
		var normalized = item || {};
		var transferType = normalized.transferType || "";
		var verificationTransferId = resolveVerificationTransferId(normalized);
		var showVerificationQr = truthy(normalized.showVerificationQr);
		if (!showVerificationQr && isValidVerificationUuid(verificationTransferId)) {
			showVerificationQr = true;
		}
		var verifyQrUrl = normalized.verifyQrUrl;
		if (showVerificationQr && !verifyQrUrl && verificationTransferId) {
			verifyQrUrl = buildVerifyQrFormUrl(verificationTransferId);
		}
		var verifyRemoteUrl = normalized.verifyRemoteUrl;
		if (showVerificationQr && !verifyRemoteUrl && verificationTransferId && global.transferVerifyBaseUrl) {
			verifyRemoteUrl = String(global.transferVerifyBaseUrl).replace(/\/+$/, "")
				+ "/verify/transfer/" + encodeURIComponent(verificationTransferId) + "/remote";
		}

		return {
			province: normalized.province,
			district: normalized.district || normalized.clientDistrict,
			hospitalName: normalized.hospitalName || normalized.sendingFacility,
			referringFacilityName: normalized.referringFacilityName || normalized.sendingFacility,
			referringUnit: normalized.referringUnit,
			receivingClinicianPhone: normalized.receivingClinicianPhone || normalized.staffContactedPhone,
			clientName: normalized.clientName,
			serialNumberEmr: normalized.serialNumberEmr || normalized.emrId,
			clientTelephone: normalized.clientTelephone,
			ageOrDob: normalized.ageOrDob || normalized.ageDob,
			sex: normalized.sex,
			caregiverName: normalized.caregiverName,
			caregiverTelephone: normalized.caregiverTelephone,
			clientDistrict: normalized.clientDistrict || normalized.district,
			sector: normalized.sector || normalized.patientSector,
			cell: normalized.cell || normalized.patientCell,
			village: normalized.village || normalized.patientVillage,
			admissionAt: normalized.admissionAt || normalized.admissionDatetime,
			decisionToTransferAt: normalized.decisionToTransferAt || normalized.transferDecisionDatetime,
			receivingFacility: normalized.receivingFacility,
			receivingService: normalized.receivingService,
			callingTime: normalized.callingTime,
			staffContactedName: normalized.staffContactedName || normalized.staffContactedAtReceivingFacility,
			staffContactedPhone: normalized.staffContactedPhone || normalized.staffContactPhone,
			isEmergency: normalized.isEmergency != null ? truthy(normalized.isEmergency) : transferType === "EMERGENCY",
			isNonEmergency: normalized.isNonEmergency != null ? truthy(normalized.isNonEmergency) : transferType === "NOT_EMERGENCY",
			isFollowUp: normalized.isFollowUp != null ? truthy(normalized.isFollowUp) : transferType === "FOLLOW_UP",
			ambulanceCalledTime: normalized.ambulanceCalledTime,
			departureFromReferringTime: normalized.departureFromReferringTime || normalized.departureTime,
			reasonForTransfer: normalized.reasonForTransfer,
			clinicalPresentation: normalized.clinicalPresentation,
			disabilityType: normalized.disabilityType,
			vitalTemp: normalized.vitalTemp || normalized.temperature,
			vitalSpo2: normalized.vitalSpo2 || normalized.spo2,
			vitalRr: normalized.vitalRr || normalized.respiratoryRate,
			vitalPulse: normalized.vitalPulse || normalized.pulse,
			vitalBp: normalized.vitalBp || normalized.bloodPressure,
			vitalWeight: normalized.vitalWeight || normalized.weight,
			vitalHeight: normalized.vitalHeight || normalized.height,
			vitalMuac: normalized.vitalMuac || normalized.muac,
			laboratory: normalized.laboratory,
			othersNotes: normalized.othersNotes || normalized.others,
			diagnosis: normalized.diagnosis,
			proceduresAndTreatments: normalized.proceduresAndTreatments,
			isAmbulanceTransport: normalized.isAmbulanceTransport != null ? truthy(normalized.isAmbulanceTransport) : normalized.transportationType === "AMBULANCE",
			transportationOtherSpec: normalized.transportationOtherSpec || normalized.otherTransportType,
			isNaTransport: normalized.isNaTransport != null ? truthy(normalized.isNaTransport) : normalized.transportationType === "NA",
			isCbhiInsurance: normalized.isCbhiInsurance != null ? truthy(normalized.isCbhiInsurance) : normalized.healthInsuranceType === "CBHI",
			isRssbInsurance: normalized.isRssbInsurance != null ? truthy(normalized.isRssbInsurance) : normalized.healthInsuranceType === "RSSB",
			isMmiInsurance: normalized.isMmiInsurance != null ? truthy(normalized.isMmiInsurance) : normalized.healthInsuranceType === "MMI",
			otherInsurance: normalized.otherInsurance || normalized.healthInsuranceOtherSpec,
			isNoInsurance: normalized.isNoInsurance != null ? truthy(normalized.isNoInsurance) : normalized.healthInsuranceType === "NONE",
			referringProviderName: normalized.referringProviderName,
			referringProviderQualification: normalized.referringProviderQualification,
			referringSignedDate: normalized.referringSignedDate || normalized.formDate,
			referringSignedTime: normalized.referringSignedTime || normalized.formTime,
			referringProviderPhone: normalized.referringProviderPhone || normalized.providerPhone,
			signatureAndStamp: normalized.signatureAndStamp,
			showVerificationQr: showVerificationQr,
			verifyQrUrl: verifyQrUrl,
			verifyRemoteUrl: verifyRemoteUrl,
			verificationTransferId: verificationTransferId || normalized.verificationTransferId,
			hieTransferId: normalized.hieTransferId || verificationTransferId,
			uuid: normalized.uuid || normalized.id || verificationTransferId
		};
	}

	function resolveTransferPreviewQrUrl(verifyQrUrl) {
		if (!verifyQrUrl) {
			return "";
		}
		if (/^https?:\/\//i.test(verifyQrUrl)) {
			return verifyQrUrl;
		}
		var openmrsPath = global.transferOpenmrsPath || "";
		if (verifyQrUrl.charAt(0) === "/") {
			return openmrsPath + verifyQrUrl;
		}
		return openmrsPath + "/" + verifyQrUrl;
	}

	function buildTransferFormPreviewHtml(item) {
		var p = normalizeTransferPreviewItem(item);
		var logoUri = global.transferMohLogoDataUri || "";
		var logoHtml = logoUri
			? "<img class='tf-moh-logo' src='" + logoUri + "' alt='Ministry of Health' />"
			: "";
		var signatureBlock = line(p.signatureAndStamp, 260);
		var bottomRows = "<div class='tf-row'><strong>Laboratory:</strong> " + line(p.laboratory, 680) + "</div>"
			+ "<div class='tf-row'><strong>Others:</strong> " + line(p.othersNotes, 725) + "</div>"
			+ "<div class='tf-row'><strong>Diagnosis:</strong> " + line(p.diagnosis, 700) + "</div>"
			+ "<div class='tf-row'><strong>Procedures and Treatments:</strong> " + line(p.proceduresAndTreatments, 565) + "</div>"
			+ "<div class='tf-row'><strong>Type of Transportation:</strong>"
			+ " Ambulance:<span class='tf-circle'>" + yesNoCircle(p.isAmbulanceTransport) + "</span>"
			+ " Other (specify):" + line(p.transportationOtherSpec, 230)
			+ " NA:<span class='tf-circle'>" + yesNoCircle(p.isNaTransport) + "</span></div>"
			+ "<div class='tf-row'><strong>Health insurance:</strong>"
			+ " CBHI (mutuelle):<span class='tf-circle'>" + yesNoCircle(p.isCbhiInsurance) + "</span>"
			+ " RSSB:<span class='tf-circle'>" + yesNoCircle(p.isRssbInsurance) + "</span>"
			+ " MMI:<span class='tf-circle'>" + yesNoCircle(p.isMmiInsurance) + "</span>"
			+ " Other (Specify):" + line(p.otherInsurance, 120)
			+ " None:<span class='tf-circle'>" + yesNoCircle(p.isNoInsurance) + "</span></div>"
			+ "<div class='tf-row tf-bottom-gap'><strong>Names of referring health care provider:</strong> " + line(p.referringProviderName, 220)
			+ " <strong>Qualification:</strong> " + line(p.referringProviderQualification, 160) + "</div>"
			+ "<div class='tf-row tf-signature-row'><strong>Date:</strong> " + line(p.referringSignedDate, 120)
			+ " <strong>Time:</strong> " + line(p.referringSignedTime, 120)
			+ " <strong>Phone:</strong> " + line(p.referringProviderPhone, 180)
			+ signatureBlock + "</div>";

		var bottomSection = bottomRows;
		if (p.showVerificationQr && p.verifyQrUrl) {
			bottomSection = "<table class='tf-bottom-table' cellpadding='0' cellspacing='0'>"
				+ "<tr>"
				+ "<td class='tf-bottom-fields'>" + bottomRows + "</td>"
				+ "<td class='tf-qr-cell'>"
				+ "<img class='tf-qr-image' src='" + escTransferPreview(resolveTransferPreviewQrUrl(p.verifyQrUrl))
				+ "' alt='Scan to verify this transfer'"
				+ (p.verifyRemoteUrl ? " title='" + escTransferPreview(p.verifyRemoteUrl) + "'" : "")
				+ "/>"
				+ "</td>"
				+ "</tr>"
				+ "</table>";
		}

		return "<div class='transfer-form-preview'><div class='tf-sheet'>"
			+ "<div class='tf-head'>"
			+ "<div class='tf-left'>"
			+ "<div class='tf-row'><strong>REPUBLIC OF RWANDA</strong></div>"
			+ "<div class='tf-row'>" + logoHtml + "</div>"
			+ "<div class='tf-row' style='margin-top: 22px;'><strong>MINISTRY OF HEALTH</strong></div>"
			+ "</div>"
			+ "<div class='tf-right'>"
			+ "<div class='tf-row'><strong>Province:</strong>" + line(p.province, 320) + "</div>"
			+ "<div class='tf-row'><strong>District:</strong>" + line(p.district, 332) + "</div>"
			+ "<div class='tf-row'><strong>Name of Hospital:</strong>" + line(p.receivingFacility, 230) + "</div>"
			+ "<div class='tf-row'><strong>Name of Referring Facility:</strong>" + line(p.referringFacilityName, 172) + "</div>"
			+ "<div class='tf-row'><strong>Referring Unit:</strong>" + line(p.referringUnit, 280) + "</div>"
			+ "<div class='tf-row'><strong>Receiving Clinician/Phone:</strong>" + line(p.receivingClinicianPhone, 190) + "</div>"
			+ "</div>"
			+ "</div>"

			+ "<div class='tf-title'>EXTERNAL TRANSFER FORM</div>"

			+ "<div class='tf-row'><strong>Client Name:</strong> " + line(p.clientName, 280)
			+ " <strong>EMR ID:</strong> " + line(p.serialNumberEmr, 220)
			+ " <strong>Telephone:</strong> " + line(p.clientTelephone, 180) + "</div>"
			+ "<div class='tf-row'><strong>Age(DOB):</strong> " + line(p.ageOrDob, 150)
			+ " <strong>Sex:</strong> " + line(p.sex, 90)
			+ " <strong>Name of caregiver:</strong> " + line(p.referringProviderName, 270)
			+ " <strong>Caregiver telephone:</strong> " + line(p.caregiverTelephone, 180) + "</div>"
			+ "<div class='tf-row'><strong>District:</strong> " + line(p.clientDistrict, 200)
			+ " <strong>Sector:</strong> " + line(p.sector, 170)
			+ " <strong>Cell:</strong> " + line(p.cell, 210)
			+ " <strong>Village:</strong> " + line(p.village, 190) + "</div>"
			+ "<div class='tf-row'><strong>Date and time of Admission:</strong> " + line(p.admissionAt, 280)
			+ " <strong>Date and Time of decision to transfer:</strong> " + line(p.decisionToTransferAt, 240) + "</div>"
			+ "<div class='tf-row'><strong>Receiving Facility:</strong> " + line(p.receivingFacility, 240)
			+ " <strong>Receiving Service:</strong> " + line(p.receivingService, 250)
			+ " <strong>Calling Time:</strong> " + line(p.callingTime, 140) + "</div>"
			+ "<div class='tf-row'><strong>Staff contacted at receiving facility:</strong> " + line(p.staffContactedName, 320)
			+ " <strong>Phone:</strong> " + line(p.staffContactedPhone, 260) + "</div>"

			+ "<div class='tf-row'><strong>Type of transfer:</strong>"
			+ " Emergency:<span class='tf-circle'>" + yesNoCircle(p.isEmergency) + "</span>"
			+ " Not- Emergency:<span class='tf-circle'>" + yesNoCircle(p.isNonEmergency) + "</span>"
			+ " Follow up:<span class='tf-circle'>" + yesNoCircle(p.isFollowUp) + "</span></div>"
			+ "<div class='tf-row'><strong>If emergency:</strong> Time ambulance called: " + line(p.ambulanceCalledTime, 200)
			+ " Time of departure from referring facility: " + line(p.departureFromReferringTime, 250) + "</div>"
			+ "<div class='tf-row'><strong>Reason for Transfer:</strong> " + line(p.reasonForTransfer, 830) + "</div>"

			+ "<div class='tf-section-title'>Significant Findings:</div>"
			+ "<div class='tf-row'><strong>Clinical Presentation:</strong> " + line(p.clinicalPresentation, 780) + "</div>"
			+ "<div class='tf-lines-block'></div><div class='tf-lines-block'></div><div class='tf-lines-block'></div>"
			+ "<div class='tf-row'><strong>If person with disability, record the type of disability:</strong> " + line(p.disabilityType, 470) + "</div>"

			+ "<div class='tf-row'><strong>Vital Signs:</strong>"
			+ " T&#176;:" + line(p.vitalTemp, 70)
			+ " SpO<sub>2</sub>:" + line(p.vitalSpo2, 70)
			+ " RR:" + line(p.vitalRr, 70)
			+ " Pulse:" + line(p.vitalPulse, 70)
			+ " BP:" + line(p.vitalBp, 90)
			+ " Weight:" + line(p.vitalWeight, 80)
			+ " Height:" + line(p.vitalHeight, 80)
			+ " MUAC:" + line(p.vitalMuac, 80) + "</div>"
			+ bottomSection

			+ "</div></div>";
	}

	function yesNo(flag) {
		return flag ? "Yes" : "No";
	}

	function buildMaternityTreatmentTableHtml(treatments) {
		if (!treatments || !treatments.length) {
			return "<div class='tf-row'><em>No treatments recorded.</em></div>";
		}
		var rows = "";
		for (var i = 0; i < treatments.length; i++) {
			var t = treatments[i] || {};
			rows += "<tr>"
				+ "<td style='padding:3px 8px;border:1px solid #ccc;'>" + escTransferPreview(t.treatmentName) + "</td>"
				+ "<td style='padding:3px 8px;border:1px solid #ccc;'>" + escTransferPreview(t.dose) + "</td>"
				+ "<td style='padding:3px 8px;border:1px solid #ccc;'>" + escTransferPreview(t.givenDate) + "</td>"
				+ "<td style='padding:3px 8px;border:1px solid #ccc;'>" + escTransferPreview(t.givenTime) + "</td>"
				+ "</tr>";
		}
		return "<table style='border-collapse:collapse;width:100%;margin:6px 0;font-size:12px;'>"
			+ "<thead><tr>"
			+ "<th style='padding:3px 8px;border:1px solid #ccc;text-align:left;'>Treatment</th>"
			+ "<th style='padding:3px 8px;border:1px solid #ccc;text-align:left;'>Dose</th>"
			+ "<th style='padding:3px 8px;border:1px solid #ccc;text-align:left;'>Date</th>"
			+ "<th style='padding:3px 8px;border:1px solid #ccc;text-align:left;'>Time</th>"
			+ "</tr></thead><tbody>" + rows + "</tbody></table>";
	}

	/**
	 * Builds the print/preview HTML for a Maternity/ANC-Delivery-PNC transfer, the sibling
	 * of buildTransferFormPreviewHtml() for the External Transfer form. Field names match
	 * TransferSaveController#toMaternityPreviewMap.
	 */
	function buildMaternityTransferFormPreviewHtml(item) {
		var p = item || {};
		var logoUri = global.transferMohLogoDataUri || "";
		var logoHtml = logoUri
			? "<img class='tf-moh-logo' src='" + logoUri + "' alt='Ministry of Health' />"
			: "";

		var treatmentsHtml = buildMaternityTreatmentTableHtml(p.treatments);

		return "<div class='transfer-form-preview'><div class='tf-sheet'>"
			+ "<div class='tf-head'>"
			+ "<div class='tf-left'>"
			+ "<div class='tf-row'><strong>REPUBLIC OF RWANDA</strong></div>"
			+ "<div class='tf-row'>" + logoHtml + "</div>"
			+ "<div class='tf-row' style='margin-top: 22px;'><strong>MINISTRY OF HEALTH</strong></div>"
			+ "</div>"
			+ "<div class='tf-right'>"
			+ "<div class='tf-row'><strong>Name of Hospital:</strong>" + line(p.hospitalName, 230) + "</div>"
			+ "<div class='tf-row'><strong>Name of Referring Facility:</strong>" + line(p.referringFacilityName, 172) + "</div>"
			+ "</div>"
			+ "</div>"

			+ "<div class='tf-title'>MATERNITY TRANSFER FORM</div>"
			+ "<div class='tf-section-title'>ANC / Delivery / PNC Transfer</div>"

			+ "<div class='tf-row'><strong>Client Name:</strong> " + line(p.clientName, 280)
			+ " <strong>Serial No. / EMR ID:</strong> " + line(p.serialNumberEmr, 220)
			+ " <strong>Age(DOB):</strong> " + line(p.ageOrDob, 150) + "</div>"
			+ "<div class='tf-row'><strong>Next of kin:</strong> " + line(p.nextOfKinName, 250)
			+ " <strong>Telephone:</strong> " + line(p.nextOfKinTelephone, 180) + "</div>"
			+ "<div class='tf-row'><strong>District:</strong> " + line(p.clientDistrict, 200)
			+ " <strong>Sector:</strong> " + line(p.sector, 170)
			+ " <strong>Cell:</strong> " + line(p.cell, 210)
			+ " <strong>Village:</strong> " + line(p.village, 190) + "</div>"

			+ "<div class='tf-row'><strong>Date and time of Admission:</strong> " + line(p.admissionAt, 280)
			+ " <strong>Date and Time of decision to transfer:</strong> " + line(p.decisionToTransferAt, 240) + "</div>"
			+ "<div class='tf-row'><strong>Receiving Facility:</strong> " + line(p.receivingFacility, 240)
			+ " <strong>Receiving Service:</strong> " + line(p.receivingService, 250)
			+ " <strong>Calling Time:</strong> " + line(p.callingTime, 140) + "</div>"
			+ "<div class='tf-row'><strong>Staff contacted at receiving facility:</strong> " + line(p.staffContactedName, 320)
			+ " <strong>Phone:</strong> " + line(p.staffContactedPhone, 260) + "</div>"

			+ "<div class='tf-row'><strong>Type of transfer:</strong>"
			+ " Emergency:<span class='tf-circle'>" + yesNoCircle(truthy(p.isEmergency)) + "</span>"
			+ " Not-Emergency:<span class='tf-circle'>" + yesNoCircle(truthy(p.isNonEmergency)) + "</span>"
			+ " Follow up:<span class='tf-circle'>" + yesNoCircle(truthy(p.isFollowUp)) + "</span></div>"
			+ "<div class='tf-row'><strong>If emergency:</strong> Time ambulance called: " + line(p.ambulanceCalledTime, 200)
			+ " Time of departure from referring facility: " + line(p.departureFromReferringTime, 250) + "</div>"
			+ "<div class='tf-row'><strong>Reason for Transfer:</strong> " + line(p.reasonForTransfer, 830) + "</div>"
			+ "<div class='tf-row'><strong>Partograph attached:</strong> " + yesNo(truthy(p.partographAttached))
			+ " <strong>Disability type:</strong> " + line(p.disabilityType, 400) + "</div>"
			+ "<div class='tf-row'><strong>Clinical Presentation:</strong> " + line(p.clinicalPresentation, 780) + "</div>"

			+ "<div class='tf-section-title'>Obstetric History</div>"
			+ "<div class='tf-row'>"
			+ "<strong>Gravida:</strong>" + line(p.obstetricGravida, 60)
			+ " <strong>Parity:</strong>" + line(p.obstetricParity, 60)
			+ " <strong>Living children:</strong>" + line(p.obstetricLivingChildren, 60)
			+ " <strong>Abortion:</strong>" + line(p.obstetricAbortion, 60)
			+ " <strong>Stillbirth:</strong>" + line(p.obstetricStillbirth, 60)
			+ " <strong>Neonatal death:</strong>" + line(p.obstetricNeonatalDeath, 60)
			+ " <strong>Preterm birth:</strong>" + line(p.obstetricPretermBirth, 60) + "</div>"

			+ "<div class='tf-section-title'>Current Pregnancy</div>"
			+ "<div class='tf-row'><strong>LMP:</strong>" + line(p.lmpDate, 120)
			+ " <strong>EDD:</strong>" + line(p.eddDate, 120)
			+ " <strong>Gestation age:</strong>" + line(p.gestationAge, 120)
			+ " <strong>MUAC:</strong>" + line(p.muac, 90)
			+ " <strong>ANC visits:</strong>" + line(p.ancCompletedCount, 90)
			+ " <strong>Tetanus doses:</strong>" + line(p.tetanusVaccineDoses, 90) + "</div>"
			+ "<div class='tf-row'><strong>Previous significant history:</strong> " + line(p.previousSignificantHistory, 780) + "</div>"
			+ "<div class='tf-row'><strong>Current pregnancy complications:</strong> " + line(p.currentPregnancyComplications, 700) + "</div>"

			+ "<div class='tf-section-title'>Latest Results</div>"
			+ "<div class='tf-row'><strong>Hemoglobin:</strong>" + line(p.latestHemoglobin, 90)
			+ " <strong>HIV status:</strong>" + line(p.latestHivStatus, 90)
			+ " <strong>Blood group:</strong>" + line(p.latestBloodGroup, 90)
			+ " <strong>Other results:</strong>" + line(p.latestOtherResults, 400) + "</div>"

			+ "<div class='tf-row'><strong>Vital Signs:</strong>"
			+ " BP:" + line(p.vitalBp, 90)
			+ " T&#176;:" + line(p.vitalTemp, 70)
			+ " SpO<sub>2</sub>:" + line(p.vitalSpo2, 70)
			+ " RR:" + line(p.vitalRr, 70)
			+ " Pulse:" + line(p.vitalPulse, 70)
			+ " Weight:" + line(p.vitalWeight, 80)
			+ " Height:" + line(p.vitalHeight, 80) + "</div>"

			+ "<div class='tf-section-title'>Abdominal &amp; Vaginal Exam</div>"
			+ "<div class='tf-row'><strong>Fetal presentation:</strong>" + line(p.fetalPresentation, 150)
			+ " <strong>Fundal height:</strong>" + line(p.fundalHeight, 120)
			+ " <strong>Fetal heart rate:</strong>" + line(p.fetalHeartRate, 120)
			+ " <strong>Contractions:</strong>" + line(p.contractions, 150) + "</div>"
			+ "<div class='tf-row'><strong>Vaginal exam at:</strong>" + line(p.vaginalExamAt, 200)
			+ " <strong>Dilation:</strong>" + line(p.dilation, 70)
			+ " <strong>Effacement:</strong>" + line(p.effacement, 70)
			+ " <strong>Descent:</strong>" + line(p.descent, 70)
			+ " <strong>Consistency:</strong>" + line(p.consistency, 100)
			+ " <strong>Position:</strong>" + line(p.position, 100) + "</div>"
			+ "<div class='tf-row'>"
			+ "Caput:<span class='tf-circle'>" + yesNoCircle(truthy(p.caput)) + "</span>"
			+ " Moulding:<span class='tf-circle'>" + yesNoCircle(truthy(p.moulding)) + "</span>"
			+ " Membranes ruptured:<span class='tf-circle'>" + yesNoCircle(truthy(p.membranesRuptured)) + "</span>"
			+ " at " + line(p.membranesRupturedAt, 180)
			+ " Offensive:<span class='tf-circle'>" + yesNoCircle(truthy(p.offensive)) + "</span></div>"
			+ "<div class='tf-row'><strong>Amniotic fluid color:</strong>" + line(p.amnioticFluidColor, 150)
			+ " <strong>Estimated blood loss (mL):</strong>" + line(p.estimatedBloodLossMl, 120) + "</div>"

			+ "<div class='tf-section-title'>Investigations &amp; Diagnosis</div>"
			+ "<div class='tf-row'><strong>HGB:</strong>" + line(p.investigationHgb, 90)
			+ " <strong>Urine test:</strong>" + line(p.investigationUrineTest, 150)
			+ " <strong>Other test:</strong>" + line(p.investigationOtherTest, 150) + "</div>"
			+ "<div class='tf-row'><strong>Imaging investigations:</strong> " + line(p.imagingInvestigations, 700) + "</div>"
			+ "<div class='tf-row'><strong>Diagnosis:</strong> " + line(p.diagnosis, 700) + "</div>"
			+ "<div class='tf-row'><strong>Procedures:</strong> " + line(p.procedures, 700) + "</div>"
			+ "<div class='tf-row'>"
			+ "Lab tests attached:<span class='tf-circle'>" + yesNoCircle(truthy(p.attachedLabTests)) + "</span>"
			+ " Imaging attached:<span class='tf-circle'>" + yesNoCircle(truthy(p.attachedImaging)) + "</span>"
			+ " <strong>Other:</strong>" + line(p.attachedOther, 250) + "</div>"

			+ "<div class='tf-section-title'>Treatment Given</div>"
			+ treatmentsHtml

			+ "<div class='tf-row'><strong>Type of Transportation:</strong>"
			+ " Ambulance:<span class='tf-circle'>" + yesNoCircle(truthy(p.isAmbulanceTransport)) + "</span>"
			+ " Other (specify):" + line(p.transportationOtherSpec, 230)
			+ " NA:<span class='tf-circle'>" + yesNoCircle(truthy(p.isNaTransport)) + "</span></div>"
			+ "<div class='tf-row'><strong>Health insurance:</strong>"
			+ " CBHI (mutuelle):<span class='tf-circle'>" + yesNoCircle(truthy(p.isCbhiInsurance)) + "</span>"
			+ " RSSB:<span class='tf-circle'>" + yesNoCircle(truthy(p.isRssbInsurance)) + "</span>"
			+ " MMI:<span class='tf-circle'>" + yesNoCircle(truthy(p.isMmiInsurance)) + "</span>"
			+ " Other (Specify):" + line(p.healthInsuranceOtherSpec, 120)
			+ " None:<span class='tf-circle'>" + yesNoCircle(truthy(p.isNoInsurance)) + "</span></div>"

			+ "<div class='tf-row tf-bottom-gap'><strong>Names of referring health care provider:</strong> " + line(p.referringProviderName, 220)
			+ " <strong>Qualification:</strong> " + line(p.referringProviderQualification, 160) + "</div>"
			+ "<div class='tf-row tf-signature-row'><strong>Date:</strong> " + line(p.referringSignedDate, 120)
			+ " <strong>Time:</strong> " + line(p.referringSignedTime, 120)
			+ " <strong>Phone:</strong> " + line(p.referringProviderPhone, 180) + "</div>"

			+ "</div></div>";
	}

	/**
	 * Builds the print/preview HTML for a Neonatal transfer, the sibling of
	 * buildTransferFormPreviewHtml()/buildMaternityTransferFormPreviewHtml() for the
	 * Neonatal Transfer form. Field names match TransferSaveController#toNeonatalPreviewMap.
	 */
	function buildNeonatalTransferFormPreviewHtml(item) {
		var p = item || {};
		var logoUri = global.transferMohLogoDataUri || "";
		var logoHtml = logoUri
			? "<img class='tf-moh-logo' src='" + logoUri + "' alt='Ministry of Health' />"
			: "";

		return "<div class='transfer-form-preview'><div class='tf-sheet'>"
			+ "<div class='tf-head'>"
			+ "<div class='tf-left'>"
			+ "<div class='tf-row'><strong>REPUBLIC OF RWANDA</strong></div>"
			+ "<div class='tf-row'>" + logoHtml + "</div>"
			+ "<div class='tf-row' style='margin-top: 22px;'><strong>MINISTRY OF HEALTH</strong></div>"
			+ "</div>"
			+ "<div class='tf-right'>"
			+ "<div class='tf-row'><strong>Name of Hospital:</strong>" + line(p.hospitalName, 230) + "</div>"
			+ "<div class='tf-row'><strong>Name of Referring Facility:</strong>" + line(p.referringFacilityName, 172) + "</div>"
			+ "</div>"
			+ "</div>"

			+ "<div class='tf-title'>NEONATAL TRANSFER FORM</div>"

			+ "<div class='tf-section-title'>Baby &amp; Referral Info</div>"
			+ "<div class='tf-row'><strong>Baby Name:</strong> " + line(p.babyName, 260)
			+ " <strong>Sex:</strong> " + line(p.sex, 90)
			+ " <strong>DOB:</strong> " + line(p.dob, 150) + "</div>"
			+ "<div class='tf-row'><strong>Gestational age (wks):</strong>" + line(p.gestationalAgeWeeks, 90)
			+ " <strong>Birth weight (g):</strong>" + line(p.birthWeightG, 100)
			+ " <strong>Current weight (g):</strong>" + line(p.currentWeightG, 110)
			+ " <strong>Current age (days):</strong>" + line(p.currentAgeDays, 110) + "</div>"
			+ "<div class='tf-row'><strong>Mother Name:</strong> " + line(p.motherName, 260)
			+ " <strong>Mother Age:</strong> " + line(p.motherAge, 90)
			+ " <strong>Mother/Caregiver Phone:</strong> " + line(p.motherCaregiverPhone, 180) + "</div>"
			+ "<div class='tf-row'><strong>Place of Birth:</strong> " + line(p.placeOfBirth, 300) + "</div>"
			+ "<div class='tf-row'><strong>Receiving Facility:</strong> " + line(p.receivingFacility, 240)
			+ " <strong>Receiving Service:</strong> " + line(p.receivingService, 250)
			+ " <strong>Calling Time:</strong> " + line(p.callingTime, 140) + "</div>"
			+ "<div class='tf-row'><strong>Staff contacted at receiving facility:</strong> " + line(p.staffContactedName, 320)
			+ " <strong>Phone:</strong> " + line(p.staffContactedPhone, 260) + "</div>"
			+ "<div class='tf-row'><strong>Date and Time of decision to transfer:</strong> " + line(p.decisionToTransferAt, 280) + "</div>"
			+ "<div class='tf-row'><strong>Type of transfer:</strong>"
			+ " Emergency:<span class='tf-circle'>" + yesNoCircle(truthy(p.isEmergency)) + "</span>"
			+ " Not-Emergency:<span class='tf-circle'>" + yesNoCircle(truthy(p.isNonEmergency)) + "</span>"
			+ " Follow up:<span class='tf-circle'>" + yesNoCircle(truthy(p.isFollowUp)) + "</span></div>"
			+ "<div class='tf-row'><strong>Mode of transport:</strong>" + line(p.modeOfTransport, 150)
			+ " <strong>Other (specify):</strong>" + line(p.transportOther, 230) + "</div>"
			+ "<div class='tf-row'><strong>Reason for Transfer:</strong> " + line(p.reasonForTransfer, 830) + "</div>"

			+ "<div class='tf-section-title'>Maternal History</div>"
			+ "<div class='tf-row'><strong>Mother alive:</strong>" + line(p.motherAlive, 120)
			+ " <strong>Gravida:</strong>" + line(p.obstetricGravida, 60)
			+ " <strong>Parity:</strong>" + line(p.obstetricParity, 60)
			+ " <strong>Pregnancy type:</strong>" + line(p.pregnancyType, 120) + "</div>"
			+ "<div class='tf-row'><strong>ANC screening:</strong> " + line(p.ancScreening, 700) + "</div>"
			+ "<div class='tf-row'><strong>Pathologies during pregnancy:</strong> " + line(p.pathologiesDuringPregnancy, 700) + "</div>"
			+ "<div class='tf-row'><strong>Pregnancy treatment:</strong> " + line(p.pregnancyTreatment, 700) + "</div>"
			+ "<div class='tf-row'><strong>Blood group:</strong>" + line(p.bloodGroup, 90)
			+ " <strong>Rh factor:</strong>" + line(p.rhFactor, 90)
			+ " <strong>Tetanus doses:</strong>" + line(p.tetanusVaccineDoses, 90) + "</div>"
			+ "<div class='tf-row'><strong>HIV status:</strong>" + line(p.hivStatus, 100)
			+ " <strong>Regimen:</strong>" + line(p.hivRegimen, 150)
			+ " <strong>Recent VL:</strong>" + line(p.hivRecentVl, 100)
			+ " <strong>CD4 count:</strong>" + line(p.hivCd4Count, 100) + "</div>"
			+ "<div class='tf-row'><strong>Opportunistic infections:</strong> " + line(p.hivOpportunisticInfections, 700) + "</div>"
			+ "<div class='tf-row'><strong>Illicit drug history:</strong> " + line(p.maternalIllicitDrugHistory, 700) + "</div>"

			+ "<div class='tf-section-title'>Labor Details</div>"
			+ "<div class='tf-row'><strong>ROM at:</strong>" + line(p.romAt, 200)
			+ " <strong>AF quality:</strong>" + line(p.afQuality, 120)
			+ " <strong>AF quantity:</strong>" + line(p.afQuantity, 120)
			+ " <strong>Fever timing:</strong>" + line(p.feverTiming, 150) + "</div>"
			+ "<div class='tf-row'><strong>Steroid doses:</strong>" + line(p.steroidDoses, 100)
			+ " <strong>Last steroid dose at:</strong>" + line(p.lastSteroidDoseAt, 200)
			+ " <strong>MgSO4 at:</strong>" + line(p.mgso4At, 200) + "</div>"
			+ "<div class='tf-row'><strong>Mode of delivery:</strong>" + line(p.modeOfDelivery, 150)
			+ " <strong>Labor complications:</strong>" + line(p.laborComplications, 200)
			+ " <strong>Other:</strong>" + line(p.laborComplicationsOther, 200) + "</div>"
			+ "<div class='tf-row'><strong>Maternal anesthesia:</strong>" + line(p.maternalAnesthesia, 150)
			+ " <strong>Other:</strong>" + line(p.maternalAnesthesiaOther, 200) + "</div>"
			+ "<div class='tf-row'><strong>Maternal antibiotics:</strong> " + line(p.maternalAntibiotics, 700) + "</div>"
			+ "<div class='tf-row'><strong>Other drugs:</strong> " + line(p.otherDrugs, 700) + "</div>"
			+ "<div class='tf-row'><strong>Sepsis risk factors:</strong> " + line(p.sepsisRiskFactors, 700) + "</div>"

			+ "<div class='tf-section-title'>Neonatal History &amp; Drugs</div>"
			+ "<div class='tf-row'><strong>Resuscitation at birth:</strong>" + line(p.resuscitationAtBirth, 120)
			+ " <strong>APGAR 1/5/10 min:</strong>" + line(p.apgar1min, 50) + line(p.apgar5min, 50) + line(p.apgar10min, 50) + "</div>"
			+ "<div class='tf-row'><strong>Resuscitation methods:</strong> " + line(p.resuscitationMethods, 700) + "</div>"
			+ "<div class='tf-row'><strong>HIE:</strong>" + line(p.hie, 90)
			+ " <strong>HIE grade:</strong>" + line(p.hieGrade, 90)
			+ " <strong>Allergies:</strong>" + line(p.allergies, 250) + "</div>"
			+ "<div class='tf-row'><strong>Immunization:</strong>" + line(p.immunization, 90)
			+ " <strong>Vitamin K:</strong>" + line(p.vitaminK, 90)
			+ " <strong>Tetracycline eye ointment:</strong>" + line(p.tetracyclineEyeOintment, 90)
			+ " <strong>Surfactant:</strong>" + line(p.surfactant, 90) + "</div>"
			+ "<div class='tf-row'><strong>Immunization details:</strong> " + line(p.immunizationDetails, 700) + "</div>"

			+ "<div class='tf-section-title'>Chief Complaint &amp; Diagnoses</div>"
			+ "<div class='tf-row'><strong>Chief complaint:</strong> " + line(p.chiefComplaintDetails, 780) + "</div>"
			+ "<div class='tf-row'><strong>Clinical condition:</strong>"
			+ " SpO2 pre:" + line(p.spo2Preductal, 70)
			+ " SpO2 post:" + line(p.spo2Postductal, 70)
			+ " T&#176;:" + line(p.conditionTemp, 70)
			+ " HR:" + line(p.conditionHr, 70)
			+ " RR:" + line(p.conditionRr, 70)
			+ " BP:" + line(p.conditionBp, 90)
			+ " Neuro:" + line(p.neurologicalStatus, 120) + "</div>"
			+ "<div class='tf-row'>Seizures:<span class='tf-circle'>" + yesNoCircle(truthy(p.seizures)) + "</span></div>"
			+ "<div class='tf-row'><strong>Adverse events (24h):</strong> " + line(p.adverseEvents24h, 700) + "</div>"
			+ "<div class='tf-row'><strong>Diagnoses:</strong> "
			+ line(p.diagnosis1, 190) + line(p.diagnosis2, 190) + line(p.diagnosis3, 190) + line(p.diagnosis4, 190) + "</div>"

			+ "<div class='tf-section-title'>Management at Referring Facility</div>"
			+ "<div class='tf-row'><strong>Respiratory support:</strong>" + line(p.respiratorySupport, 150)
			+ " <strong>IV fluid vol:</strong>" + line(p.ivFluidVol, 100)
			+ " <strong>Passed urine:</strong>" + line(p.passedUrine, 100) + "</div>"
			+ "<div class='tf-row'><strong>Ventilation settings:</strong> " + line(p.ventilationSettings, 700) + "</div>"
			+ "<div class='tf-row'><strong>Inotropes:</strong>" + line(p.inotropes, 200)
			+ " <strong>Peripheral IV:</strong>" + line(p.peripheralIv, 90)
			+ " <strong>Central IV:</strong>" + line(p.centralIv, 90)
			+ " <strong>Intraosseous line:</strong>" + line(p.intraosseousLine, 90) + "</div>"
			+ "<div class='tf-row'><strong>Antibiotic 1:</strong>" + line(p.antibiotic1Name, 180)
			+ " Dose:" + line(p.antibiotic1Doses, 100) + " Duration:" + line(p.antibiotic1Durations, 100) + "</div>"
			+ "<div class='tf-row'><strong>Antibiotic 2:</strong>" + line(p.antibiotic2Name, 180)
			+ " Dose:" + line(p.antibiotic2Doses, 100) + " Duration:" + line(p.antibiotic2Durations, 100) + "</div>"
			+ "<div class='tf-row'><strong>ARVs:</strong> " + line(p.arvs, 400) + "</div>"
			+ "<div class='tf-row'><strong>NPO:</strong>" + line(p.npo, 80)
			+ " <strong>Last feed time:</strong>" + line(p.lastFeedTime, 100)
			+ " <strong>Last feed amount:</strong>" + line(p.lastFeedAmount, 120)
			+ " <strong>Feed volume:</strong>" + line(p.feedVol, 100)
			+ " <strong>Feed type:</strong>" + line(p.feedType, 120) + "</div>"
			+ "<div class='tf-row'><strong>Passed stool:</strong>" + line(p.passedStool, 90)
			+ " <strong>Nasogastric tube:</strong>" + line(p.nasogastricTube, 90) + "</div>"
			+ "<div class='tf-row'><strong>Latest labs:</strong>"
			+ " Glucose:" + line(p.labGlucose, 80)
			+ " FBC:" + line(p.labFbc, 100)
			+ " Hb:" + line(p.labHb, 80)
			+ " WBC:" + line(p.labWbc, 80)
			+ " Platelets:" + line(p.labPlatelets, 90)
			+ " CRP:" + line(p.labCrp, 80) + "</div>"
			+ "<div class='tf-row'>"
			+ " Bili total:" + line(p.labBiliTotal, 90)
			+ " Bili direct:" + line(p.labBiliDirect, 90)
			+ " U&amp;E:" + line(p.labUe, 100)
			+ " Cultures:" + line(p.labCultures, 200) + "</div>"
			+ "<div class='tf-row'><strong>Imaging results:</strong> " + line(p.imagingResults, 700) + "</div>"
			+ "<div class='tf-row'><strong>Pain/sedation drugs:</strong> " + line(p.painSedationDrugs, 700) + "</div>"
			+ "<div class='tf-row'>"
			+ "Imaging report attached:<span class='tf-circle'>" + yesNoCircle(truthy(p.imagingReportAttached)) + "</span>"
			+ " Lab reports attached:<span class='tf-circle'>" + yesNoCircle(truthy(p.labReportsAttached)) + "</span></div>"

			+ "<div class='tf-section-title'>Summary &amp; Sign-off</div>"
			+ "<div class='tf-row'><strong>Clinical management summary:</strong> " + line(p.clinicalManagementSummary, 830) + "</div>"
			+ "<div class='tf-row tf-bottom-gap'><strong>Names of referring health care provider:</strong> " + line(p.referringProviderName, 220)
			+ " <strong>Qualification:</strong> " + line(p.referringProviderQualification, 160) + "</div>"
			+ "<div class='tf-row tf-signature-row'><strong>Date:</strong> " + line(p.referringSignedDate, 120)
			+ " <strong>Time:</strong> " + line(p.referringSignedTime, 120)
			+ " <strong>Phone:</strong> " + line(p.referringProviderPhone, 180) + "</div>"

			+ "</div></div>";
	}

	/**
	 * Opens a print window for the MOH transfer form so the user can Save as PDF.
	 * @param {string|HTMLElement|jQuery} contentOrSelector preview HTML or a container with .transfer-form-preview
	 * @param {{fileName?: string}} [options]
	 * @returns {boolean} true when the print window was opened
	 */
	function exportTransferFormPreviewPdf(contentOrSelector, options) {
		var opts = options || {};
		var contentHtml = "";
		if (typeof contentOrSelector === "string") {
			var trimmed = contentOrSelector.trim();
			if (trimmed.charAt(0) === "<") {
				contentHtml = trimmed;
			} else if (global.jQuery || global.jq) {
				var $ = global.jq || global.jQuery;
				var el = $(trimmed);
				if (el.length) {
					var preview = el.find(".transfer-form-preview").first();
					contentHtml = (preview.length ? preview : el).html() || "";
					if (preview.length) {
						contentHtml = preview[0].outerHTML;
					}
				}
			}
		} else if (contentOrSelector && contentOrSelector.nodeType === 1) {
			var node = contentOrSelector;
			var previewNode = node.querySelector
				? node.querySelector(".transfer-form-preview")
				: null;
			contentHtml = previewNode ? previewNode.outerHTML : (node.outerHTML || node.innerHTML || "");
		} else if (contentOrSelector && contentOrSelector.jquery && contentOrSelector.length) {
			var previewEl = contentOrSelector.find(".transfer-form-preview").first();
			contentHtml = previewEl.length
				? previewEl[0].outerHTML
				: (contentOrSelector.html() || "");
		}

		if (!contentHtml) {
			return false;
		}

		var openmrsPath = global.transferOpenmrsPath
			|| (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "/openmrs");
		while (openmrsPath.length > 1 && openmrsPath.charAt(openmrsPath.length - 1) === "/") {
			openmrsPath = openmrsPath.substring(0, openmrsPath.length - 1);
		}
		var cssHref = openmrsPath + "/moduleResources/transferapp/styles/transferFormPreview.css";
		var title = opts.fileName || "External-Transfer-Form";
		var popup = window.open("", "transfer_form_pdf_export", "width=1200,height=900");
		if (!popup) {
			return false;
		}

		popup.document.open();
		popup.document.write(
			"<!DOCTYPE html><html><head><meta charset='utf-8'/>"
			+ "<title>" + escTransferPreview(title) + "</title>"
			+ "<link rel='stylesheet' type='text/css' href='" + escTransferPreview(cssHref) + "'/>"
			+ "<style>"
			+ "body{margin:12px;background:#fff;}"
			+ ".transfer-form-preview{max-width:100%;}"
			+ "@media print{body{margin:0;} .tf-no-print{display:none !important;}}"
			+ "</style></head><body>"
			+ "<p class='tf-no-print' style='font-family:sans-serif;font-size:13px;color:#334155;margin:0 0 12px;'>"
			+ "Use your browser print dialog and choose <strong>Save as PDF</strong> to keep a copy.</p>"
			+ contentHtml
			+ "<script>window.onload=function(){setTimeout(function(){window.focus();window.print();},250);};<\/script>"
			+ "</body></html>"
		);
		popup.document.close();
		return true;
	}

	global.escTransferPreview = escTransferPreview;
	global.buildTransferFormPreviewHtml = buildTransferFormPreviewHtml;
	global.buildMaternityTransferFormPreviewHtml = buildMaternityTransferFormPreviewHtml;
	global.buildNeonatalTransferFormPreviewHtml = buildNeonatalTransferFormPreviewHtml;
	global.exportTransferFormPreviewPdf = exportTransferFormPreviewPdf;
})(window);
