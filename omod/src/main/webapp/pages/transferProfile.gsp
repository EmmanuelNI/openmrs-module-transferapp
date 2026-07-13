<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("transferapp", "dashboard.css")
    ui.includeCss("transferapp", "transferProfile.css")
    ui.includeJavascript("transferapp", "transferProfile.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("transferapp.nav.profile") }" }
    ];
</script>

<div class="transfer-profile">
${ ui.includeFragment("transferapp", "transfer/transferNav", [ activeTab: "profile", app: "transferapp.dashboard" ]) }

<div class="transfer-profile-card">
    <div class="transfer-profile-meta">
        <div><strong>${ ui.message("transferapp.profile.providerName") }:</strong> ${ ui.encodeHtmlContent(providerDisplayName ?: '') }</div>
        <div><strong>${ ui.message("transferapp.profile.username") }:</strong> ${ ui.encodeHtmlContent(username ?: '') }</div>
    </div>

    <form id="transfer-profile-form" class="transfer-profile-form">
        <div class="transfer-profile-field">
            <label for="licenseNumber">${ ui.message("transferapp.profile.licenseNumber") }</label>
            <input type="text" id="licenseNumber" name="licenseNumber" maxlength="64" required
                   value="${ ui.encodeHtmlAttribute(licenseNumber ?: '') }" />
        </div>
        <div class="transfer-profile-field">
            <label for="phoneNumber">${ ui.message("transferapp.profile.phoneNumber") }</label>
            <input type="tel" id="phoneNumber" name="phoneNumber" maxlength="64" required
                   value="${ ui.encodeHtmlAttribute(phoneNumber ?: '') }" />
        </div>
        <div class="transfer-profile-field">
            <label for="qualification">${ ui.message("transferapp.profile.qualification") }</label>
            <input type="text" id="qualification" name="qualification" maxlength="255" required
                   value="${ ui.encodeHtmlAttribute(qualification ?: '') }" />
        </div>

        <div class="transfer-profile-actions">
            <button type="submit" class="btn btn-primary">${ ui.message("transferapp.profile.save") }</button>
        </div>
    </form>
</div>
</div>

<script type="text/javascript">
    window.transferProfileConfig = {
        saveUrl: (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "/${ ui.encodeJavaScript(contextPath) }")
            + "/module/transferapp/profile/save.form",
        messages: {
            saveSuccess: "${ ui.encodeJavaScript(ui.message('transferapp.profile.saveSuccess')) }",
            saveError: "${ ui.encodeJavaScript(ui.message('transferapp.profile.saveError')) }"
        }
    };
</script>
