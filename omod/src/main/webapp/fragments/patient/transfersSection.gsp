<%
ui.includeCss("transferapp", "styles/transferSection.css")
%>

<% if (!(canListTransfers || canCreateTransfer)) { %>
<div class="info-section">
    <div class="info-header">
        <i class="icon-retweet"></i>
        <h3>${ ui.message("transferapp.patient.transfers.title") }</h3>
    </div>
    <div class="info-body">
        <p>${ ui.encodeHtmlContent(accessDeniedMessage ?: ui.message("transferapp.patient.transfers.accessDenied", requiredListPrivilege, requiredCreatePrivilege)) }</p>
    </div>
</div>
<% } else { %>
<% if (canCreateTransfer) { %>
<div id="new-transfer-out-dialog" class="dialog transfer-wizard-dialog" style="display: none">
    <div class="dialog-header" style="display: none;">
        <i class="icon-retweet"></i>
        <h3 id="new-transfer-out-title">
            ${ ui.message("transferapp.patient.transfers.newTransferOut") }
        </h3>
    </div>
    <div class="dialog-content">
        <div id="new-transfer-out-data">
            <div style="padding: 10px;"><i class="icon-spinner icon-spin"></i> ${ ui.message("transferapp.patient.transfers.wizard.loading") }</div>
        </div>
    </div>
    <div class="dialog-footer">
        <button type="button" id="transfer-wizard-submit-btn" class="transfer-wizard-btn transfer-wizard-btn-primary">
            Save referral information
        </button>
        <button type="button" id="transfer-wizard-cancel" class="transfer-wizard-btn transfer-wizard-btn-outline">
            ${ ui.message("coreapps.cancel") }
        </button>
    </div>
</div>

<style>
#new-transfer-out-dialog.transfer-wizard-dialog.dialog {
  position: fixed !important;
  top: 50% !important;
  left: 50% !important;
  transform: translate(-50%, -50%) !important;
  z-index: 10000 !important;
  margin: 0 !important;
  padding: 0 !important;
  background: #eef2f6;
  border: 1px solid #d8e0ea;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.18);
  max-width: 95% !important;
  max-height: 94vh !important;
  width: 1100px !important;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

#new-transfer-out-dialog .dialog-content {
  padding: 0 !important;
  overflow: hidden;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

#new-transfer-out-data {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

#new-transfer-out-data .transfer-wizard-shell {
  min-height: 0;
  max-height: 100%;
  height: 100%;
  overflow: hidden;
}

#new-transfer-out-data .transfer-wizard-panel {
  padding-right: 0.35rem;
}

#new-transfer-out-dialog .dialog-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-top: 1px solid #e2e8f0;
  background: #fff;
}
</style>
<% } %>

<% if (canListTransfers) { %>
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

<style>
#transfer-preview-dialog .transfer-preview-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

#transfer-preview-dialog #transfer-preview-submit {
  background: #0f766e;
  color: #fff;
  border: 1px solid #0a5c52;
}

#transfer-preview-dialog #transfer-preview-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
#transfer-preview-dialog.transfer-preview-dialog.dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10001;
  background: white;
  border: 1px solid #00473f;
  border-radius: 4px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  max-width: 95%;
  max-height: 92vh;
  width: 1400px;
  display: flex;
  flex-direction: column;
  margin: 0;
}

#transfer-preview-dialog .dialog-header {
  padding: 15px 20px;
  border-bottom: 1px solid #00473f;
  background: #00473f;
  border-radius: 4px 4px 0 0;
  flex-shrink: 0;
  color: #fff;
}

#transfer-preview-dialog .dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

#transfer-preview-dialog .dialog-content {
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-height: 0;
}

#transfer-preview-body {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
  margin-bottom: 15px;
}
</style>
<% } %>

