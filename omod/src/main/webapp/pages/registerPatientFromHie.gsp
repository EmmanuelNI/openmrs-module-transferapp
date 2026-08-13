<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("transferapp", "hiePatientPreview.css")
    def missingValue = ui.message("transferapp.pending.registration.preview.notProvided")
    def showValue = { value -> value == null || value.toString().trim().isEmpty() ? missingValue : value.toString() }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.encodeJavaScript(ui.message('transferapp.pending.title')) }", link: "${ ui.encodeJavaScript(cancelUrl) }" },
        { label: "${ ui.encodeJavaScript(ui.message('transferapp.pending.registration.preview.title')) }" }
    ];
</script>

<div class="transfer-hie-preview">
    <header class="transfer-hie-preview-header">
        <div>
            <h1>${ ui.message("transferapp.pending.registration.preview.title") }</h1>
            <p>${ ui.message("transferapp.pending.registration.preview.subtitle") }</p>
        </div>
        <span class="transfer-hie-preview-upid">${ ui.encodeHtmlContent(upid) }</span>
    </header>

    <section class="transfer-hie-preview-section" aria-labelledby="preview-identifiers-title">
        <h2 id="preview-identifiers-title">${ ui.message("transferapp.pending.registration.preview.identifiers") }</h2>
        <dl class="transfer-hie-preview-grid">
            <div><dt>${ ui.message("transferapp.pending.registration.preview.nationalId") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.nationalId)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.upid") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.upid)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.applicationNumber") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.applicationNumber)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.nin") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.nin)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.passportNumber") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.passportNumber)) }</dd></div>
        </dl>
    </section>

    <section class="transfer-hie-preview-section" aria-labelledby="preview-demographics-title">
        <h2 id="preview-demographics-title">${ ui.message("transferapp.pending.registration.preview.demographics") }</h2>
        <dl class="transfer-hie-preview-grid">
            <div><dt>${ ui.message("transferapp.pending.registration.preview.givenName") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.givenName)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.middleName") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.middleName)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.familyName") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.familyName)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.gender") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.gender)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.birthdate") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.birthdate)) }</dd></div>
        </dl>
    </section>

    <section class="transfer-hie-preview-section" aria-labelledby="preview-contact-title">
        <h2 id="preview-contact-title">${ ui.message("transferapp.pending.registration.preview.contact") }</h2>
        <dl class="transfer-hie-preview-grid">
            <div><dt>${ ui.message("transferapp.pending.registration.preview.phoneNumber") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.phoneNumber)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.country") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.country)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.province") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.stateProvince)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.district") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.countyDistrict)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.sector") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.cityVillage)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.cell") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.address3)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.village") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.address1)) }</dd></div>
        </dl>
    </section>

    <section class="transfer-hie-preview-section" aria-labelledby="preview-attributes-title">
        <h2 id="preview-attributes-title">${ ui.message("transferapp.pending.registration.preview.attributes") }</h2>
        <dl class="transfer-hie-preview-grid">
            <div><dt>${ ui.message("transferapp.pending.registration.preview.mothersName") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.mothersName)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.fathersName") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.fathersName)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.education") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.educationLevel)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.profession") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.profession)) }</dd></div>
            <div><dt>${ ui.message("transferapp.pending.registration.preview.religion") }</dt><dd>${ ui.encodeHtmlContent(showValue(patientDetails.religion)) }</dd></div>
        </dl>
    </section>

    <div class="transfer-hie-preview-actions">
        <a class="button cancel" href="${ ui.encodeHtmlAttribute(cancelUrl) }">
            ${ ui.message("transferapp.pending.registration.preview.cancel") }
        </a>
        <form method="post" action="${ ui.pageLink('transferapp', 'registerPatientFromHie') }">
            <input type="hidden" name="upid" value="${ ui.encodeHtmlAttribute(upid) }" />
            <input type="hidden" name="returnUrl" value="${ ui.encodeHtmlAttribute(returnUrl) }" />
            <button type="submit" class="button confirm">
                <i class="icon-ok"></i> ${ ui.message("transferapp.pending.registration.preview.confirm") }
            </button>
        </form>
    </div>
</div>
