(function(jq) {
    if (window.__transferPendingBound) {
        return;
    }
    window.__transferPendingBound = true;

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

    jq(document).ready(function() {
        if (jq.fn.dataTable && jq("#transfer-pending-table").length) {
            jq("#transfer-pending-table").dataTable({
                "sDom": "lfrtip",
                "bLengthChange": true,
                "iDisplayLength": 25,
                "aLengthMenu": [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
                "sPaginationType": "full_numbers",
                "aaSorting": [[0, "desc"]],
                "aoColumnDefs": [
                    { "bSortable": false, "aTargets": [6] }
                ]
            });
        }

        var transferOpenmrsPath = (typeof openmrsContextPath !== "undefined" ? openmrsContextPath : "");
        window.transferOpenmrsPath = transferOpenmrsPath;
        var transferHiePreviewUrl = normalizeRootUrl(transferOpenmrsPath + "/ws/rest/v1/transferapp/transfer");
        var transferPreviewResourcesBase = normalizeRootUrl(transferOpenmrsPath + "/moduleResources/transferapp/scripts/");
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
                .done(callback)
                .fail(function() {
                    jq("#transfer-preview-body").html("<p style='color:red;'>Unable to load transfer preview renderer.</p>");
                });
        }

        function showTransferPreviewDialog() {
            var dialogEl = jq("#transfer-preview-dialog");
            if (!dialogEl.length) {
                return;
            }
            if (dialogEl.parent()[0] !== document.body) {
                dialogEl.appendTo(document.body);
            }
            if (transferPreviewDialog == null && typeof emr !== "undefined" && typeof emr.setupConfirmationDialog === "function") {
                transferPreviewDialog = emr.setupConfirmationDialog({
                    selector: "#transfer-preview-dialog",
                    actions: {
                        confirm: function() {},
                        cancel: function() {
                            dialogEl.hide();
                        }
                    }
                });
                if (transferPreviewDialog && typeof transferPreviewDialog.close === "function") {
                    transferPreviewDialog.close();
                }
            }
            if (transferPreviewDialog && typeof transferPreviewDialog.show === "function") {
                transferPreviewDialog.show();
            } else {
                dialogEl.show();
            }
        }

        function hideTransferPreviewDialog() {
            if (transferPreviewDialog && typeof transferPreviewDialog.close === "function") {
                transferPreviewDialog.close();
            }
            jq("#transfer-preview-dialog").hide();
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

            jq("#transfer-preview-body").html(
                "<div style='padding:10px;'><i class='icon-spinner icon-spin'></i> Loading transfer information...</div>"
            );
            showTransferPreviewDialog();

            jq.ajax({
                url: transferHiePreviewUrl,
                type: "GET",
                data: {
                    upid: upid,
                    transferId: uuid,
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
                        jq("#transfer-preview-body").html("<p style='color:red;'>Transfer endpoint returned non-JSON response.</p>");
                        return;
                    }
                }
                if (response && response.status === "error") {
                    jq("#transfer-preview-body").html(
                        "<p style='color:red;'>" + esc(response.message || "Unable to load transfer.") + "</p>"
                    );
                    return;
                }
                var items = response && response.data ? response.data : [];
                if (items.length) {
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
                } else if (xhr && xhr.responseText) {
                    try {
                        var parsed = jq.parseJSON(xhr.responseText);
                        if (parsed && parsed.message) {
                            message = parsed.message;
                        }
                    } catch (ignore) {}
                }
                jq("#transfer-preview-body").html("<p style='color:red;'>" + esc(message) + "</p>");
            });
        }

        jq(document).off("click.transferPending", ".transfer-pending-view-link");
        jq(document).on("click.transferPending", ".transfer-pending-view-link", function(e) {
            e.preventDefault();
            e.stopPropagation();
            var link = jq(this);
            var row = link.closest("tr.transfer-row");
            var uuid = link.attr("data-uuid") || row.attr("data-uuid") || "";
            var upid = link.attr("data-upid") || row.attr("data-upid") || "";
            showPendingTransferPreview(uuid, upid);
        });

        jq(document).off("click.transferPendingClose", "#transfer-preview-close");
        jq(document).on("click.transferPendingClose", "#transfer-preview-close", function(e) {
            e.preventDefault();
            hideTransferPreviewDialog();
        });
    });
})(typeof jq !== "undefined" ? jq : jQuery);
