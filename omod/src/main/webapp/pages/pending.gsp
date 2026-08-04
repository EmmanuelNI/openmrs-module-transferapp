<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("transferapp", "dashboard.css")
    ui.includeCss("transferapp", "styles/transferSection.css")
    ui.includeCss("transferapp", "styles/transferRecords.css")
    ui.includeCss("transferapp", "styles/transferFormPreview.css")
    ui.includeCss("uicommons", "styles/datatables/dataTables.jqueryui.css")
    ui.includeJavascript("uicommons", "scripts/datatables/jquery.dataTables.min.js")
    ui.includeJavascript("transferapp", "scripts/transferMohLogo.js")
    ui.includeJavascript("transferapp", "scripts/transferFormPreview.js")
    ui.includeJavascript("transferapp", "scripts/transferPending.js")
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
            %>
            <tr class="transfer-row"
                data-uuid="${ ui.encodeHtmlAttribute(uuid) }"
                data-upid="${ ui.encodeHtmlAttribute(upid) }">
                <td>${ ui.encodeHtmlContent((transfer.date ?: "") as String) }</td>
                <td>${ ui.encodeHtmlContent((transfer.clientName ?: "") as String) }</td>
                <td>${ ui.encodeHtmlContent(upid) }</td>
                <td>${ ui.encodeHtmlContent((transfer.origin ?: transfer.referringFacilityName ?: "") as String) }</td>
                <td>${ ui.encodeHtmlContent((transfer.receivingService ?: "") as String) }</td>
                <td>
                    <span class="transfer-status-pending">${ ui.encodeHtmlContent((transfer.status ?: "pending") as String) }</span>
                </td>
                <td>
                    <a class="transfer-pending-view-link"
                       href="javascript:void(0);"
                       data-uuid="${ ui.encodeHtmlAttribute(uuid) }"
                       data-upid="${ ui.encodeHtmlAttribute(upid) }">
                        <i class="icon-share-alt"></i> ${ ui.message("transferapp.patient.transfers.view") }
                    </a>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>
<% } %>
</div>
