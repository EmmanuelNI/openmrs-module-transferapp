<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("transferapp", "dashboard.css")
    ui.includeCss("transferapp", "transferSection.css")
    ui.includeCss("transferapp", "transferRecords.css")
    ui.includeCss("transferapp", "transferFormPreview.css")
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("transferapp", "transferMohLogo.js")
    ui.includeJavascript("transferapp", "transferFormPreview.js")
    ui.includeJavascript("transferapp", "transferPending.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("transferapp.pending.title") }" }
    ];
</script>

<div class="transfer-records-page">
${ ui.includeFragment("transferapp", "transfer/transferNav", [ activeTab: "pending", app: appId ]) }

<h3 class="transfer-records-title">${ ui.message("transferapp.pending.title") }</h3>
<p class="transfer-records-intro">${ ui.message("transferapp.pending.description") }</p>

<% if (targetOrg) { %>
<p class="transfer-records-filter">
    ${ ui.message("transferapp.pending.targetOrg") }:
    <strong>${ ui.encodeHtmlContent(targetOrg) }</strong>
</p>
<% } %>

<% if (!canListPending) { %>
<div class="transfer-records-empty">${ ui.encodeHtmlContent(pendingAccessDeniedMessage ?: ui.message("transferapp.pending.notAllowed")) }</div>
<% } else if (pendingErrorMessage) { %>
<div class="transfer-records-empty" style="color:#a94442;">${ ui.encodeHtmlContent(pendingErrorMessage) }</div>
<% } else if (!hasPendingTransfers) { %>
<div class="transfer-records-empty">${ ui.message("transferapp.pending.empty") }</div>
<% } else { %>

<div id="transfer-preview-dialog" class="dialog transfer-preview-dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-retweet"></i>
        <h3>${ ui.message("transferapp.patient.transfers.previewTitle") }</h3>
    </div>
    <div class="dialog-content">
        <div id="transfer-preview-body"></div>
        <div class="transfer-preview-actions">
            <button type="button" id="transfer-preview-close" class="cancel">
                ${ ui.message("coreapps.close") }
            </button>
        </div>
    </div>
</div>

<div class="transfer-table-wrapper">
    <table id="transfer-pending-table" class="transfer-datatable display">
        <thead>
            <tr>
                <th>${ ui.message("transferapp.patient.transfers.column.date") }</th>
                <th>${ ui.message("transferapp.records.column.patient") }</th>
                <th>${ ui.message("transferapp.pending.column.upid") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.from") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.service") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.status") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.action") }</th>
            </tr>
        </thead>
        <tbody>
            <% pendingTransfers.each { transfer ->
                def uuid = (transfer.uuid ?: transfer.id ?: "") as String
                def upid = (transfer.upid ?: transfer.subject ?: "") as String
                def existingPatient = transfer.existingPatient == true
                def existingPatientReference = (transfer.existingPatientReference ?: "") as String
                def pendingReturnUrl = ui.pageLinkWithoutContextPath("transferapp", "pending", [app: appId])
                def agentRejected = transfer.agentRejected == true || transfer.agentRejected == "true"
                def agentApproved = transfer.agentDecisionApproved == true || transfer.agentDecisionApproved == "true"
                def needsApproval = transfer.needsInsuranceApproval == true || transfer.needsInsuranceApproval == "true"
                def statusClass = agentRejected ? "transfer-status-rejected"
                        : (agentApproved ? "transfer-status-approved"
                        : (needsApproval ? "transfer-status-awaiting" : "transfer-status-pending"))
                def statusLabel = (transfer.status ?: "pending") as String
            %>
            <tr class="transfer-row"
                data-uuid="${ ui.encodeHtmlAttribute(uuid) }"
                data-upid="${ ui.encodeHtmlAttribute(upid) }"
                data-agent-approved="${ ui.encodeHtmlAttribute(String.valueOf(transfer.agentApproved == true || transfer.agentApproved == 'true')) }"
                data-agent-comment="${ ui.encodeHtmlAttribute((transfer.agentComment ?: '') as String) }">
                <td>${ ui.encodeHtmlContent((transfer.date ?: "") as String) }</td>
                <td>
                    <span>${ ui.encodeHtmlContent((transfer.clientName ?: "") as String) }</span>
                    <span class="transfer-patient-badge ${ existingPatient ? 'transfer-patient-badge-existing' : 'transfer-patient-badge-new' }">
                        ${ existingPatient ? ui.message("transferapp.pending.patient.existing") : ui.message("transferapp.pending.patient.new") }
                    </span>
                </td>
                <td>${ ui.encodeHtmlContent(upid) }</td>
                <td>${ ui.encodeHtmlContent((transfer.origin ?: transfer.referringFacilityName ?: "") as String) }</td>
                <td>${ ui.encodeHtmlContent((transfer.receivingService ?: "") as String) }</td>
                <td>
                    <span class="${ statusClass }">${ ui.encodeHtmlContent(statusLabel) }</span>
                </td>
                <td>
                    <div class="transfer-pending-actions">
                        <a class="transfer-pending-view-link"
                           href="javascript:void(0);"
                           data-uuid="${ ui.encodeHtmlAttribute(uuid) }"
                           data-upid="${ ui.encodeHtmlAttribute(upid) }">
                            <i class="icon-share-alt"></i> ${ ui.message("transferapp.patient.transfers.view") }
                        </a>
                        <% if (existingPatient && existingPatientReference) { %>
                            <a class="button transfer-pending-workflow-button"
                               href="${ ui.pageLink(rwandaEmrModuleId, requestAppointmentPage, [patientId: existingPatientReference]) }">
                                <i class="icon-calendar"></i> ${ ui.message("transferapp.pending.action.schedule") }
                            </a>
                        <% } else if (!existingPatient) { %>
                            <a class="button confirm transfer-pending-workflow-button"
                               href="${ ui.pageLink('transferapp', 'registerPatientFromHie', [upid: upid, returnUrl: pendingReturnUrl]) }">
                                <i class="icon-user"></i> ${ ui.message("transferapp.pending.action.register") }
                            </a>
                        <% } %>
                    </div>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>
<% } %>
</div>
