<%
ui.includeCss("transferapp", "styles/transferWizard.css")
ui.includeCss("transferapp", "styles/flatpickr.min.css")
ui.includeCss("transferapp", "styles/select2.min.css")
%>

<g:if test="${error != null && error.trim().length() > 0}">
    <p style="color: red;">${ ui.format(error) }</p>
</g:if>

<g:if test="${formData}">
<div class="transfer-wizard-shell">
    <header class="transfer-wizard-page-header">
        <h1 class="transfer-wizard-page-title" id="maternity-wizard-page-title">Maternity Transfer Form</h1>
        <p class="transfer-wizard-page-lead" id="maternity-wizard-page-lead">Step 1 — client identification, demographics, and referral.</p>
    </header>

    <div class="transfer-wizard-panel" style="padding: 0 5px;">
        <ul class="transfer-wizard-progress transfer-wizard-progress-5">
            <li data-step="1"><span class="transfer-wizard-step-num">1</span><span class="transfer-wizard-step-label">Client &amp; Referral</span></li>
            <li data-step="2"><span class="transfer-wizard-step-num">2</span><span class="transfer-wizard-step-label">Obstetric History</span></li>
            <li data-step="3"><span class="transfer-wizard-step-num">3</span><span class="transfer-wizard-step-label">Clinical Findings</span></li>
            <li data-step="4"><span class="transfer-wizard-step-num">4</span><span class="transfer-wizard-step-label">Treatment &amp; Transport</span></li>
            <li data-step="5"><span class="transfer-wizard-step-num">5</span><span class="transfer-wizard-step-label">Sign-off</span></li>
        </ul>

        <div class="transfer-wizard-scroll">
        <form id="moh-maternity-transfer-wizard-form" class="transfer-out-form" novalidate="novalidate"
              data-client-name="${ ui.encodeHtmlAttribute(formData.clientName ?: '') }">
            <input type="hidden" name="patientId" value="${ formData.patientId }" />
            <input type="hidden" id="maternityReceivingFacilityId" name="receivingFacilityId" value="" />

            <!-- Step 1: Client & Referral Info -->
            <div class="transfer-wizard-step-panel is-active" data-step="1">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Client information</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="maternityClientName">Client name</label>
                            <input type="text" id="maternityClientName" name="clientName" value="${ ui.encodeHtmlAttribute(formData.clientName ?: '') }" required />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternitySerialNumberEmr">Serial number / EMR ID</label>
                            <input type="text" id="maternitySerialNumberEmr" name="serialNumberEmr" value="${ ui.encodeHtmlAttribute(formData.serialNumberEmr ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityAgeOrDob">Age / DOB</label>
                            <input type="text" id="maternityAgeOrDob" name="ageOrDob" value="${ ui.encodeHtmlAttribute(formData.ageOrDob ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityNextOfKinName">Next of kin name</label>
                            <input type="text" id="maternityNextOfKinName" name="nextOfKinName" value="${ ui.encodeHtmlAttribute(formData.nextOfKinName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityNextOfKinTelephone">Next of kin telephone</label>
                            <input type="tel" id="maternityNextOfKinTelephone" name="nextOfKinTelephone" value="${ ui.encodeHtmlAttribute(formData.nextOfKinTelephone ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityClientDistrict">District</label>
                            <input type="text" id="maternityClientDistrict" name="clientDistrict" value="${ ui.encodeHtmlAttribute(formData.clientDistrict ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternitySector">Sector</label>
                            <input type="text" id="maternitySector" name="sector" value="${ ui.encodeHtmlAttribute(formData.sector ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityCell">Cell</label>
                            <input type="text" id="maternityCell" name="cell" value="${ ui.encodeHtmlAttribute(formData.cell ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityVillage">Village</label>
                            <input type="text" id="maternityVillage" name="village" value="${ ui.encodeHtmlAttribute(formData.village ?: '') }" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Referral information</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="maternityAdmissionAt">Date &amp; time of admission</label>
                            <input type="text" class="js-datetime-picker" id="maternityAdmissionAt" name="admissionAt"
                                   value="${ ui.encodeHtmlAttribute(formData.admissionAt ?: '') }" placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityDecisionToTransferAt">Decision date &amp; time</label>
                            <input type="text" class="js-datetime-picker" id="maternityDecisionToTransferAt" name="decisionToTransferAt"
                                   value="${ ui.encodeHtmlAttribute(formData.decisionToTransferAt ?: '') }" required placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityCallingTime">Calling time</label>
                            <input type="text" class="js-time-picker" id="maternityCallingTime" name="callingTime"
                                   value="${ ui.encodeHtmlAttribute(formData.callingTime ?: '') }" placeholder="Select time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReceivingFacilityCode">Receiving facility</label>
                            <select id="maternityReceivingFacilityCode" name="receivingFacilityCode" required>
                                <option value="">Select receiving facility</option>
                                <% formData.receivingFacilities.each { facility -> %>
                                    <option value="${ ui.encodeHtmlAttribute(facility.value) }"
                                            data-receiving-facility-id="${ facility.receivingFacilityId ?: '' }">
                                        ${ ui.format(facility.label) }
                                    </option>
                                <% } %>
                            </select>
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReceivingService">${ ui.message("transferapp.patient.transfers.receivingService") }</label>
                            <select id="maternityReceivingService" name="receivingService" class="js-maternity-receiving-service-select" required
                                    data-placeholder="${ ui.encodeHtmlAttribute(ui.message('transferapp.patient.transfers.receivingService.placeholder')) }">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityStaffContactedName">Staff contacted</label>
                            <input type="text" id="maternityStaffContactedName" name="staffContactedName" value="${ ui.encodeHtmlAttribute(formData.staffContactedName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityStaffContactedPhone">Contact phone</label>
                            <input type="tel" id="maternityStaffContactedPhone" name="staffContactedPhone" value="${ ui.encodeHtmlAttribute(formData.staffContactedPhone ?: '') }" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">${ ui.message("transferapp.patient.transfers.transferType") }</h2>
                    <div class="transfer-type-options">
                        <% formData.transferTypes.each { type -> %>
                        <span class="transfer-type-option">
                            <input type="radio" name="transferType" id="maternityTransferType_${ ui.encodeHtmlAttribute(type.value) }"
                                   value="${ ui.encodeHtmlAttribute(type.value) }" required />
                            <label for="maternityTransferType_${ ui.encodeHtmlAttribute(type.value) }">${ ui.format(type.label) }</label>
                        </span>
                        <% } %>
                    </div>
                </div>

                <div id="maternityEmergencyFields" class="transfer-emergency-panel">
                    <div class="transfer-wizard-row transfer-wizard-row-two-col">
                        <div class="transfer-wizard-field">
                            <label for="maternityAmbulanceCalledTime">${ ui.message("transferapp.patient.transfers.ambulanceCalledTime") }</label>
                            <input type="text" class="js-time-picker" id="maternityAmbulanceCalledTime" name="ambulanceCalledTime"
                                   value="" placeholder="Select time" autocomplete="off" data-emergency-required="true" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityDepartureFromReferringTime">${ ui.message("transferapp.patient.transfers.departureFromReferringTime") }</label>
                            <input type="text" class="js-time-picker" id="maternityDepartureFromReferringTime" name="departureFromReferringTime"
                                   value="" placeholder="Select time" autocomplete="off" data-emergency-required="true" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section transfer-wizard-field">
                    <label for="maternityReasonForTransfer" class="transfer-wizard-section-title">Reason for transfer</label>
                    <textarea id="maternityReasonForTransfer" name="reasonForTransfer" rows="4" required placeholder="Enter the reason for transfer"></textarea>
                </div>

                <div class="transfer-wizard-section">
                    <div class="transfer-wizard-row transfer-wizard-row-two-col">
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="partographAttached" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Partograph attached</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityDisabilityType">If person with disability, type of disability</label>
                            <input type="text" id="maternityDisabilityType" name="disabilityType" value="${ ui.encodeHtmlAttribute(formData.disabilityType ?: '') }" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="maternityClinicalPresentation">Clinical presentation</label>
                        <textarea id="maternityClinicalPresentation" name="clinicalPresentation" rows="3" placeholder="Enter clinical presentation"></textarea>
                    </div>
                </div>
            </div>

            <!-- Step 2: Obstetric History & Current Pregnancy -->
            <div class="transfer-wizard-step-panel" data-step="2">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Obstetric history</h2>
                    <div class="transfer-vitals-grid">
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricGravida">Gravida</label>
                            <input type="text" id="obstetricGravida" name="obstetricGravida" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricParity">Parity</label>
                            <input type="text" id="obstetricParity" name="obstetricParity" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricLivingChildren">Living children</label>
                            <input type="text" id="obstetricLivingChildren" name="obstetricLivingChildren" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricAbortion">Abortion</label>
                            <input type="text" id="obstetricAbortion" name="obstetricAbortion" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricStillbirth">Stillbirth</label>
                            <input type="text" id="obstetricStillbirth" name="obstetricStillbirth" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricNeonatalDeath">Neonatal death</label>
                            <input type="text" id="obstetricNeonatalDeath" name="obstetricNeonatalDeath" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="obstetricPretermBirth">Preterm birth</label>
                            <input type="text" id="obstetricPretermBirth" name="obstetricPretermBirth" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Current pregnancy</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="lmpDate">LMP date</label>
                            <input type="text" class="js-date-picker" id="lmpDate" name="lmpDate" placeholder="Select date" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="eddDate">EDD date</label>
                            <input type="text" class="js-date-picker" id="eddDate" name="eddDate" placeholder="Select date" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="gestationAge">Gestation age</label>
                            <input type="text" id="gestationAge" name="gestationAge" placeholder="e.g. 34 weeks" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="obstetricMuac">MUAC</label>
                            <input type="text" id="obstetricMuac" name="muac" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="ancCompletedCount">ANC visits completed</label>
                            <input type="text" id="ancCompletedCount" name="ancCompletedCount" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="tetanusVaccineDoses">Tetanus vaccine doses</label>
                            <input type="text" id="tetanusVaccineDoses" name="tetanusVaccineDoses" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="previousSignificantHistory">Previous significant history</label>
                        <textarea id="previousSignificantHistory" name="previousSignificantHistory" rows="3"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="currentPregnancyComplications">Current pregnancy complications</label>
                        <textarea id="currentPregnancyComplications" name="currentPregnancyComplications" rows="3"></textarea>
                    </div>
                </div>
            </div>

            <!-- Step 3: Clinical Findings -->
            <div class="transfer-wizard-step-panel" data-step="3">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Latest results</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="latestHemoglobin">Hemoglobin</label>
                            <input type="text" id="latestHemoglobin" name="latestHemoglobin" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="latestHivStatus">HIV status</label>
                            <input type="text" id="latestHivStatus" name="latestHivStatus" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="latestBloodGroup">Blood group</label>
                            <input type="text" id="latestBloodGroup" name="latestBloodGroup" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="latestOtherResults">Other results</label>
                        <textarea id="latestOtherResults" name="latestOtherResults" rows="2"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Vital signs</h2>
                    <div class="transfer-vitals-grid">
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalBp">BP</label>
                            <input type="text" id="maternityVitalBp" name="vitalBp" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalTemp">Temp</label>
                            <input type="text" id="maternityVitalTemp" name="vitalTemp" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalSpo2">SpO2</label>
                            <input type="text" id="maternityVitalSpo2" name="vitalSpo2" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalRr">RR</label>
                            <input type="text" id="maternityVitalRr" name="vitalRr" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalPulse">Pulse</label>
                            <input type="text" id="maternityVitalPulse" name="vitalPulse" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalWeight">Weight</label>
                            <input type="text" id="maternityVitalWeight" name="vitalWeight" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="maternityVitalHeight">Height</label>
                            <input type="text" id="maternityVitalHeight" name="vitalHeight" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Abdominal &amp; vaginal exam</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="fetalPresentation">Fetal presentation</label>
                            <input type="text" id="fetalPresentation" name="fetalPresentation" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="fundalHeight">Fundal height</label>
                            <input type="text" id="fundalHeight" name="fundalHeight" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="fetalHeartRate">Fetal heart rate</label>
                            <input type="text" id="fetalHeartRate" name="fetalHeartRate" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="contractions">Contractions</label>
                            <input type="text" id="contractions" name="contractions" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="vaginalExamAt">Vaginal exam date &amp; time</label>
                            <input type="text" class="js-datetime-picker" id="vaginalExamAt" name="vaginalExamAt" placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="dilation">Dilation</label>
                            <input type="text" id="dilation" name="dilation" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="effacement">Effacement</label>
                            <input type="text" id="effacement" name="effacement" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="descent">Descent</label>
                            <input type="text" id="descent" name="descent" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="consistency">Consistency</label>
                            <input type="text" id="consistency" name="consistency" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityPosition">Position</label>
                            <input type="text" id="maternityPosition" name="position" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="membranesRupturedAt">Membranes ruptured at</label>
                            <input type="text" class="js-datetime-picker" id="membranesRupturedAt" name="membranesRupturedAt" placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="amnioticFluidColor">Amniotic fluid color</label>
                            <input type="text" id="amnioticFluidColor" name="amnioticFluidColor" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="estimatedBloodLossMl">Estimated blood loss (mL)</label>
                            <input type="text" id="estimatedBloodLossMl" name="estimatedBloodLossMl" />
                        </div>
                    </div>
                    <div class="transfer-wizard-row transfer-wizard-row-two-col" style="margin-top:0.75rem;">
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="caput" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Caput</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="moulding" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Moulding</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="membranesRuptured" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Membranes ruptured</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="offensive" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Offensive</label>
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Investigations &amp; diagnosis</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="investigationHgb">Investigation HGB</label>
                            <input type="text" id="investigationHgb" name="investigationHgb" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="investigationUrineTest">Urine test</label>
                            <input type="text" id="investigationUrineTest" name="investigationUrineTest" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="investigationOtherTest">Other test</label>
                            <input type="text" id="investigationOtherTest" name="investigationOtherTest" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="imagingInvestigations">Imaging investigations</label>
                        <textarea id="imagingInvestigations" name="imagingInvestigations" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="maternityDiagnosis">Diagnosis</label>
                        <textarea id="maternityDiagnosis" name="diagnosis" rows="3"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="maternityProcedures">Procedures</label>
                        <textarea id="maternityProcedures" name="procedures" rows="3"></textarea>
                    </div>
                    <div class="transfer-wizard-row transfer-wizard-row-two-col">
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="attachedLabTests" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Lab tests attached</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" name="attachedImaging" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Imaging attached</label>
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="attachedOther">Other attachments</label>
                        <input type="text" id="attachedOther" name="attachedOther" />
                    </div>
                </div>
            </div>

            <!-- Step 4: Treatment & Transport -->
            <div class="transfer-wizard-step-panel" data-step="4">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Treatment given</h2>
                    <div style="overflow-x:auto;">
                        <table id="maternity-treatment-table" style="border-collapse:collapse;width:100%;">
                            <thead>
                                <tr>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Treatment</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Dose</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Date</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Time</th>
                                    <th style="border-bottom:2px solid #e2e8f0;"></th>
                                </tr>
                            </thead>
                            <tbody id="maternity-treatment-table-body">
                                <% formData.defaultTreatmentRows.each { row -> %>
                                <tr class="maternity-treatment-row">
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;">
                                        <input type="text" name="treatmentName" value="${ ui.encodeHtmlAttribute(row.treatmentName ?: '') }" />
                                    </td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" name="treatmentDose" value="" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" class="js-date-picker" name="treatmentGivenDate" value="" placeholder="Date" autocomplete="off" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" class="js-time-picker" name="treatmentGivenTime" value="" placeholder="Time" autocomplete="off" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><button type="button" class="transfer-wizard-btn transfer-wizard-btn-outline js-remove-treatment-row">Remove</button></td>
                                </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                    <button type="button" id="maternity-add-treatment-row" class="transfer-wizard-btn transfer-wizard-btn-outline" style="margin-top:0.5rem;">Add treatment row</button>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">${ ui.message("transferapp.patient.transfers.transportationType") }</h2>
                    <div class="transfer-transport-options">
                        <% formData.transportationTypes.each { transport -> %>
                        <span class="transfer-transport-option">
                            <input type="radio" name="transportationType"
                                   id="maternityTransportationType_${ ui.encodeHtmlAttribute(transport.value) }"
                                   value="${ ui.encodeHtmlAttribute(transport.value) }"
                                   data-transport-value="${ ui.encodeHtmlAttribute(transport.value) }" />
                            <label for="maternityTransportationType_${ ui.encodeHtmlAttribute(transport.value) }">${ ui.format(transport.label) }</label>
                        </span>
                        <% } %>
                    </div>
                    <div id="maternityTransportOtherField" class="transfer-transport-other transfer-wizard-field">
                        <label for="maternityTransportationOtherSpec">${ ui.message("transferapp.patient.transfers.transportationOtherSpec") }</label>
                        <input type="text" id="maternityTransportationOtherSpec" name="transportationOtherSpec" maxlength="255"
                               placeholder="${ ui.message('transferapp.patient.transfers.transportationOtherSpec.placeholder') }" />
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Health insurance</h2>
                    <div class="transfer-insurance-options">
                        <% formData.healthInsuranceTypes.each { insurance -> %>
                        <span class="transfer-insurance-option">
                            <input type="radio" name="healthInsuranceType"
                                   id="maternityHealthInsuranceType_${ ui.encodeHtmlAttribute(insurance.value) }"
                                   value="${ ui.encodeHtmlAttribute(insurance.value) }" required />
                            <label for="maternityHealthInsuranceType_${ ui.encodeHtmlAttribute(insurance.value) }">${ ui.format(insurance.label) }</label>
                        </span>
                        <% } %>
                    </div>
                    <div id="maternityInsuranceOtherField" class="transfer-insurance-other transfer-wizard-field">
                        <label for="maternityHealthInsuranceOtherSpec">Other (specify)</label>
                        <input type="text" id="maternityHealthInsuranceOtherSpec" name="healthInsuranceOtherSpec" maxlength="255" />
                    </div>
                </div>
            </div>

            <!-- Step 5: Sign-off -->
            <div class="transfer-wizard-step-panel" data-step="5">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Referring provider sign-off</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="maternityReferringProviderName">Provider name</label>
                            <input type="text" id="maternityReferringProviderName" name="referringProviderName"
                                   value="${ ui.encodeHtmlAttribute(formData.referringProviderName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReferringProviderQualification">Qualification</label>
                            <input type="text" id="maternityReferringProviderQualification" name="referringProviderQualification" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReferringProviderPhone">Provider phone</label>
                            <input type="tel" id="maternityReferringProviderPhone" name="referringProviderPhone" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReferringSignedDate">Date</label>
                            <input type="text" class="js-date-picker" id="maternityReferringSignedDate" name="referringSignedDate"
                                   value="${ ui.encodeHtmlAttribute(formData.referringSignedDate ?: '') }" placeholder="Select date" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="maternityReferringSignedTime">Time</label>
                            <input type="text" class="js-time-picker" id="maternityReferringSignedTime" name="referringSignedTime"
                                   value="${ ui.encodeHtmlAttribute(formData.referringSignedTime ?: '') }" placeholder="Select time" autocomplete="off" />
                        </div>
                    </div>
                </div>
            </div>

        </form>
        </div>
    </div>
</div>
</g:if>
