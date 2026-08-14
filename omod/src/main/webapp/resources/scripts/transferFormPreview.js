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

	function firstNonBlank() {
		for (var i = 0; i < arguments.length; i++) {
			var value = arguments[i];
			if (value !== null && value !== undefined && String(value).trim() !== "") {
				return String(value).trim();
			}
		}
		return "";
	}

	function resolveFlag(value, fallback) {
		if (value === null || value === undefined || value === "") {
			return !!fallback;
		}
		return truthy(value);
	}

	function normalizeTransportType(normalized) {
		return String(normalized.transportationType || normalized.transportType || "").trim().toUpperCase();
	}

	function resolveOtherTransportSpec(normalized, transportType) {
		var other = firstNonBlank(normalized.transportationOtherSpec, normalized.otherTransportType);
		if (!other) {
			return "";
		}
		var upper = other.toUpperCase();
		if (upper === "NA" || upper === "N/A" || upper === "AMBULANCE" || transportType === "NA" || transportType === "AMBULANCE") {
			return firstNonBlank(normalized.transportationOtherSpec);
		}
		return other;
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
		var transferType = String(normalized.transferType || "").trim().toUpperCase().replace(/[\s-]+/g, "_");
		var transportType = normalizeTransportType(normalized);
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
			referringFacilityName: normalized.referringFacilityName || normalized.sendingFacility || normalized.origin,
			referringUnit: normalized.referringUnit || normalized.admitSource,
			receivingClinicianPhone: normalized.receivingClinicianPhone || normalized.staffContactedPhone,
			clientName: normalized.clientName,
			serialNumberEmr: firstNonBlank(
				normalized.serialNumberEmr,
				normalized.serialNumberOrEmrId,
				normalized.emrId,
				normalized.upid,
				normalized.subject
			),
			clientTelephone: firstNonBlank(normalized.clientTelephone, normalized.telephone),
			ageOrDob: normalized.ageOrDob || normalized.ageDob,
			sex: normalized.sex,
			caregiverName: normalized.caregiverName,
			caregiverTelephone: firstNonBlank(normalized.caregiverTelephone, normalized.telephone),
			clientDistrict: firstNonBlank(normalized.clientDistrict, normalized.patientDistrict),
			sector: normalized.sector || normalized.patientSector,
			cell: normalized.cell || normalized.patientCell,
			village: normalized.village || normalized.patientVillage,
			admissionAt: normalized.admissionAt || normalized.admissionDatetime,
			decisionToTransferAt: normalized.decisionToTransferAt || normalized.transferDecisionDatetime,
			receivingFacility: normalized.receivingFacility || normalized.destination || normalized.hospitalName,
			receivingService: normalized.receivingService,
			callingTime: normalized.callingTime,
			staffContactedName: normalized.staffContactedName || normalized.staffContactedAtReceivingFacility,
			staffContactedPhone: normalized.staffContactedPhone || normalized.staffContactPhone,
			isEmergency: resolveFlag(normalized.isEmergency, transferType === "EMERGENCY"),
			isNonEmergency: resolveFlag(normalized.isNonEmergency,
				transferType === "NOT_EMERGENCY" || transferType === "NON_EMERGENCY"),
			isFollowUp: resolveFlag(normalized.isFollowUp, transferType === "FOLLOW_UP" || transferType === "FOLLOWUP"),
			ambulanceCalledTime: normalized.ambulanceCalledTime,
			departureFromReferringTime: normalized.departureFromReferringTime || normalized.departureTime,
			reasonForTransfer: normalized.reasonForTransfer,
			significantFindings: normalized.significantFindings,
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
			isAmbulanceTransport: resolveFlag(normalized.isAmbulanceTransport,
				transportType === "AMBULANCE" || transportType.indexOf("AMBULANCE") >= 0),
			transportationOtherSpec: resolveOtherTransportSpec(normalized, transportType),
			isNaTransport: resolveFlag(normalized.isNaTransport, transportType === "NA" || transportType === "N/A"),
			isCbhiInsurance: resolveFlag(normalized.isCbhiInsurance, normalized.healthInsuranceType === "CBHI"),
			isRssbInsurance: resolveFlag(normalized.isRssbInsurance, normalized.healthInsuranceType === "RSSB"),
			isMmiInsurance: resolveFlag(normalized.isMmiInsurance, normalized.healthInsuranceType === "MMI"),
			otherInsurance: normalized.otherInsurance || normalized.healthInsuranceOtherSpec,
			isNoInsurance: resolveFlag(normalized.isNoInsurance, normalized.healthInsuranceType === "NONE"),
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
			uuid: normalized.uuid || normalized.id || verificationTransferId,
			requiresInsuranceAgentVerification: resolveFlag(normalized.requiresInsuranceAgentVerification, false),
			hasAgentApprovedExtension: resolveFlag(normalized.hasAgentApprovedExtension, false),
			agentApproved: resolveFlag(normalized.agentApproved, false),
			agentComment: firstNonBlank(normalized.agentComment, ""),
			agentRejected: resolveFlag(normalized.agentRejected,
				resolveFlag(normalized.hasAgentApprovedExtension, false)
					&& !resolveFlag(normalized.agentApproved, false)),
			agentDecisionApproved: resolveFlag(normalized.agentDecisionApproved,
				resolveFlag(normalized.hasAgentApprovedExtension, false)
					&& resolveFlag(normalized.agentApproved, false)),
			needsInsuranceApproval: resolveFlag(normalized.needsInsuranceApproval,
				resolveFlag(normalized.requiresInsuranceAgentVerification, false)
					&& !resolveFlag(normalized.hasAgentApprovedExtension, false))
		};
	}

	function buildInsuranceAgentDecisionBannerHtml(p) {
		if (p.agentRejected) {
			var rejectedComment = p.agentComment
				? "<div class='tf-insurance-decision-comment'>" + escTransferPreview(p.agentComment) + "</div>"
				: "";
			return "<div class='tf-insurance-decision tf-insurance-decision--rejected' role='status'>"
				+ "<strong>Rejected by insurance</strong>"
				+ rejectedComment
				+ "</div>";
		}
		if (p.agentDecisionApproved) {
			var approvedComment = p.agentComment
				? "<div class='tf-insurance-decision-comment'>" + escTransferPreview(p.agentComment) + "</div>"
				: "";
			return "<div class='tf-insurance-decision tf-insurance-decision--approved' role='status'>"
				+ "<strong>Approved by insurance</strong>"
				+ approvedComment
				+ "</div>";
		}
		if (p.needsInsuranceApproval) {
			return "<div class='tf-insurance-decision tf-insurance-decision--pending' role='status'>"
				+ "<strong>Awaiting insurance agent decision</strong>"
				+ "</div>";
		}
		return "";
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

		return "<div class='transfer-form-preview'"
			+ " data-requires-insurance-agent-verification='" + (p.requiresInsuranceAgentVerification ? "true" : "false") + "'"
			+ " data-has-agent-approved='" + (p.hasAgentApprovedExtension ? "true" : "false") + "'"
			+ " data-agent-approved='" + (p.agentApproved ? "true" : "false") + "'"
			+ " data-agent-rejected='" + (p.agentRejected ? "true" : "false") + "'"
			+ " data-agent-comment='" + escTransferPreview(p.agentComment || "") + "'"
			+ ">"
			+ buildInsuranceAgentDecisionBannerHtml(p)
			+ "<div class='tf-sheet'>"
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
			+ " <strong>Name of caregiver:</strong> " + line(p.caregiverName, 270)
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
			+ (p.significantFindings ? "<div class='tf-row'>" + line(p.significantFindings, 900) + "</div>" : "")
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
	global.exportTransferFormPreviewPdf = exportTransferFormPreviewPdf;
})(window);
