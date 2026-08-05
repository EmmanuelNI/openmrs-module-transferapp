(function() {
    jq(document).ready(function() {
        if (jq.fn.dataTable && jq("#transfer-pending-table").length) {
            jq("#transfer-pending-table").dataTable({
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
        var transferHiePreviewUrl = transferOpenmrsPath + "/ws/rest/v1/transferapp/transfer";
        var transferPreviewResourcesBase = transferOpenmrsPath + "/moduleResources/transferapp/scripts/";
        var transferPreviewDialog = null;
        var transferPreviewScriptsLoading = null;

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

        function renderTransferPreview(transfer) {
            var previewHtml = typeof buildTransferFormPreviewHtml === "function"
                ? buildTransferFormPreviewHtml(transfer)
                : "<p style='color:red;'>Preview renderer not loaded.</p>";
            jq("#transfer-preview-body").html(previewHtml);
        }

        function showPendingTransferPreview(uuid, upid) {
            if (!uuid || !upid) {
                jq("#transfer-preview-body").html("<p style='color:red;'>Missing transfer UUID or UPID.</p>");
                showTransferPreviewDialog();
                return;
            }

            jq("#transfer-preview-body").html("<div style='padding:10px;'><i class='icon-spinner icon-spin'></i> Loading...</div>");
            showTransferPreviewDialog();

            jq.ajax({
                url: transferHiePreviewUrl,
                type: "GET",
                data: {
                    upid: upid,
                    transferId: uuid
                },
                dataType: "json"
            }).done(function(response) {
                var items = response && response.data ? response.data : null;
                if (response && response.status === "error") {
                    jq("#transfer-preview-body").html("<p style='color:red;'>" + (response.message || "Unable to load transfer.") + "</p>");
                    return;
                }
                if (items && items.length) {
                    ensureTransferPreviewRenderer(function() {
                        renderTransferPreview(items[0]);
                    });
                    return;
                }
                jq("#transfer-preview-body").html("<p style='color:red;'>Transfer not found in HIE.</p>");
            }).fail(function(xhr) {
                var message = "Unable to load transfer details.";
                if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
                    message = xhr.responseJSON.message;
                }
                jq("#transfer-preview-body").html("<p style='color:red;'>" + message + "</p>");
            });
        }

        jq(document).on("click", ".transfer-pending-view-link", function(e) {
            e.preventDefault();
            var link = jq(this);
            var row = link.closest("tr.transfer-row");
            var uuid = link.attr("data-uuid") || row.attr("data-uuid");
            var upid = link.attr("data-upid") || row.attr("data-upid");
            showPendingTransferPreview(uuid, upid);
        });

        jq(document).on("click", "#transfer-preview-close", function(e) {
            e.preventDefault();
            if (transferPreviewDialog && typeof transferPreviewDialog.close === "function") {
                transferPreviewDialog.close();
            }
            jq("#transfer-preview-dialog").hide();
        });
    });
})();
