<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("transferapp", "dashboard.css")
    ui.includeCss("transferapp", "transferSection.css")
    ui.includeCss("transferapp", "transferRecords.css")
    ui.includeCss("transferapp", "ambulanceVoucherPreview.css")
    ui.includeCss("transferapp", "flatpickr.min.css")
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("transferapp", "flatpickr/flatpickr.min.js")
    ui.includeJavascript("transferapp", "transferAmbulanceVoucher.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("transferapp.ambulanceVoucher.title") }" }
    ];
    var openmrsContextPath = (typeof openmrsContextPath !== "undefined" && openmrsContextPath)
        ? openmrsContextPath
        : "/${ ui.encodeJavaScript(contextPath) }";
    window.transferOpenmrsPath = openmrsContextPath;
    window.transferAmbulanceVoucherFilterConfig = {
        startDate: "${ ui.encodeJavaScript(filterStartDate ?: '') }",
        endDate: "${ ui.encodeJavaScript(filterEndDate ?: '') }",
        maxDateRangeMonths: ${ maxDateRangeMonths ?: 3 },
        previewUrl: openmrsContextPath + "/module/transferapp/transfer/ambulanceVoucherPreview.form",
        messages: {
            dateRangeError: "${ ui.encodeJavaScript(ui.message('transferapp.ambulanceVoucher.filter.dateRangeError', maxDateRangeMonths ?: 3)) }",
            invalidDateRange: "${ ui.encodeJavaScript(ui.message('transferapp.ambulanceVoucher.filter.invalidDateRange')) }",
            previewLoading: "${ ui.encodeJavaScript(ui.message('transferapp.ambulanceVoucher.preview.loading')) }",
            previewError: "${ ui.encodeJavaScript(ui.message('transferapp.ambulanceVoucher.preview.error')) }",
            previewClose: "${ ui.encodeJavaScript(ui.message('coreapps.close')) }",
            previewPrint: "${ ui.encodeJavaScript(ui.message('transferapp.ambulanceVoucher.preview.print')) }"
        }
    };
</script>

<div class="transfer-records-page transfer-ambulance-voucher-page">
${ ui.includeFragment("transferapp", "transfer/transferNav", [ activeTab: "ambulanceVoucher", app: appId ]) }

<h3 class="transfer-records-title">${ ui.message("transferapp.ambulanceVoucher.title") }</h3>
<p class="transfer-history-description">${ ui.message("transferapp.ambulanceVoucher.description") }</p>

<% if (!canListTransfers) { %>
<div class="transfer-records-empty">${ ui.encodeHtmlContent(listAccessDeniedMessage ?: ui.message("transferapp.patient.transfers.listNotAllowed")) }</div>
<% } else { %>

<form id="transfer-ambulance-voucher-filter-form"
      class="transfer-records-filters"
      method="get"
      action="${ ui.pageLink('transferapp', 'ambulanceVoucher') }">
    <input type="hidden" name="app" value="${ ui.encodeHtmlAttribute(appId) }" />

    <div class="transfer-ambulance-voucher-filters-grid">
        <div class="transfer-records-filter-field">
            <label for="ambulance-voucher-filter-start-date">${ ui.message("transferapp.ambulanceVoucher.filter.startDate") }</label>
            <input type="text"
                   id="ambulance-voucher-filter-start-date"
                   name="startDate"
                   class="transfer-records-date-input"
                   value="${ ui.encodeHtmlAttribute(filterStartDate ?: '') }"
                   autocomplete="off" />
        </div>
        <div class="transfer-records-filter-field">
            <label for="ambulance-voucher-filter-end-date">${ ui.message("transferapp.ambulanceVoucher.filter.endDate") }</label>
            <input type="text"
                   id="ambulance-voucher-filter-end-date"
                   name="endDate"
                   class="transfer-records-date-input"
                   value="${ ui.encodeHtmlAttribute(filterEndDate ?: '') }"
                   autocomplete="off" />
        </div>
        <div class="transfer-records-filter-actions">
            <button type="submit" id="ambulance-voucher-filter-apply" class="confirm">
                ${ ui.message("transferapp.ambulanceVoucher.filter.apply") }
            </button>
        </div>
    </div>
    <p id="ambulance-voucher-filter-error" class="transfer-records-filter-error" style="display:none;"></p>
</form>

<div id="ambulance-voucher-preview-overlay" class="ambulance-voucher-preview-overlay" style="display:none;"></div>
<div id="ambulance-voucher-preview-dialog" class="dialog ambulance-voucher-preview-dialog" style="display:none;">
    <div class="dialog-header">
        <i class="icon-file-alt"></i>
        <h3>${ ui.message("transferapp.ambulanceVoucher.preview.title") }</h3>
    </div>
    <div class="dialog-content">
        <div id="ambulance-voucher-preview-body"></div>
        <div class="ambulance-voucher-preview-actions">
            <button type="button" id="ambulance-voucher-preview-print" class="confirm">
                ${ ui.message("transferapp.ambulanceVoucher.preview.print") }
            </button>
            <button type="button" id="ambulance-voucher-preview-close" class="cancel">
                ${ ui.message("coreapps.close") }
            </button>
        </div>
    </div>
</div>

<% if (!hasVouchers) { %>
<div class="transfer-records-empty">${ ui.message("transferapp.ambulanceVoucher.empty") }</div>
<% } else { %>
<div class="transfer-table-wrapper">
    <table id="transfer-ambulance-voucher-table" class="transfer-datatable display">
        <thead>
            <tr>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.number") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.date") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.upid") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.patient") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.from") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.destination") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.distance") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.amount") }</th>
                <th>${ ui.message("transferapp.ambulanceVoucher.column.action") }</th>
            </tr>
        </thead>
        <tbody>
            <% vouchers.each { voucher -> %>
            <tr>
                <td>${ voucher.rowNumber }</td>
                <td>${ ui.format(voucher.transferDate) }</td>
                <td>${ ui.encodeHtmlContent(voucher.patientUpid ?: '') }</td>
                <td>${ ui.encodeHtmlContent(voucher.patientName ?: '') }</td>
                <td>${ ui.encodeHtmlContent(voucher.fromHospital ?: '') }</td>
                <td>${ ui.encodeHtmlContent(voucher.destinationHospital ?: '') }</td>
                <td>${ voucher.distance != null ? voucher.distance : '' }</td>
                <td>${ voucher.amount != null ? voucher.amount : '' }</td>
                <td>
                    <a class="ambulance-voucher-preview-link"
                       href="javascript:void(0);"
                       data-uuid="${ ui.encodeHtmlAttribute(voucher.transferUuid ?: '') }"
                       title="${ ui.encodeHtmlAttribute(ui.message('transferapp.ambulanceVoucher.preview.action')) }">
                        <i class="icon-eye-open"></i> ${ ui.message("transferapp.ambulanceVoucher.preview.action") }
                    </a>
                </td>
            </tr>
            <% } %>
        </tbody>
    </table>
</div>
<% } %>
<% } %>
</div>
