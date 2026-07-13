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
    ui.includeJavascript("transferapp", "scripts/transferRecords.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("transferapp.records.title") }" }
    ];
</script>

<div class="transfer-records-page">
${ ui.includeFragment("transferapp", "transfer/transferNav", [ activeTab: "records", app: appId ]) }

<h3 class="transfer-records-title">${ ui.message("transferapp.records.title") }</h3>
<p class="transfer-records-intro">${ ui.message("transferapp.records.description") }</p>

<% if (filteredPatientId != null && filteredPatientName != null) { %>
<p class="transfer-records-filter">
    ${ ui.message("transferapp.records.filteredByPatient") }:
    <strong>${ ui.encodeHtmlContent(filteredPatientName) }</strong>
    <a href="${ ui.pageLink('transferapp', 'records') }?app=${ ui.encodeHtmlAttribute(appId) }">
        ${ ui.message("transferapp.records.clearFilter") }
    </a>
</p>
<% } %>

<% if (!canListTransfers) { %>
<div class="transfer-records-empty">${ ui.message("transferapp.patient.transfers.listNotAllowed") }</div>
<% } else if (!hasRecords) { %>
<div class="transfer-records-empty">${ ui.message("transferapp.records.empty") }</div>
<% } else { %>

<div id="transfer-preview-dialog" class="dialog transfer-preview-dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-retweet"></i>
        <h3>${ ui.message("transferapp.patient.transfers.previewTitle") }</h3>
    </div>
    <div class="dialog-content">
        <div id="transfer-preview-body"></div>
        <div class="transfer-preview-actions">
            <% if (canCreateTransfer) { %>
            <button type="button" id="transfer-preview-submit" class="confirm">
                ${ ui.message("transferapp.patient.transfers.submitToHie") }
            </button>
            <% } %>
            <button type="button" id="transfer-preview-close" class="cancel">
                ${ ui.message("coreapps.close") }
            </button>
        </div>
    </div>
</div>

<div class="transfer-table-wrapper">
    <table id="transfer-records-table" class="transfer-datatable display">
        <thead>
            <tr>
                <th>${ ui.message("transferapp.patient.transfers.column.date") }</th>
                <th>${ ui.message("transferapp.records.column.patient") }</th>
                <th>${ ui.message("transferapp.records.column.emrId") }</th>
                <th>${ ui.message("transferapp.records.column.to") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.service") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.status") }</th>
                <th>${ ui.message("transferapp.patient.transfers.column.action") }</th>
            </tr>
        </thead>
        <tbody>
            <% records.each { record -> %>
            <tr class="transfer-row${ record.hieSent ? ' transfer-row-sent' : '' }" data-transfer-id="${ ui.encodeHtmlAttribute(record.id) }">
                <td>${ ui.format(record.transferDate) }</td>
                <td>${ ui.format(record.clientName) }</td>
                <td>${ ui.format(record.emrId) }</td>
                <td>${ ui.format(record.receivingFacility) }</td>
                <td>${ ui.format(record.service) }</td>
                <td>
                    <% if (record.hieSent) { %>
                        <span class="transfer-status-sent">${ ui.message("transferapp.patient.transfers.statusSent") }</span>
                    <% } else { %>
                        <span class="transfer-status-pending">${ ui.message("transferapp.patient.transfers.statusPending") }</span>
                    <% } %>
                </td>
                <td>
                    <a class="transfer-view-link"
                       href="javascript:void(0);"
                       data-transfer-id="${ ui.encodeHtmlAttribute(record.id) }">
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