<div class="info-section transfer-section">
    <div class="info-header">
        <i class="icon-retweet"></i>
        <h3>${ ui.message(config.label ? config.label : "transferapp.patient.transfers.title").toUpperCase() }</h3>
        <div class="transfer-section-subtitle">
            ${ ui.message("transferapp.patient.transfers.subtitle") }
        </div>
    </div>
    <div class="info-body">
        <% if (canCreateTransfer && patientInsuranceAvailable) { %>
            <div class="transfer-new-btn-wrap">
                <a id="open-new-transfer-out"
                   class="transfer-new-btn"
                   href="javascript:void(0);"
                   role="button"
                   data-load-url="${ ui.encodeHtmlAttribute(ui.pageLink('transferapp', 'patient/newTransferOutForm') + '?patientId=' + patient.patient.patientId) }"
                   title="${ ui.encodeHtmlAttribute(ui.message('transferapp.patient.transfers.newTransferOut')) }">
                    ${ ui.message("transferapp.patient.transfers.newTransferOut") }
                </a>
                <span class="transfer-insurance-info">
                    <strong>${ ui.message("transferapp.patient.transfers.insurance") }:</strong>
                    ${ ui.encodeHtmlContent(patientInsuranceType) }
                    <span class="transfer-insurance-separator">|</span>
                    ${ ui.encodeHtmlContent(patientInsuranceNumber) }
                </span>
            </div>
        <% } %>

        <% if (hasTransfers) { %>
            <div class="transfer-table-wrapper">
                <table id="patient-transfers-table" class="transfer-datatable">
                    <thead>
                        <tr>
                            <th>${ ui.message("transferapp.patient.transfers.column.date") }</th>
                            <th>${ ui.message("transferapp.patient.transfers.column.from") }</th>
                            <th>${ ui.message("transferapp.patient.transfers.column.service") }</th>
                            <th>${ ui.message("transferapp.patient.transfers.column.status") }</th>
                            <th>${ ui.message("transferapp.patient.transfers.column.action") }</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% transfers.each { transfer -> %>
                            <tr class="transfer-row${ transfer.hieSent ? ' transfer-row-sent' : '' }" data-transfer-id="${ ui.encodeHtmlAttribute(transfer.id) }" data-hie-sent="${ transfer.hieSent ? 'true' : 'false' }">
                                <td>${ ui.format(transfer.transferDate) }</td>
                                <td>${ ui.format(transfer.fromFacility) }</td>
                                <td>${ ui.format(transfer.service) }</td>
                                <td>
                                    <% if (transfer.hieSent) { %>
                                        <span class="transfer-status-sent">${ ui.message("transferapp.patient.transfers.statusSent") }</span>
                                    <% } else { %>
                                        <span class="transfer-status-pending">${ ui.message("transferapp.patient.transfers.statusPending") }</span>
                                    <% } %>
                                </td>
                                <td>
                                    <a class="transfer-view-link"
                                       href="javascript:void(0);"
                                       data-transfer-id="${ ui.encodeHtmlAttribute(transfer.id) }"
                                       title="${ ui.encodeHtmlAttribute(ui.message('transferapp.patient.transfers.view')) }">
                                        <i class="icon-share-alt"></i> ${ ui.message("transferapp.patient.transfers.view") }
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
                <% if (hasMorePatientTransfers) { %>
                <div class="transfer-records-link-wrap">
                    <a href="${ ui.encodeHtmlAttribute(recordsPageUrl) }">
                        ${ ui.message("transferapp.records.viewAllForPatient", totalPatientTransfers) }
                    </a>
                </div>
                <% } %>
            </div>
        <% } %>
    </div>
</div>

<% if (canCreateTransfer) { %>
<script type="text/javascript">
    var newTransferOutDialog = null;
    var transferWizardOpenmrsPath = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "/${ ui.encodeJavaScript(contextPath) }");
    var transferWizardResourcesBase = transferWizardOpenmrsPath + "/moduleResources/transferapp/";
    var transferWizardScriptsLoading = null;

    function ensureTransferWizardAssets(callback) {
        if (!document.getElementById("transfer-wizard-flatpickr-css")) {
            jq("head").append(
                "<link id='transfer-wizard-flatpickr-css' rel='stylesheet' type='text/css' href='"
                + transferWizardResourcesBase + "styles/flatpickr.min.css' />"
            );
        }
        if (!document.getElementById("transfer-wizard-select2-css")) {
            jq("head").append(
                "<link id='transfer-wizard-select2-css' rel='stylesheet' type='text/css' href='"
                + transferWizardResourcesBase + "styles/select2.min.css' />"
            );
        }
        if (!document.getElementById("transfer-wizard-css")) {
            jq("head").append(
                "<link id='transfer-wizard-css' rel='stylesheet' type='text/css' href='"
                + transferWizardResourcesBase + "styles/transferWizard.css' />"
            );
        }

        var wizardReady = (typeof flatpickr === "function")
            && (typeof jq.fn.select2 === "function" || (typeof jQuery !== "undefined" && typeof jQuery.fn.select2 === "function"))
            && typeof initTransferWizardModal === "function";
        if (wizardReady) {
            callback();
            return;
        }
        if (transferWizardScriptsLoading) {
            transferWizardScriptsLoading.done(callback);
            return;
        }

        transferWizardScriptsLoading = jq.getScript(transferWizardResourcesBase + "scripts/flatpickr/flatpickr.min.js")
            .then(function() {
                return jq.getScript(transferWizardResourcesBase + "scripts/select2/select2.min.js");
            })
            .then(function() {
                if (typeof jq !== "undefined" && typeof jQuery !== "undefined"
                    && typeof jq.fn.select2 !== "function" && typeof jQuery.fn.select2 === "function") {
                    jq.fn.select2 = jQuery.fn.select2;
                }
                return jq.getScript(transferWizardResourcesBase + "scripts/transferWizardModal.js");
            })
            .done(function() {
                if (typeof initTransferWizardModal === "function") {
                    callback();
                } else {
                    jq("#new-transfer-out-data").html("<p style='color:red;'>${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.wizard.loadError')) }</p>");
                }
            })
            .fail(function() {
                jq("#new-transfer-out-data").html("<p style='color:red;'>${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.wizard.loadError')) }</p>");
            });
    }

    function closeNewTransferOutDialog() {
        try {
            if (newTransferOutDialog && typeof newTransferOutDialog.close === "function") {
                newTransferOutDialog.close();
            }
        } catch (ignoreClose) {}
        jq("#new-transfer-out-dialog").hide();
    }

    function showNewTransferOutDialog(loadUrl) {
        if (newTransferOutDialog == null && typeof emr !== "undefined" && typeof emr.setupConfirmationDialog === "function") {
            newTransferOutDialog = emr.setupConfirmationDialog({
                selector: "#new-transfer-out-dialog",
                actions: {
                    confirm: function() {},
                    cancel: function() { closeNewTransferOutDialog(); }
                }
            });
            newTransferOutDialog.close();
        }

        jq("#new-transfer-out-data").html("<div style='padding: 10px;'><i class='icon-spinner icon-spin'></i> ${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.wizard.loading')) }</div>");

        if (loadUrl) {
            var fullUrl = loadUrl;
            if (fullUrl.indexOf("http") !== 0) {
                fullUrl = window.location.origin + fullUrl;
            }
            jq("#new-transfer-out-data").load(fullUrl, function(response, status) {
                if (status === "error") {
                    jq("#new-transfer-out-data").html("<p style='color:red;'>${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.wizard.loadError')) }</p>");
                } else {
                    ensureTransferWizardAssets(function() {
                        initTransferWizardModal();
                    });
                }
            });
        }

        if (newTransferOutDialog) {
            newTransferOutDialog.show();
        } else {
            jq("#new-transfer-out-dialog").show();
        }
    }

    var transferSaveUrl = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "/${ ui.encodeJavaScript(contextPath) }")
        + "/module/transferapp/transfer/save.form";

    jq(document).ready(function() {
        jq(document).on("click", "#open-new-transfer-out", function(e) {
            e.preventDefault();
            var loadUrl = jq(this).attr("data-load-url");
            showNewTransferOutDialog(loadUrl);
        });

        jq(document).on("click", "#transfer-wizard-cancel", function(e) {
            e.preventDefault();
            closeNewTransferOutDialog();
        });

        jq(document).on("click", "#transfer-wizard-submit-btn", function(e) {
            e.preventDefault();
            var wizardForm = jq("#moh-transfer-wizard-form");
            if (wizardForm.length && !wizardForm[0].checkValidity()) {
                wizardForm[0].reportValidity();
                return;
            }

            var submitBtn = jq(this);
            submitBtn.prop("disabled", true);

            jq.ajax({
                url: transferSaveUrl,
                type: "POST",
                data: wizardForm.serialize(),
                dataType: "json"
            }).done(function(response) {
                if (response && response.status === "success") {
                    if (typeof emr !== "undefined" && typeof emr.successMessage === "function") {
                        emr.successMessage("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitSuccess')) }");
                    }
                    closeNewTransferOutDialog();
                    if (response.uuid) {
                        try {
                            sessionStorage.setItem("transferapp.previewTransferUuid", response.uuid);
                        } catch (ignoreStorage) {}
                    }
                    window.location.reload();
                    return;
                } else {
                    var message = response && response.message
                        ? response.message
                        : "${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitError')) }";
                    if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                        emr.errorMessage(message);
                    }
                }
            }).fail(function() {
                if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                    emr.errorMessage("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitError')) }");
                }
            }).always(function() {
                submitBtn.prop("disabled", false);
            });
        });
    });
