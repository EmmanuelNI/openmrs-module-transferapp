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
        <h1 class="transfer-wizard-page-title" id="neonatal-wizard-page-title">Neonatal Transfer Form</h1>
        <p class="transfer-wizard-page-lead" id="neonatal-wizard-page-lead">Step 1 — baby identification, mother details, and referral.</p>
    </header>

    <div class="transfer-wizard-panel" style="padding: 0 5px;">
        <ul class="transfer-wizard-progress transfer-wizard-progress-7">
            <li data-step="1"><span class="transfer-wizard-step-num">1</span><span class="transfer-wizard-step-label">Baby &amp; Referral</span></li>
            <li data-step="2"><span class="transfer-wizard-step-num">2</span><span class="transfer-wizard-step-label">Maternal History</span></li>
            <li data-step="3"><span class="transfer-wizard-step-num">3</span><span class="transfer-wizard-step-label">Labor Details</span></li>
            <li data-step="4"><span class="transfer-wizard-step-num">4</span><span class="transfer-wizard-step-label">Neonatal History &amp; Drugs</span></li>
            <li data-step="5"><span class="transfer-wizard-step-num">5</span><span class="transfer-wizard-step-label">Chief Complaint &amp; Diagnoses</span></li>
            <li data-step="6"><span class="transfer-wizard-step-num">6</span><span class="transfer-wizard-step-label">Management</span></li>
            <li data-step="7"><span class="transfer-wizard-step-num">7</span><span class="transfer-wizard-step-label">Summary &amp; Sign-off</span></li>
        </ul>

        <div class="transfer-wizard-scroll">
        <form id="moh-neonatal-transfer-wizard-form" class="transfer-out-form" novalidate="novalidate"
              data-client-name="${ ui.encodeHtmlAttribute(formData.babyName ?: '') }">
            <input type="hidden" name="patientId" value="${ formData.patientId }" />
            <input type="hidden" id="neonatalReceivingFacilityId" name="receivingFacilityId" value="" />

            <!-- Step 1: Baby & Referral Info -->
            <div class="transfer-wizard-step-panel is-active" data-step="1">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Baby information</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalBabyName">Baby name</label>
                            <input type="text" id="neonatalBabyName" name="babyName" value="${ ui.encodeHtmlAttribute(formData.babyName ?: '') }" required />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalSex">Sex</label>
                            <select id="neonatalSex" name="sex">
                                <option value="">Select sex</option>
                                <option value="M" <% if (formData.sex == "M") { %>selected="selected"<% } %>>Male</option>
                                <option value="F" <% if (formData.sex == "F") { %>selected="selected"<% } %>>Female</option>
                                <option value="AMBIGUOUS" <% if (formData.sex == "AMBIGUOUS") { %>selected="selected"<% } %>>Ambiguous</option>
                            </select>
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalDob">Date of birth</label>
                            <input type="text" class="js-date-picker" id="neonatalDob" name="dob"
                                   value="${ ui.encodeHtmlAttribute(formData.dob ?: '') }" placeholder="Select date" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalGestationalAgeWeeks">Gestational age (weeks)</label>
                            <input type="text" id="neonatalGestationalAgeWeeks" name="gestationalAgeWeeks" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalBirthWeightG">Birth weight (g)</label>
                            <input type="text" id="neonatalBirthWeightG" name="birthWeightG" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalCurrentWeightG">Current weight (g)</label>
                            <input type="text" id="neonatalCurrentWeightG" name="currentWeightG" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalCurrentAgeDays">Current age (days)</label>
                            <input type="text" id="neonatalCurrentAgeDays" name="currentAgeDays" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Mother information</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalMotherName">Mother name</label>
                            <input type="text" id="neonatalMotherName" name="motherName" value="${ ui.encodeHtmlAttribute(formData.motherName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalMotherAge">Mother age</label>
                            <input type="text" id="neonatalMotherAge" name="motherAge" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalMotherCaregiverPhone">Mother / caregiver phone</label>
                            <input type="tel" id="neonatalMotherCaregiverPhone" name="motherCaregiverPhone" value="${ ui.encodeHtmlAttribute(formData.motherCaregiverPhone ?: '') }" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Referral information</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalPlaceOfBirth">Place of birth</label>
                            <input type="text" id="neonatalPlaceOfBirth" name="placeOfBirth" value="${ ui.encodeHtmlAttribute(formData.placeOfBirth ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalDecisionToTransferAt">Decision date &amp; time</label>
                            <input type="text" class="js-datetime-picker" id="neonatalDecisionToTransferAt" name="decisionToTransferAt"
                                   value="${ ui.encodeHtmlAttribute(formData.decisionToTransferAt ?: '') }" required placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalCallingTime">Calling time</label>
                            <input type="text" class="js-time-picker" id="neonatalCallingTime" name="callingTime"
                                   value="${ ui.encodeHtmlAttribute(formData.callingTime ?: '') }" placeholder="Select time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalReceivingFacilityCode">Receiving facility</label>
                            <select id="neonatalReceivingFacilityCode" name="receivingFacilityCode" required>
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
                            <label for="neonatalReceivingService">${ ui.message("transferapp.patient.transfers.receivingService") }</label>
                            <select id="neonatalReceivingService" name="receivingService" class="js-neonatal-receiving-service-select" required
                                    data-placeholder="${ ui.encodeHtmlAttribute(ui.message('transferapp.patient.transfers.receivingService.placeholder')) }">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalStaffContactedName">Staff contacted</label>
                            <input type="text" id="neonatalStaffContactedName" name="staffContactedName" value="${ ui.encodeHtmlAttribute(formData.staffContactedName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalStaffContactedPhone">Contact phone</label>
                            <input type="tel" id="neonatalStaffContactedPhone" name="staffContactedPhone" value="${ ui.encodeHtmlAttribute(formData.staffContactedPhone ?: '') }" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">${ ui.message("transferapp.patient.transfers.transferType") }</h2>
                    <div class="transfer-type-options">
                        <% formData.transferTypes.each { type -> %>
                        <span class="transfer-type-option">
                            <input type="radio" name="transferType" id="neonatalTransferType_${ ui.encodeHtmlAttribute(type.value) }"
                                   value="${ ui.encodeHtmlAttribute(type.value) }" required />
                            <label for="neonatalTransferType_${ ui.encodeHtmlAttribute(type.value) }">${ ui.format(type.label) }</label>
                        </span>
                        <% } %>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Mode of transport</h2>
                    <div class="transfer-transport-options">
                        <% formData.transportTypes.each { transport -> %>
                        <span class="transfer-transport-option">
                            <input type="radio" name="modeOfTransport"
                                   id="neonatalModeOfTransport_${ ui.encodeHtmlAttribute(transport.value) }"
                                   value="${ ui.encodeHtmlAttribute(transport.value) }" />
                            <label for="neonatalModeOfTransport_${ ui.encodeHtmlAttribute(transport.value) }">${ ui.format(transport.label) }</label>
                        </span>
                        <% } %>
                    </div>
                    <div id="neonatalTransportOtherField" class="transfer-transport-other transfer-wizard-field">
                        <label for="neonatalTransportOther">Other mode of transport (specify)</label>
                        <input type="text" id="neonatalTransportOther" name="transportOther" maxlength="255" />
                    </div>
                </div>

                <div class="transfer-wizard-section transfer-wizard-field">
                    <label for="neonatalReasonForTransfer" class="transfer-wizard-section-title">Reason for transfer</label>
                    <textarea id="neonatalReasonForTransfer" name="reasonForTransfer" rows="4" required placeholder="Enter the reason for transfer"></textarea>
                </div>
            </div>

            <!-- Step 2: Maternal History -->
            <div class="transfer-wizard-step-panel" data-step="2">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Maternal history</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalMotherAlive">Mother alive</label>
                            <input type="text" id="neonatalMotherAlive" name="motherAlive" placeholder="Alive / Deceased / Unknown" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalObstetricGravida">Gravida</label>
                            <input type="text" id="neonatalObstetricGravida" name="obstetricGravida" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalObstetricParity">Parity</label>
                            <input type="text" id="neonatalObstetricParity" name="obstetricParity" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalPregnancyType">Pregnancy type</label>
                            <input type="text" id="neonatalPregnancyType" name="pregnancyType" placeholder="Singleton / Twin / Multiple" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalBloodGroup">Blood group</label>
                            <input type="text" id="neonatalBloodGroup" name="bloodGroup" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalRhFactor">Rh factor</label>
                            <input type="text" id="neonatalRhFactor" name="rhFactor" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalTetanusVaccineDoses">Tetanus vaccine doses</label>
                            <input type="text" id="neonatalTetanusVaccineDoses" name="tetanusVaccineDoses" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalAncScreening">ANC screening</label>
                        <textarea id="neonatalAncScreening" name="ancScreening" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalPathologiesDuringPregnancy">Pathologies during pregnancy</label>
                        <textarea id="neonatalPathologiesDuringPregnancy" name="pathologiesDuringPregnancy" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalPregnancyTreatment">Pregnancy treatment</label>
                        <textarea id="neonatalPregnancyTreatment" name="pregnancyTreatment" rows="2"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">HIV status</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalHivStatus">HIV status</label>
                            <input type="text" id="neonatalHivStatus" name="hivStatus" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalHivRegimen">HIV regimen</label>
                            <input type="text" id="neonatalHivRegimen" name="hivRegimen" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalHivRecentVl">Recent viral load</label>
                            <input type="text" id="neonatalHivRecentVl" name="hivRecentVl" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalHivCd4Count">CD4 count</label>
                            <input type="text" id="neonatalHivCd4Count" name="hivCd4Count" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalHivOpportunisticInfections">Opportunistic infections</label>
                        <textarea id="neonatalHivOpportunisticInfections" name="hivOpportunisticInfections" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalMaternalIllicitDrugHistory">Illicit drug history</label>
                        <textarea id="neonatalMaternalIllicitDrugHistory" name="maternalIllicitDrugHistory" rows="2"></textarea>
                    </div>
                </div>
            </div>

            <!-- Step 3: Labor Details -->
            <div class="transfer-wizard-step-panel" data-step="3">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Rupture of membranes &amp; fever</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalRomAt">ROM date &amp; time</label>
                            <input type="text" class="js-datetime-picker" id="neonatalRomAt" name="romAt" placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalAfQuality">AF quality</label>
                            <input type="text" id="neonatalAfQuality" name="afQuality" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalAfQuantity">AF quantity</label>
                            <input type="text" id="neonatalAfQuantity" name="afQuantity" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalFeverTiming">Fever timing</label>
                            <input type="text" id="neonatalFeverTiming" name="feverTiming" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Steroids &amp; MgSO4</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalSteroidDoses">Steroid doses</label>
                            <input type="text" id="neonatalSteroidDoses" name="steroidDoses" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalLastSteroidDoseAt">Last steroid dose at</label>
                            <input type="text" class="js-datetime-picker" id="neonatalLastSteroidDoseAt" name="lastSteroidDoseAt" placeholder="Select date and time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalMgso4At">MgSO4 given at</label>
                            <input type="text" class="js-datetime-picker" id="neonatalMgso4At" name="mgso4At" placeholder="Select date and time" autocomplete="off" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Delivery &amp; complications</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalModeOfDelivery">Mode of delivery</label>
                            <input type="text" id="neonatalModeOfDelivery" name="modeOfDelivery" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalLaborComplications">Labor complications</label>
                            <input type="text" id="neonatalLaborComplications" name="laborComplications" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalLaborComplicationsOther">Labor complications (other)</label>
                            <input type="text" id="neonatalLaborComplicationsOther" name="laborComplicationsOther" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalMaternalAnesthesia">Maternal anesthesia</label>
                            <input type="text" id="neonatalMaternalAnesthesia" name="maternalAnesthesia" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalMaternalAnesthesiaOther">Maternal anesthesia (other)</label>
                            <input type="text" id="neonatalMaternalAnesthesiaOther" name="maternalAnesthesiaOther" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalMaternalAntibiotics">Maternal antibiotics</label>
                        <textarea id="neonatalMaternalAntibiotics" name="maternalAntibiotics" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalOtherDrugs">Other drugs</label>
                        <textarea id="neonatalOtherDrugs" name="otherDrugs" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalSepsisRiskFactors">Sepsis risk factors</label>
                        <textarea id="neonatalSepsisRiskFactors" name="sepsisRiskFactors" rows="2"></textarea>
                    </div>
                </div>
            </div>

            <!-- Step 4: Neonatal History & Drugs -->
            <div class="transfer-wizard-step-panel" data-step="4">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Resuscitation &amp; APGAR</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalResuscitationAtBirth">Resuscitation at birth</label>
                            <input type="text" id="neonatalResuscitationAtBirth" name="resuscitationAtBirth" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalApgar1min">APGAR (1 min)</label>
                            <input type="text" id="neonatalApgar1min" name="apgar1min" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalApgar5min">APGAR (5 min)</label>
                            <input type="text" id="neonatalApgar5min" name="apgar5min" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalApgar10min">APGAR (10 min)</label>
                            <input type="text" id="neonatalApgar10min" name="apgar10min" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalResuscitationMethods">Resuscitation methods</label>
                        <textarea id="neonatalResuscitationMethods" name="resuscitationMethods" rows="2"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">HIE &amp; allergies</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalHie">HIE</label>
                            <input type="text" id="neonatalHie" name="hie" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalHieGrade">HIE grade</label>
                            <input type="text" id="neonatalHieGrade" name="hieGrade" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalAllergies">Allergies</label>
                            <input type="text" id="neonatalAllergies" name="allergies" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Immunization &amp; prophylaxis</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalImmunization">Immunization</label>
                            <input type="text" id="neonatalImmunization" name="immunization" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalVitaminK">Vitamin K</label>
                            <input type="text" id="neonatalVitaminK" name="vitaminK" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalTetracyclineEyeOintment">Tetracycline eye ointment</label>
                            <input type="text" id="neonatalTetracyclineEyeOintment" name="tetracyclineEyeOintment" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalSurfactant">Surfactant</label>
                            <input type="text" id="neonatalSurfactant" name="surfactant" placeholder="Yes / No" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalImmunizationDetails">Immunization details</label>
                        <textarea id="neonatalImmunizationDetails" name="immunizationDetails" rows="2"></textarea>
                    </div>
                </div>
            </div>

            <!-- Step 5: Chief Complaint & Diagnoses -->
            <div class="transfer-wizard-step-panel" data-step="5">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Chief complaint</h2>
                    <div class="transfer-wizard-field">
                        <label for="neonatalChiefComplaintDetails">Chief complaint details</label>
                        <textarea id="neonatalChiefComplaintDetails" name="chiefComplaintDetails" rows="3"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Clinical condition</h2>
                    <div class="transfer-vitals-grid">
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalSpo2Preductal">SpO2 (pre-ductal)</label>
                            <input type="text" id="neonatalSpo2Preductal" name="spo2Preductal" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalSpo2Postductal">SpO2 (post-ductal)</label>
                            <input type="text" id="neonatalSpo2Postductal" name="spo2Postductal" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalConditionTemp">Temp</label>
                            <input type="text" id="neonatalConditionTemp" name="conditionTemp" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalConditionHr">HR</label>
                            <input type="text" id="neonatalConditionHr" name="conditionHr" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalConditionRr">RR</label>
                            <input type="text" id="neonatalConditionRr" name="conditionRr" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalConditionBp">BP</label>
                            <input type="text" id="neonatalConditionBp" name="conditionBp" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalNeurologicalStatus">Neurological status</label>
                            <input type="text" id="neonatalNeurologicalStatus" name="neurologicalStatus" />
                        </div>
                    </div>
                    <div class="transfer-wizard-row transfer-wizard-row-two-col" style="margin-top:0.75rem;">
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" id="neonatalSeizures" name="seizures" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Seizures</label>
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalAdverseEvents24h">Adverse events (24h)</label>
                        <textarea id="neonatalAdverseEvents24h" name="adverseEvents24h" rows="2"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Diagnoses</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-two-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalDiagnosis1">Diagnosis 1</label>
                            <input type="text" id="neonatalDiagnosis1" name="diagnosis1" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalDiagnosis2">Diagnosis 2</label>
                            <input type="text" id="neonatalDiagnosis2" name="diagnosis2" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalDiagnosis3">Diagnosis 3</label>
                            <input type="text" id="neonatalDiagnosis3" name="diagnosis3" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalDiagnosis4">Diagnosis 4</label>
                            <input type="text" id="neonatalDiagnosis4" name="diagnosis4" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- Step 6: Management at Referring Facility -->
            <div class="transfer-wizard-step-panel" data-step="6">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Airway, breathing &amp; circulation</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalRespiratorySupport">Respiratory support</label>
                            <input type="text" id="neonatalRespiratorySupport" name="respiratorySupport" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalIvFluidVol">IV fluid volume</label>
                            <input type="text" id="neonatalIvFluidVol" name="ivFluidVol" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalPassedUrine">Passed urine</label>
                            <input type="text" id="neonatalPassedUrine" name="passedUrine" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalInotropes">Inotropes</label>
                            <input type="text" id="neonatalInotropes" name="inotropes" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalPeripheralIv">Peripheral IV</label>
                            <input type="text" id="neonatalPeripheralIv" name="peripheralIv" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalCentralIv">Central IV</label>
                            <input type="text" id="neonatalCentralIv" name="centralIv" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalIntraosseousLine">Intraosseous line</label>
                            <input type="text" id="neonatalIntraosseousLine" name="intraosseousLine" placeholder="Yes / No" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalVentilationSettings">Ventilation settings</label>
                        <textarea id="neonatalVentilationSettings" name="ventilationSettings" rows="2"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Infectious &amp; antibiotics</h2>
                    <div style="overflow-x:auto;">
                        <table style="border-collapse:collapse;width:100%;">
                            <thead>
                                <tr>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Antibiotic</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Name</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Doses</th>
                                    <th style="text-align:left;padding:0.4rem;border-bottom:2px solid #e2e8f0;">Duration</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;">1</td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic1Name" name="antibiotic1Name" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic1Doses" name="antibiotic1Doses" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic1Durations" name="antibiotic1Durations" /></td>
                                </tr>
                                <tr>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;">2</td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic2Name" name="antibiotic2Name" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic2Doses" name="antibiotic2Doses" /></td>
                                    <td style="padding:0.3rem;border-bottom:1px solid #eef2f6;"><input type="text" id="neonatalAntibiotic2Durations" name="antibiotic2Durations" /></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="transfer-wizard-field" style="margin-top:0.75rem;">
                        <label for="neonatalArvs">ARVs</label>
                        <input type="text" id="neonatalArvs" name="arvs" />
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Feeding &amp; GIT</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalNpo">NPO</label>
                            <input type="text" id="neonatalNpo" name="npo" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalLastFeedTime">Last feed time</label>
                            <input type="text" class="js-time-picker" id="neonatalLastFeedTime" name="lastFeedTime" placeholder="Select time" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalLastFeedAmount">Last feed amount</label>
                            <input type="text" id="neonatalLastFeedAmount" name="lastFeedAmount" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalFeedVol">Feed volume</label>
                            <input type="text" id="neonatalFeedVol" name="feedVol" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalFeedType">Feed type</label>
                            <input type="text" id="neonatalFeedType" name="feedType" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalPassedStool">Passed stool</label>
                            <input type="text" id="neonatalPassedStool" name="passedStool" placeholder="Yes / No" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalNasogastricTube">Nasogastric tube</label>
                            <input type="text" id="neonatalNasogastricTube" name="nasogastricTube" placeholder="Yes / No" />
                        </div>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Latest labs &amp; imaging</h2>
                    <div class="transfer-vitals-grid">
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabGlucose">Glucose</label>
                            <input type="text" id="neonatalLabGlucose" name="labGlucose" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabFbc">FBC</label>
                            <input type="text" id="neonatalLabFbc" name="labFbc" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabHb">Hb</label>
                            <input type="text" id="neonatalLabHb" name="labHb" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabWbc">WBC</label>
                            <input type="text" id="neonatalLabWbc" name="labWbc" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabPlatelets">Platelets</label>
                            <input type="text" id="neonatalLabPlatelets" name="labPlatelets" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabCrp">CRP</label>
                            <input type="text" id="neonatalLabCrp" name="labCrp" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabBiliTotal">Bilirubin (total)</label>
                            <input type="text" id="neonatalLabBiliTotal" name="labBiliTotal" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabBiliDirect">Bilirubin (direct)</label>
                            <input type="text" id="neonatalLabBiliDirect" name="labBiliDirect" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabUe">U&amp;E</label>
                            <input type="text" id="neonatalLabUe" name="labUe" />
                        </div>
                        <div class="transfer-wizard-field transfer-vital-field">
                            <label for="neonatalLabCultures">Cultures</label>
                            <input type="text" id="neonatalLabCultures" name="labCultures" />
                        </div>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalImagingResults">Imaging results</label>
                        <textarea id="neonatalImagingResults" name="imagingResults" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-field">
                        <label for="neonatalPainSedationDrugs">Pain / sedation drugs</label>
                        <textarea id="neonatalPainSedationDrugs" name="painSedationDrugs" rows="2"></textarea>
                    </div>
                    <div class="transfer-wizard-row transfer-wizard-row-two-col">
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" id="neonatalImagingReportAttached" name="imagingReportAttached" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Imaging report attached</label>
                        </div>
                        <div class="transfer-wizard-field">
                            <label><input type="checkbox" id="neonatalLabReportsAttached" name="labReportsAttached" value="true" style="width:auto;display:inline-block;margin-right:0.4rem;" /> Lab reports attached</label>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Step 7: Summary & Sign-off -->
            <div class="transfer-wizard-step-panel" data-step="7">
                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Clinical management summary</h2>
                    <div class="transfer-wizard-field">
                        <label for="neonatalClinicalManagementSummary">Summary</label>
                        <textarea id="neonatalClinicalManagementSummary" name="clinicalManagementSummary" rows="5"></textarea>
                    </div>
                </div>

                <div class="transfer-wizard-section">
                    <h2 class="transfer-wizard-section-title">Referring provider sign-off</h2>
                    <div class="transfer-wizard-row transfer-wizard-row-three-col">
                        <div class="transfer-wizard-field">
                            <label for="neonatalReferringProviderName">Provider name</label>
                            <input type="text" id="neonatalReferringProviderName" name="referringProviderName"
                                   value="${ ui.encodeHtmlAttribute(formData.referringProviderName ?: '') }" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalReferringProviderQualification">Qualification</label>
                            <input type="text" id="neonatalReferringProviderQualification" name="referringProviderQualification" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalReferringProviderPhone">Provider phone</label>
                            <input type="tel" id="neonatalReferringProviderPhone" name="referringProviderPhone" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalReferringSignedDate">Date</label>
                            <input type="text" class="js-date-picker" id="neonatalReferringSignedDate" name="referringSignedDate"
                                   value="${ ui.encodeHtmlAttribute(formData.referringSignedDate ?: '') }" placeholder="Select date" autocomplete="off" />
                        </div>
                        <div class="transfer-wizard-field">
                            <label for="neonatalReferringSignedTime">Time</label>
                            <input type="text" class="js-time-picker" id="neonatalReferringSignedTime" name="referringSignedTime"
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
