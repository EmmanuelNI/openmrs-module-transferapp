(function() {
    jq(document).ready(function() {
        if (jq.fn.dataTable && jq("#transfer-records-table").length) {
            jq("#transfer-records-table").dataTable({
                "sDom": "lfrtip",
                "bLengthChange": true,
                "iDisplayLength": 25,
                "aLengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
                "aaSorting": [[0, "desc"]],
                "aoColumnDefs": [
                    { "bSortable": false, "aTargets": [6] }
                ]
            });
        }

        var transferOpenmrsPath = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "");
        window.transferOpenmrsPath = transferOpenmrsPath;
        var transferPreviewUrl = transferOpenmrsPath + "/module/transferapp/transfer/preview.form";
        var transferSubmitUrl = transferOpenmrsPath + "/module/transferapp/transfer/submit.form";
        var transferPreviewResourcesBase = transferOpenmrsPath + "/moduleResources/transferapp/scripts/";
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
                submitBtn.prop("disabled", true).text("Sent to HIE");
            } else {
                submitBtn.prop("disabled", false).text("Submit");
            }
        }

        function ensureTransferPreviewRenderer(callback) {
            if (typeof buildTransferFormPreviewHtml === "function") {
                callback();
                return;
            }
            if (transferPreviewScriptsLoading) {
                transferPreviewScriptsLoading.done(callback);
                return;
            }
            transferPreviewScriptsLoading = jq.getScript(transferPreviewResourcesBase + "transferMohLogo.js")
                .then(function() {
                    return jq.getScript(transferPreviewResourcesBase + "transferFormPreview.js");
                })
                .done(callback);
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
            jq("#transfer-preview-body").html("<div style='padding:10px;'><i class='icon-spinner icon-spin'></i> Loading...</div>");
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
                var message = response && response.message ? response.message : "Unable to load transfer details.";
                jq("#transfer-preview-body").html("<p style='color:red;'>" + message + "</p>");
            }).fail(function() {
                jq("#transfer-preview-body").html("<p style='color:red;'>Unable to load transfer details.</p>");
            });
        }

        jq(document).on("click", ".transfer-view-link", function(e) {
            e.preventDefault();
            var transferUuid = jq(this).attr("data-transfer-id")
                || jq(this).closest("tr.transfer-row").attr("data-transfer-id");
            showTransferPreview(transferUuid);
        });

        jq(document).on("click", "#transfer-preview-close", function(e) {
            e.preventDefault();
            if (transferPreviewDialog && typeof transferPreviewDialog.close === "function") {
                transferPreviewDialog.close();
            }
            jq("#transfer-preview-dialog").hide();
        });

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
                        emr.successMessage("Transfer sent to HIE successfully.");
                    }
                    window.location.reload();
                    return;
                }
                var message = response && response.message ? response.message : "Unable to send transfer to HIE.";
                if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                    emr.errorMessage(message);
                }
                syncTransferPreviewSubmitButton();
            }).fail(function() {
                if (typeof emr !== "undefined" && typeof emr.errorMessage === "function") {
                    emr.errorMessage("Unable to send transfer to HIE.");
                }
                syncTransferPreviewSubmitButton();
            });
        });
    });
})();