</script>
<% } %>

<% if (canListTransfers) { %>
<script type="text/javascript">
    var transferOpenmrsPath = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "/${ ui.encodeJavaScript(contextPath) }");
    window.transferOpenmrsPath = transferOpenmrsPath;
    var transferPreviewUrl = transferOpenmrsPath + "/module/transferapp/transfer/preview.form";
    var transferSubmitUrl = transferOpenmrsPath + "/module/transferapp/transfer/submit.form";
    var transferPreviewResourcesBase = transferOpenmrsPath + "/moduleResources/transferapp/scripts/";
    var transferPreviewStorageKey = "transferapp.previewTransferUuid";
    var transferPreviewDialog = null;
    var transferPreviewScriptsLoading = null;
    var currentPreviewTransferUuid = null;
    var currentPreviewTransferSent = false;

    function syncTransferPreviewSubmitButton() {
        var submitBtn = jq("#transfer-preview-submit");
        if (!submitBtn.length) {
            return;
        }
        if (currentPreviewTransferSent) {
            submitBtn.prop("disabled", true).text("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.alreadySent')) }");
        } else {
            submitBtn.prop("disabled", false).text("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitToHie')) }");
        }
    }

    function ensureTransferPreviewRenderer(callback) {
        if (typeof buildTransferFormPreviewHtml === "function") {
            callback();
            return;
        }
        if (!document.getElementById("transfer-form-preview-css")) {
            jq("head").append(
                "<link id='transfer-form-preview-css' rel='stylesheet' type='text/css' href='"
                + transferOpenmrsPath + "/moduleResources/transferapp/styles/transferFormPreview.css' />"
            );
        }
        if (transferPreviewScriptsLoading) {
            transferPreviewScriptsLoading.done(callback);
            return;
        }
        transferPreviewScriptsLoading = jq.getScript(transferPreviewResourcesBase + "transferMohLogo.js")
            .then(function() {
                return jq.getScript(transferPreviewResourcesBase + "transferFormPreview.js");
            })
            .done(function() {
                if (typeof buildTransferFormPreviewHtml === "function") {
                    callback();
                } else {
                    jq("#transfer-preview-body").html("<p style='color:red;'>Preview renderer failed to initialize.</p>");
                }
            })
            .fail(function() {
                jq("#transfer-preview-body").html("<p style='color:red;'>Unable to load preview scripts.</p>");
            });
    }

    function renderTransferPreview(transfer) {
        var previewHtml = typeof buildTransferFormPreviewHtml === "function"
            ? buildTransferFormPreviewHtml(transfer)
            : "<p style='color:red;'>Preview renderer not loaded.</p>";
        jq("#transfer-preview-body").html(previewHtml);
        currentPreviewTransferSent = !!(transfer && (transfer.hieSent === true || transfer.hieSent === "true"));
        syncTransferPreviewSubmitButton();
    }

    function showTransferPreviewDialog() {
        if (transferPreviewDialog == null && typeof emr !== "undefined" && typeof emr.setupConfirmationDialog === "function") {
            transferPreviewDialog = emr.setupConfirmationDialog({
                selector: "#transfer-preview-dialog",
                actions: {
                    confirm: function() {},
                    cancel: function() { jq("#transfer-preview-dialog").hide(); }
                }
            });
            transferPreviewDialog.close();
        }
        if (transferPreviewDialog) {
            transferPreviewDialog.show();
        } else {
            jq("#transfer-preview-dialog").show();
        }
    }

    function showTransferPreview(transferUuid) {
        if (!transferUuid) {
            return;
        }

        jq("#transfer-preview-body").html("<div style='padding: 10px;'><i class='icon-spinner icon-spin'></i> ${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.previewLoading')) }</div>");
        currentPreviewTransferUuid = transferUuid;
        currentPreviewTransferSent = false;
        syncTransferPreviewSubmitButton();
        showTransferPreviewDialog();

        jq.ajax({
            url: transferPreviewUrl,
            type: "GET",
            data: { uuid: transferUuid },
            dataType: "json"
        }).done(function(response) {
            if (response && response.status === "success" && response.transfer) {
                ensureTransferPreviewRenderer(function() {
                    renderTransferPreview(response.transfer);
                });
                return;
            }
            var message = response && response.message
                ? response.message
                : "${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.previewError')) }";
            var esc = typeof escTransferPreview === "function" ? escTransferPreview : function(v) { return v || ""; };
            jq("#transfer-preview-body").html("<p style='color:red;'>" + esc(message) + "</p>");
        }).fail(function() {
            jq("#transfer-preview-body").html("<p style='color:red;'>${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.previewError')) }</p>");
        });
    }

    jq(document).ready(function() {
        jq(document).on("click", "#transfer-preview-submit", function(e) {
            e.preventDefault();
            if (!currentPreviewTransferUuid || currentPreviewTransferSent) {
                return;
            }
            var submitBtn = jq(this);
            submitBtn.prop("disabled", true);
            jq.ajax({
                url: transferSubmitUrl,
                type: "POST",
                data: { uuid: currentPreviewTransferUuid },
                dataType: "json"
            }).done(function(response) {
                if (response && response.status === "success") {
                    if (typeof emr !== "undefined" && typeof emr.successMessage === "function") {
                        emr.successMessage("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitToHieSuccess')) }");
                    }
                    window.location.reload();
                    return;
                }
                var message = response && response.message
                    ? response.message
                    : "${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitToHieError')) }";
                if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                    emr.errorMessage(message);
                }
                syncTransferPreviewSubmitButton();
            }).fail(function() {
                if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                    emr.errorMessage("${ ui.encodeJavaScript(ui.message('transferapp.patient.transfers.submitToHieError')) }");
                }
                syncTransferPreviewSubmitButton();
            });
        });

        jq(document).on("click", "#transfer-preview-close", function(e) {
            e.preventDefault();
            if (transferPreviewDialog && typeof transferPreviewDialog.close === "function") {
                transferPreviewDialog.close();
            }
            jq("#transfer-preview-dialog").hide();
        });

        jq(document).on("click", ".transfer-view-link", function(e) {
            e.preventDefault();
            var transferUuid = jq(this).attr("data-transfer-id")
                || jq(this).closest("tr.transfer-row").attr("data-transfer-id");
            showTransferPreview(transferUuid);
        });

        var pendingPreviewUuid = null;
        try {
            pendingPreviewUuid = sessionStorage.getItem(transferPreviewStorageKey);
        } catch (ignoreStorage) {}
        if (pendingPreviewUuid) {
            try {
                sessionStorage.removeItem(transferPreviewStorageKey);
            } catch (ignoreStorage) {}
            showTransferPreview(pendingPreviewUuid);
        }
    });
</script>
<% } %>
<% } %>
