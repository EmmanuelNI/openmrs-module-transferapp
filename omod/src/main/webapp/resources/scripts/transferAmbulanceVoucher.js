(function() {
    var jq = (typeof jQuery !== "undefined") ? jQuery : (typeof $ !== "undefined" ? $ : null);
    if (!jq) {
        return;
    }

    jq(function() {
        var filterConfig = window.transferAmbulanceVoucherFilterConfig || {};
        var filterMessages = filterConfig.messages || {};
        var maxDateRangeMonths = filterConfig.maxDateRangeMonths || 3;
        var filterForm = jq("#transfer-ambulance-voucher-filter-form");
        var startInput = document.getElementById("ambulance-voucher-filter-start-date");
        var endInput = document.getElementById("ambulance-voucher-filter-end-date");
        var errorEl = jq("#ambulance-voucher-filter-error");
        var previewUrl = filterConfig.previewUrl
            || ((window.transferOpenmrsPath || "") + "/module/transferapp/transfer/ambulanceVoucherPreview.form");

        function parseYmd(value) {
            if (!value) {
                return null;
            }
            var parts = String(value).split("-");
            if (parts.length !== 3) {
                return null;
            }
            var year = parseInt(parts[0], 10);
            var month = parseInt(parts[1], 10) - 1;
            var day = parseInt(parts[2], 10);
            if (isNaN(year) || isNaN(month) || isNaN(day)) {
                return null;
            }
            return new Date(year, month, day);
        }

        function monthDiff(startDate, endDate) {
            var months = (endDate.getFullYear() - startDate.getFullYear()) * 12;
            months += endDate.getMonth() - startDate.getMonth();
            if (endDate.getDate() < startDate.getDate()) {
                months -= 1;
            }
            return months;
        }

        function showFilterError(message) {
            if (!errorEl.length) {
                window.alert(message);
                return;
            }
            errorEl.text(message || "").show();
        }

        function clearFilterError() {
            if (errorEl.length) {
                errorEl.hide().text("");
            }
        }

        function validateDateRange(startValue, endValue) {
            clearFilterError();
            var startDate = parseYmd(startValue);
            var endDate = parseYmd(endValue);
            if (!startDate || !endDate) {
                showFilterError(filterMessages.invalidDateRange || "Please select valid start and end dates.");
                return false;
            }
            if (startDate > endDate) {
                showFilterError(filterMessages.invalidDateRange || "Start date must be on or before end date.");
                return false;
            }
            if (monthDiff(startDate, endDate) > maxDateRangeMonths) {
                showFilterError(filterMessages.dateRangeError
                    || ("Date range cannot exceed " + maxDateRangeMonths + " months."));
                return false;
            }
            return true;
        }

        function escapeHtml(value) {
            return String(value == null ? "" : value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }

        function displayValue(value) {
            var text = value == null ? "" : String(value).trim();
            return text.length ? escapeHtml(text) : "&nbsp;";
        }

        function buildVoucherHtml(v) {
            var distanceLabel = (v.distanceKm != null && v.distanceKm !== "")
                ? (escapeHtml(v.distanceKm) + " Km")
                : "&nbsp;";
            var amountLabel = v.amount ? escapeHtml(v.amount) + " RWF" : "&nbsp;";
            return ""
                + "<div class='bon-ambulance'>"
                + "  <div class='bon-ambulance-header'>"
                + "    <div class='bon-ambulance-brand'>" + displayValue(v.organizationName) + "</div>"
                + "    <div class='bon-ambulance-fax'>FAX: " + displayValue(v.fax) + "</div>"
                + "    <h2 class='bon-ambulance-title'>" + displayValue(v.title || "BON D'AMBULANCE") + "</h2>"
                + "  </div>"
                + "  <div class='bon-ambulance-location'>"
                + "    <div><span>Province</span><strong>" + displayValue(v.province) + "</strong></div>"
                + "    <div><span>District</span><strong>" + displayValue(v.district) + "</strong></div>"
                + "    <div><span>Section / Hôpital</span><strong>" + displayValue(v.sectionHospital) + "</strong></div>"
                + "  </div>"
                + "  <div class='bon-ambulance-grid'>"
                + "    <div class='bon-field'><label>1. Date</label><div>" + displayValue(v.date) + "</div></div>"
                + "    <div class='bon-field'><label>2. Heure de départ</label><div>" + displayValue(v.departureTime) + "</div></div>"
                + "    <div class='bon-field'><label>3. Nombre de patients à bord</label><div>" + displayValue(v.patientCount) + "</div></div>"
                + "    <div class='bon-field'><label>4. ID</label><div>" + displayValue(v.voucherId) + "</div></div>"
                + "    <div class='bon-field bon-field-wide'><label>5. Destination</label><div>" + displayValue(v.destination) + "</div></div>"
                + "    <div class='bon-field'><label>Distance parcourue (Allée et Retour)</label><div>" + distanceLabel + "</div></div>"
                + "    <div class='bon-field'><label>Montant facture</label><div>" + amountLabel + "</div></div>"
                + "  </div>"
                + "  <table class='bon-patients'>"
                + "    <thead><tr>"
                + "      <th>#</th>"
                + "      <th>Nom et prénom du (des) patient(s)</th>"
                + "      <th>Numéro d'affiliation du (des) patient(s)</th>"
                + "    </tr></thead>"
                + "    <tbody>"
                + "      <tr><td>1</td><td>" + displayValue(v.patientName) + "</td><td>" + displayValue(v.affiliationNumber) + "</td></tr>"
                + "      <tr><td>2</td><td>&nbsp;</td><td>&nbsp;</td></tr>"
                + "      <tr><td>3</td><td>&nbsp;</td><td>&nbsp;</td></tr>"
                + "    </tbody>"
                + "  </table>"
                + "  <div class='bon-notes'>"
                + "    <p><strong>N.B:</strong> " + displayValue(v.noteReferral) + "</p>"
                + "    <p>" + displayValue(v.noteInvoice) + "</p>"
                + "  </div>"
                + "</div>";
        }

        function closePreview() {
            jq("#ambulance-voucher-preview-dialog").hide().removeClass("is-open");
            jq("#ambulance-voucher-preview-overlay").hide();
            jq("body").removeClass("ambulance-voucher-preview-open");
        }

        function showPreview(html) {
            jq("#ambulance-voucher-preview-body").html(html);
            jq("#ambulance-voucher-preview-overlay").show();
            jq("#ambulance-voucher-preview-dialog").addClass("is-open").show().css("display", "flex");
            jq("body").addClass("ambulance-voucher-preview-open");
        }

        function loadPreview(uuid) {
            if (!uuid) {
                showPreview("<p class='bon-error'>" + escapeHtml(filterMessages.previewError || "Unable to load voucher") + "</p>");
                return;
            }
            showPreview("<p class='bon-loading'><i class='icon-spinner icon-spin'></i> "
                + escapeHtml(filterMessages.previewLoading || "Loading…") + "</p>");
            jq.ajax({
                url: previewUrl,
                type: "GET",
                dataType: "json",
                data: { uuid: uuid },
                timeout: 30000
            }).done(function(response) {
                if (response && response.status === "success" && response.voucher) {
                    showPreview(buildVoucherHtml(response.voucher));
                } else {
                    var message = (response && response.message)
                        ? response.message
                        : (filterMessages.previewError || "Unable to load voucher");
                    showPreview("<p class='bon-error'>" + escapeHtml(message) + "</p>");
                }
            }).fail(function() {
                showPreview("<p class='bon-error'>" + escapeHtml(filterMessages.previewError || "Unable to load voucher") + "</p>");
            });
        }

        if (filterForm.length && typeof flatpickr === "function") {
            if (startInput) {
                flatpickr(startInput, {
                    dateFormat: "Y-m-d",
                    allowInput: true,
                    defaultDate: filterConfig.startDate || null
                });
            }
            if (endInput) {
                flatpickr(endInput, {
                    dateFormat: "Y-m-d",
                    allowInput: true,
                    defaultDate: filterConfig.endDate || null
                });
            }
            filterForm.on("submit", function(e) {
                if (!validateDateRange(
                        jq("#ambulance-voucher-filter-start-date").val(),
                        jq("#ambulance-voucher-filter-end-date").val())) {
                    e.preventDefault();
                }
            });
        }

        if (jq.fn.dataTable && jq("#transfer-ambulance-voucher-table").length) {
            jq("#transfer-ambulance-voucher-table").dataTable({
                bFilter: true,
                bInfo: true,
                bPaginate: true,
                bLengthChange: true,
                sPaginationType: "full_numbers",
                iDisplayLength: 25,
                aLengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
                aaSorting: [[1, "desc"]],
                aoColumnDefs: [
                    { bSortable: false, aTargets: [8] }
                ],
                oLanguage: {
                    sSearch: "Filter:",
                    oPaginate: {
                        sFirst: "First",
                        sPrevious: "Previous",
                        sNext: "Next",
                        sLast: "Last"
                    }
                }
            });
        }

        jq(document).on("click", ".ambulance-voucher-preview-link", function(e) {
            e.preventDefault();
            loadPreview(jq(this).attr("data-uuid"));
        });

        jq(document).on("click", "#ambulance-voucher-preview-close, #ambulance-voucher-preview-overlay", function(e) {
            e.preventDefault();
            closePreview();
        });

        jq(document).on("click", "#ambulance-voucher-preview-print", function(e) {
            e.preventDefault();
            var content = jq("#ambulance-voucher-preview-body").html();
            if (!content) {
                return;
            }
            var printWindow = window.open("", "_blank", "width=900,height=1000");
            if (!printWindow) {
                window.print();
                return;
            }
            printWindow.document.write("<!DOCTYPE html><html><head><title>BON D'AMBULANCE</title>");
            printWindow.document.write("<link rel='stylesheet' href='"
                + (window.transferOpenmrsPath || "")
                + "/moduleResources/transferapp/styles/ambulanceVoucherPreview.css' />");
            printWindow.document.write("</head><body class='bon-print-body'>");
            printWindow.document.write(content);
            printWindow.document.write("</body></html>");
            printWindow.document.close();
            printWindow.focus();
            setTimeout(function() {
                printWindow.print();
            }, 300);
        });
    });
}());
