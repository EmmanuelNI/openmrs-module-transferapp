(function(jq) {
    if (window.__hieTransferSectionBound) {
        return;
    }
    window.__hieTransferSectionBound = true;

    function normalizeRootUrl(path) {
        var p = String(path || "");
        if (p.indexOf("http://") === 0 || p.indexOf("https://") === 0) {
            return p;
        }
        while (p.indexOf("//") === 0) {
            p = p.substring(1);
        }
        if (p.charAt(0) !== "/") {
            p = "/" + p;
        }
        while (p.indexOf("//") !== -1) {
            p = p.split("//").join("/");
        }
        return p;
    }

    function esc(value) {
        if (value === null || value === undefined) {
            return "";
        }
        return String(value)
            .split("&").join("&amp;")
            .split("<").join("&lt;")
            .split(">").join("&gt;")
            .split("\"").join("&quot;");
    }

    function resolveDestination(item) {
        if (!item) {
            return "";
        }
        return item.destinationDisplay || item.destination || item.receivingFacility || item.hospitalName || "";
    }

    function targetsCurrentFacility(item) {
        if (!item) {
            return false;
        }
        if (item.targetsCurrentFacility === true || item.targetsCurrentFacility === "true") {
            return true;
        }
        return false;
    }

    function initHieTransferSection() {
        var section = jq("#hie-transfer-section");
        if (!section.length) {
            return;
        }

        var canList = section.attr("data-can-list") === "true";
        if (!canList) {
            return;
        }

        var transferOpenmrsPath = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "");
        var restUrl = normalizeRootUrl(
            section.attr("data-rest-url")
            || (transferOpenmrsPath + "/ws/rest/v1/transferapp/transfer")
        );
        var validateUrl = normalizeRootUrl(
            section.attr("data-validate-url")
            || (transferOpenmrsPath + "/ws/rest/v1/transferapp/transfer/validate")
        );
        var upid = section.attr("data-upid") || "";
        var patientId = section.attr("data-patient-id") || "";
        var hasTransferId = section.attr("data-has-transfer-id") === "true";
        var listFromHie = section.attr("data-list-from-hie") === "true";
        var showSection = section.attr("data-show-section") === "true";
        var canValidate = section.attr("data-can-validate") === "true" && listFromHie && !hasTransferId;
        var previewResourcesBase = normalizeRootUrl(transferOpenmrsPath + "/moduleResources/transferapp/scripts/");
        var previewDialog = null;
        var previewScriptsLoading = null;
        var currentPreviewTransfer = null;

        function ensureTransferPreviewRenderer(callback) {
            if (typeof buildTransferFormPreviewHtml === "function") {
                callback();
                return;
            }
            if (previewScriptsLoading) {
                previewScriptsLoading.done(callback);
                return;
            }
            previewScriptsLoading = jq.getScript(previewResourcesBase + "transferMohLogo.js")
                .then(function() {
                    return jq.getScript(previewResourcesBase + "transferFormPreview.js");
                })
                .done(callback)
                .fail(function() {
                    jq("#hie-transfer-preview-body").html("<p style='color:red;'>Unable to load transfer preview renderer.</p>");
                });
        }

        function showPreviewDialog() {
            var dialogEl = jq("#hie-transfer-preview-dialog");
            if (dialogEl.length && dialogEl.parent()[0] !== document.body) {
                dialogEl.appendTo(document.body);
            }
            if (previewDialog == null && typeof emr !== "undefined" && typeof emr.setupConfirmationDialog === "function") {
                previewDialog = emr.setupConfirmationDialog({
                    selector: "#hie-transfer-preview-dialog",
                    actions: {
                        confirm: function() {},
                        cancel: function() {
                            dialogEl.hide();
                        }
                    }
                });
                if (previewDialog && typeof previewDialog.close === "function") {
                    previewDialog.close();
                }
            }
            if (previewDialog && typeof previewDialog.show === "function") {
                previewDialog.show();
            } else {
                dialogEl.show();
            }
        }

        function hidePreviewDialog() {
            if (previewDialog && typeof previewDialog.close === "function") {
                previewDialog.close();
            }
            jq("#hie-transfer-preview-dialog").hide();
        }

        function updateValidateButton(transfer) {
            var validateBtn = jq("#hie-transfer-validate-btn");
            var exportBtn = jq("#hie-transfer-export-pdf-btn");
            var validateStatus = jq("#hie-transfer-validate-status");
            validateStatus.hide().text("");
            currentPreviewTransfer = transfer || null;

            var transferUuid = transfer
                ? (transfer.uuid || transfer.id || transfer.hieTransferId || "")
                : "";
            var previewLoaded = !!(transfer && transferUuid);

            if (hasTransferId && previewLoaded) {
                exportBtn.show().prop("disabled", false);
            } else {
                exportBtn.hide().prop("disabled", false);
            }

            if (!canValidate || !transfer || !targetsCurrentFacility(transfer)) {
                validateBtn.hide().prop("disabled", false).text(validateBtn.data("default-label") || validateBtn.text());
                return;
            }

            if (!transferUuid) {
                validateBtn.hide();
                return;
            }

            if (!validateBtn.data("default-label")) {
                validateBtn.data("default-label", validateBtn.text());
            }
            validateBtn
                .show()
                .prop("disabled", false)
                .text(validateBtn.data("default-label"))
                .attr("data-transfer-id", transferUuid);
        }

        function exportCurrentPreviewPdf() {
            if (!hasTransferId || !currentPreviewTransfer) {
                return;
            }
            var transferUuid = currentPreviewTransfer.uuid
                || currentPreviewTransfer.id
                || currentPreviewTransfer.hieTransferId
                || "transfer";
            var ok = typeof exportTransferFormPreviewPdf === "function"
                && exportTransferFormPreviewPdf("#hie-transfer-preview-body", {
                    fileName: "External-Transfer-Form-" + transferUuid
                });
            if (!ok) {
                jq("#hie-transfer-validate-status").show().css("color", "#a94442")
                    .text("Unable to open PDF export. Allow pop-ups and try again.");
            }
        }

        function renderPreview(transfer) {
            var previewHtml = typeof buildTransferFormPreviewHtml === "function"
                ? buildTransferFormPreviewHtml(transfer)
                : "<p style='color:red;'>Preview renderer not loaded.</p>";
            jq("#hie-transfer-preview-body").html(previewHtml);
            updateValidateButton(transfer);
        }

        function loadTransferPreview(transferId, patientUpid) {
            if (!transferId || !patientUpid) {
                jq("#hie-transfer-preview-body").html("<p style='color:red;'>Missing transfer UUID or UPID.</p>");
                updateValidateButton(null);
                showPreviewDialog();
                return;
            }

            jq("#hie-transfer-preview-body").html(
                "<div style='padding:10px;'><i class='icon-spinner icon-spin'></i> Loading transfer information...</div>"
            );
            updateValidateButton(null);
            showPreviewDialog();

            jq.ajax({
                url: restUrl,
                type: "GET",
                data: {
                    upid: patientUpid,
                    transferId: transferId,
                    activeOnly: false
                },
                dataType: "json",
                headers: {
                    "Accept": "application/json"
                }
            }).done(function(response) {
                if (typeof response === "string") {
                    try {
                        response = jq.parseJSON(response);
                    } catch (err) {
                        jq("#hie-transfer-preview-body").html("<p style='color:red;'>Transfer endpoint returned non-JSON response.</p>");
                        updateValidateButton(null);
                        return;
                    }
                }
                if (response && response.status === "error") {
                    jq("#hie-transfer-preview-body").html(
                        "<p style='color:red;'>" + esc(response.message || "Unable to load transfer.") + "</p>"
                    );
                    updateValidateButton(null);
                    return;
                }
                var items = response && response.data ? response.data : [];
                if (items.length) {
                    ensureTransferPreviewRenderer(function() {
                        renderPreview(items[0]);
                    });
                    return;
                }
                jq("#hie-transfer-preview-body").html("<p style='color:red;'>No matching transfer found in HIE.</p>");
                updateValidateButton(null);
            }).fail(function(xhr) {
                var message = "Unable to load transfer details.";
                if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                } else if (xhr && xhr.responseText) {
                    try {
                        var parsed = jq.parseJSON(xhr.responseText);
                        if (parsed && parsed.message) {
                            message = parsed.message;
                        }
                    } catch (ignore) {}
                }
                jq("#hie-transfer-preview-body").html("<p style='color:red;'>" + esc(message) + "</p>");
                updateValidateButton(null);
            });
        }

        function transferListRowHtml(item) {
            var uuid = item.uuid || item.id || "";
            var date = item.date || item.transferDecisionDatetime || item.admissionDatetime || "";
            var from = item.origin || item.referringFacilityName || item.hospitalName || "";
            var destination = resolveDestination(item);
            var status = item.status || "";
            var statusClass = "transfer-status-pending";
            if (item.agentRejected === true || item.agentRejected === "true") {
                statusClass = "transfer-status-rejected";
            } else if (item.agentDecisionApproved === true || item.agentDecisionApproved === "true") {
                statusClass = "transfer-status-approved";
            } else if (item.needsInsuranceApproval === true || item.needsInsuranceApproval === "true") {
                statusClass = "transfer-status-awaiting";
            }
            var statusHtml = status
                ? "<div class='" + statusClass + "' style='font-size:12px;margin-top:4px;'>" + esc(status) + "</div>"
                : "";
            return ""
                + "<tr class='hie-transfer-row' data-uuid='" + esc(uuid) + "' data-upid='" + esc(upid) + "'>"
                + "<td>" + esc(date) + statusHtml + "</td>"
                + "<td>" + esc(from) + "</td>"
                + "<td>" + esc(destination) + "</td>"
                + "<td><a href='javascript:void(0);' class='hie-transfer-view-link' "
                + "data-transfer-id='" + esc(uuid) + "' data-upid='" + esc(upid) + "' "
                + "title='Open transfer'><i class='icon-eye-open'></i></a></td>"
                + "</tr>";
        }

        function loadHieTransferList() {
            var statusEl = jq("#hie-transfer-list-status");
            var wrapEl = jq("#hie-transfer-list-wrap");
            var bodyEl = jq("#hie-transfer-list-body");

            if (!upid) {
                statusEl.html("<span style='color:#a94442;'>No UPID found for this patient.</span>");
                return;
            }

            jq.ajax({
                url: restUrl,
                type: "GET",
                data: {
                    upid: upid,
                    activeOnly: true
                },
                dataType: "json",
                headers: {
                    "Accept": "application/json"
                }
            }).done(function(response) {
                if (response && response.status === "error") {
                    statusEl.html("<span style='color:#a94442;'>" + esc(response.message || "Unable to load transfers.") + "</span>");
                    return;
                }
                var items = response && response.data ? response.data : [];
                if (!items.length) {
                    statusEl.html("No inbound HIE transfers found for this patient.");
                    return;
                }
                var rows = [];
                for (var i = 0; i < items.length; i++) {
                    rows.push(transferListRowHtml(items[i]));
                }
                bodyEl.html(rows.join(""));
                statusEl.hide();
                wrapEl.show();
            }).fail(function(xhr) {
                var message = "Unable to load transfers from HIE.";
                if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                statusEl.html("<span style='color:#a94442;'>" + esc(message) + "</span>");
            });
        }

        function validateCurrentTransfer() {
            if (!canValidate || !patientId) {
                return;
            }
            var transfer = currentPreviewTransfer;
            if (!transfer || !targetsCurrentFacility(transfer)) {
                return;
            }
            var hieTransferId = transfer.uuid || transfer.id || transfer.hieTransferId || "";
            if (!hieTransferId) {
                return;
            }

            var validateBtn = jq("#hie-transfer-validate-btn");
            var validateStatus = jq("#hie-transfer-validate-status");
            validateBtn.prop("disabled", true).html("<i class='icon-spinner icon-spin'></i> Saving...");
            validateStatus.hide().text("");

            jq.ajax({
                url: validateUrl,
                type: "POST",
                data: {
                    patientId: patientId,
                    hieTransferId: hieTransferId
                },
                dataType: "json",
                headers: {
                    "Accept": "application/json"
                }
            }).done(function(response) {
                if (typeof response === "string") {
                    try {
                        response = jq.parseJSON(response);
                    } catch (err) {
                        validateBtn.prop("disabled", false).text(validateBtn.data("default-label") || "Yes Transfer is valid");
                        validateStatus.show().css("color", "#a94442").text("Unexpected response from server.");
                        return;
                    }
                }
                if (response && response.status === "success") {
                    validateStatus.show().css("color", "#0f766e").text(response.message || "Transfer validated. Refreshing...");
                    window.location.reload();
                    return;
                }
                validateBtn.prop("disabled", false).text(validateBtn.data("default-label") || "Yes Transfer is valid");
                validateStatus.show().css("color", "#a94442").text(
                    response && response.message ? response.message : "Unable to validate transfer."
                );
            }).fail(function(xhr) {
                var message = "Unable to validate transfer.";
                if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                } else if (xhr && xhr.responseText) {
                    try {
                        var parsed = jq.parseJSON(xhr.responseText);
                        if (parsed && parsed.message) {
                            message = parsed.message;
                        }
                    } catch (ignore) {}
                }
                validateBtn.prop("disabled", false).text(validateBtn.data("default-label") || "Yes Transfer is valid");
                validateStatus.show().css("color", "#a94442").text(message);
            });
        }

        jq(document).off("click.hieTransfer", ".hie-transfer-view-link");
        jq(document).on("click.hieTransfer", ".hie-transfer-view-link", function(e) {
            e.preventDefault();
            e.stopPropagation();
            var link = jq(this);
            var transferId = link.attr("data-transfer-id") || link.data("transfer-id") || "";
            var patientUpid = link.attr("data-upid") || link.data("upid") || upid || "";
            loadTransferPreview(transferId, patientUpid);
        });

        jq(document).off("click.hieTransferClose", "#hie-transfer-preview-close");
        jq(document).on("click.hieTransferClose", "#hie-transfer-preview-close", function(e) {
            e.preventDefault();
            hidePreviewDialog();
        });

        jq(document).off("click.hieTransferValidate", "#hie-transfer-validate-btn");
        jq(document).on("click.hieTransferValidate", "#hie-transfer-validate-btn", function(e) {
            e.preventDefault();
            validateCurrentTransfer();
        });

        jq(document).off("click.hieTransferExportPdf", "#hie-transfer-export-pdf-btn");
        jq(document).on("click.hieTransferExportPdf", "#hie-transfer-export-pdf-btn", function(e) {
            e.preventDefault();
            exportCurrentPreviewPdf();
        });

        if (showSection && listFromHie && !hasTransferId) {
            loadHieTransferList();
        }
    }

    jq(document).ready(initHieTransferSection);
})(typeof jq !== "undefined" ? jq : jQuery);
