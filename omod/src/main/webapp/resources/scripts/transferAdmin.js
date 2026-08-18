jQuery(function($) {
    var config = window.transferAdminConfig || {};
    var openmrsPath = (typeof openmrsContextPath !== "undefined" && openmrsContextPath)
        ? openmrsContextPath
        : "";
    var adminBaseUrl = config.adminBaseUrl || (openmrsPath + "/module/transferapp/admin");
    var adminPageUrl = config.adminPageUrl || (openmrsPath + "/transferapp/transferAdmin.page?app=transferapp.dashboard");
    var resourcesBase = config.resourcesBase
        || (openmrsPath + "/moduleResources/transferapp/");
    var selectedLocationId = config.selectedLocationId;
    var selectedReceivingFacilityId = config.selectedReceivingFacilityId;
    var messages = config.messages || {};
    var select2Loading = null;

    $("#transfer-admin-location-select").on("change", function() {
        var locationId = $(this).val();
        if (!locationId) {
            window.location = adminPageUrl;
            return;
        }
        window.location = adminPageUrl + "&locationId=" + encodeURIComponent(locationId);
    });

    $("#transfer-admin-receiving-facility-select").on("change", function() {
        var facilityId = $(this).val();
        if (!facilityId || !selectedLocationId) {
            return;
        }
        window.location = adminPageUrl
            + "&locationId=" + encodeURIComponent(selectedLocationId)
            + "&receivingFacilityId=" + encodeURIComponent(facilityId);
    });

    function showAdminMessage(message, isError) {
        if (typeof emr !== "undefined") {
            if (isError) {
                emr.errorMessage(message);
            } else {
                emr.successMessage(message);
            }
            return;
        }
        if (isError) {
            alert(message);
        }
    }

    function removeEmptyRow($table) {
        $table.find("tbody .transfer-admin-empty-row").remove();
    }

    function escapeHtml(value) {
        return $("<div>").text(value || "").html();
    }

    function isSelect2Ready() {
        bridgeSelect2ToCurrentJquery();
        return typeof $.fn.select2 === "function";
    }

    function bridgeSelect2ToCurrentJquery() {
        // OpenMRS may expose jq and jQuery as different instances; Select2 attaches to one of them.
        if (typeof $.fn.select2 === "function") {
            return;
        }
        if (typeof jQuery !== "undefined" && typeof jQuery.fn.select2 === "function") {
            $.fn.select2 = jQuery.fn.select2;
            if (jQuery.fn.select2.defaults) {
                $.fn.select2.defaults = jQuery.fn.select2.defaults;
            }
            return;
        }
        if (typeof jq !== "undefined" && typeof jq.fn.select2 === "function") {
            $.fn.select2 = jq.fn.select2;
            if (jq.fn.select2.defaults) {
                $.fn.select2.defaults = jq.fn.select2.defaults;
            }
        }
    }

    function ensureSelect2Css() {
        if (document.getElementById("transfer-admin-select2-css")) {
            return;
        }
        $("head").append(
            "<link id='transfer-admin-select2-css' rel='stylesheet' type='text/css' href='"
            + resourcesBase + "styles/select2.min.css' />"
        );
    }

    function ensureSelect2(callback) {
        ensureSelect2Css();
        if (isSelect2Ready()) {
            callback(true);
            return;
        }
        if (select2Loading) {
            select2Loading.done(function() {
                callback(isSelect2Ready());
            }).fail(function() {
                callback(false);
            });
            return;
        }

        select2Loading = $.getScript(resourcesBase + "scripts/select2/select2.min.js");
        select2Loading.done(function() {
            bridgeSelect2ToCurrentJquery();
            callback(isSelect2Ready());
        }).fail(function() {
            callback(false);
        });
    }

    function applySelect2($select) {
        if (!isSelect2Ready()) {
            return false;
        }
        if ($select.hasClass("select2-hidden-accessible")) {
            $select.select2("destroy");
        }
        $select.select2({
            placeholder: messages.registryPlaceholder || "Search and select a facility",
            allowClear: true,
            width: "100%"
        });
        return true;
    }

    function syncFacilityHiddenFields($select) {
        var code = $select.val() || "";
        var $option = $select.find("option:selected");
        var name = $option.attr("data-name") || $option.data("name") || "";
        $("#facilityCode").val(code);
        $("#facilityName").val(name);
    }

    function initFacilityRegistrySelect() {
        var $select = $("#facilityRegistrySelect");
        if (!$select.length) {
            return;
        }

        $select.prop("disabled", true);
        ensureSelect2(function(select2Ready) {
            if (!select2Ready) {
                showAdminMessage(messages.select2Error || messages.registryError, true);
            }

            $.getJSON(adminBaseUrl + "/facilityRegistry.form", function(response) {
                if (!response || response.status !== "success") {
                    showAdminMessage((response && response.message) || messages.registryError, true);
                    $select.prop("disabled", false);
                    return;
                }

                $select.find("option:not(:first)").remove();
                var facilities = response.facilities || [];
                if (!facilities.length) {
                    showAdminMessage(messages.registryEmpty, true);
                }

                $.each(facilities, function(_, facility) {
                    var code = facility.code || "";
                    var name = facility.name || "";
                    var category = facility.category || "";
                    if (!code || !name) {
                        return;
                    }
                    var label = name + " (" + code + ")";
                    if (category) {
                        label += " — " + category;
                    }
                    $select.append(
                        $("<option></option>")
                            .val(code)
                            .text(label)
                            .attr("data-name", name)
                            .attr("data-category", category)
                    );
                });

                if (select2Ready) {
                    applySelect2($select);
                }

                $select.prop("disabled", false);
                syncFacilityHiddenFields($select);
            }).fail(function() {
                showAdminMessage(messages.registryError, true);
                $select.prop("disabled", false);
            });
        });
    }

    $(document).on("change", "#facilityRegistrySelect", function() {
        syncFacilityHiddenFields($(this));
    });

    initFacilityRegistrySelect();

    var facilityFormEditing = false;

    function setFacilityRegistryEnabled(enabled) {
        var $select = $("#facilityRegistrySelect");
        if (!$select.length) {
            return;
        }
        $select.prop("disabled", !enabled);
        if ($select.hasClass("select2-hidden-accessible") && typeof $.fn.select2 === "function") {
            // Keep Select2 UI in sync with the native disabled state.
            $select.trigger("change.select2");
        }
    }

    function selectFacilityInRegistry(code, name) {
        var $select = $("#facilityRegistrySelect");
        if (!$select.length || !code) {
            return;
        }
        if ($select.find("option[value='" + code.replace(/'/g, "\\'") + "']").length === 0 && name) {
            $select.append(
                $("<option></option>")
                    .val(code)
                    .text(name + " (" + code + ")")
                    .attr("data-name", name)
            );
        }
        $select.val(code);
        if ($select.hasClass("select2-hidden-accessible") && typeof $.fn.select2 === "function") {
            $select.trigger("change.select2");
        } else {
            $select.trigger("change");
        }
        syncFacilityHiddenFields($select);
    }

    function enterFacilityEditMode($row) {
        facilityFormEditing = true;
        var code = $row.attr("data-facility-code") || "";
        var name = $row.attr("data-facility-name") || "";
        var province = $row.attr("data-province") || "";
        var district = $row.attr("data-district") || "";
        var distance = $row.attr("data-distance") || "";
        var external = $row.attr("data-external") === "true";

        $("#facilityCode").val(code);
        $("#facilityName").val(name);
        $("#province").val(province);
        $("#district").val(district);
        $("#distance").val(distance);
        $("#external").prop("checked", external);
        selectFacilityInRegistry(code, name);
        setFacilityRegistryEnabled(false);

        $("#facility-form-submit-btn").text(messages.update || "Update");
        $("#facility-form-cancel-btn").show();
        $("#transfer-admin-add-facility-form").addClass("is-editing");

        var formTop = $("#transfer-admin-add-facility-form").offset();
        if (formTop) {
            $("html, body").animate({ scrollTop: Math.max(0, formTop.top - 20) }, 200);
        }
    }

    function resetFacilityForm() {
        facilityFormEditing = false;
        var $form = $("#transfer-admin-add-facility-form");
        if ($form.length && $form[0]) {
            $form[0].reset();
        }
        $("#facilityCode").val("");
        $("#facilityName").val("");
        $("#external").prop("checked", false);
        setFacilityRegistryEnabled(true);
        var $select = $("#facilityRegistrySelect");
        if ($select.length) {
            $select.val("");
            if ($select.hasClass("select2-hidden-accessible") && typeof $.fn.select2 === "function") {
                $select.trigger("change.select2");
            }
            syncFacilityHiddenFields($select);
        }
        $("#facility-form-submit-btn").text(messages.add || "Add");
        $("#facility-form-cancel-btn").hide();
        $form.removeClass("is-editing");
    }

    $(document).on("click", ".transfer-admin-edit-facility", function(e) {
        e.preventDefault();
        var $row = $(this).closest("tr");
        if (!$row.length) {
            return;
        }
        enterFacilityEditMode($row);
    });

    $("#facility-form-cancel-btn").on("click", function(e) {
        e.preventDefault();
        resetFacilityForm();
    });

    $("#transfer-admin-add-facility-form").on("submit", function(event) {
        event.preventDefault();
        if (!facilityFormEditing) {
            syncFacilityHiddenFields($("#facilityRegistrySelect"));
        }
        if (!$("#facilityCode").val() || !$("#facilityName").val()) {
            showAdminMessage(messages.registryRequired || messages.registryPlaceholder, true);
            return;
        }
        var $form = $(this);
        var payload = $form.serialize();
        // Unchecked checkbox is omitted; force false when editing/clearing external.
        if (!$("#external").is(":checked")) {
            payload += (payload ? "&" : "") + "external=false";
        }
        $.post(adminBaseUrl + "/saveFacility.form", payload, function(response) {
            if (response.status === "success") {
                window.location = adminPageUrl
                    + "&locationId=" + encodeURIComponent(selectedLocationId)
                    + "&receivingFacilityId=" + encodeURIComponent(response.receivingFacilityId);
            } else {
                showAdminMessage(response.message || messages.saveError, true);
            }
        }, "json").fail(function() {
            showAdminMessage(messages.saveError, true);
        });
    });

    $("#transfer-admin-add-service-form").on("submit", function(event) {
        event.preventDefault();
        var $form = $(this);
        var editingServiceId = $("#receivingServiceId").val();
        $.post(adminBaseUrl + "/saveService.form", $form.serialize(), function(response) {
            if (response.status === "success") {
                removeEmptyRow($("#transfer-admin-services-table"));
                var $existingRow = $("#transfer-admin-services-table tr[data-service-id='" + response.receivingServiceId + "']");
                var rowHtml =
                    "<tr data-service-id=\"" + response.receivingServiceId + "\" data-service-name=\"" + escapeHtml(response.serviceName) + "\">" +
                    "<td>" + escapeHtml(response.serviceName) + "</td>" +
                    "<td class=\"transfer-admin-col-action\">" +
                    "<button type=\"button\" class=\"btn btn-link transfer-admin-edit-service\" data-service-id=\"" +
                    response.receivingServiceId + "\"><i class=\"icon-pencil\"></i> " + escapeHtml(messages.edit || "Edit") + "</button> " +
                    "<button type=\"button\" class=\"btn btn-link transfer-admin-remove-service\" data-service-id=\"" +
                    response.receivingServiceId + "\">" + escapeHtml(messages.remove) + "</button>" +
                    "</td></tr>";
                if ($existingRow.length) {
                    $existingRow.replaceWith(rowHtml);
                } else if (!editingServiceId) {
                    $("#transfer-admin-services-table tbody").append(rowHtml);
                }
                resetServiceForm();
                showAdminMessage(messages.saveSuccess, false);
            } else {
                showAdminMessage(response.message || messages.saveError, true);
            }
        }, "json").fail(function() {
            showAdminMessage(messages.saveError, true);
        });
    });

    function resetServiceForm() {
        var $form = $("#transfer-admin-add-service-form");
        $("#receivingServiceId").val("").prop("disabled", true);
        $("#serviceName").val("");
        $form.find("input[name='receivingFacilityId']").val(selectedReceivingFacilityId || "");
        $("#service-form-submit-btn").text(messages.add || "Add");
        $("#service-form-cancel-btn").hide();
        $form.removeClass("is-editing");
    }

    function enterServiceEditMode($row) {
        var serviceId = $row.attr("data-service-id") || "";
        var serviceName = $row.attr("data-service-name") || "";
        $("#receivingServiceId").prop("disabled", false).val(serviceId);
        $("#serviceName").val(serviceName).focus();
        $("#service-form-submit-btn").text(messages.update || "Update");
        $("#service-form-cancel-btn").show();
        $("#transfer-admin-add-service-form").addClass("is-editing");
        var formTop = $("#transfer-admin-add-service-form").offset();
        if (formTop) {
            $("html, body").animate({ scrollTop: Math.max(0, formTop.top - 20) }, 200);
        }
    }

    $(document).on("click", ".transfer-admin-edit-service", function(e) {
        e.preventDefault();
        var $row = $(this).closest("tr");
        if (!$row.length) {
            return;
        }
        enterServiceEditMode($row);
    });

    $("#service-form-cancel-btn").on("click", function(e) {
        e.preventDefault();
        resetServiceForm();
    });

    $(document).on("click", ".transfer-admin-remove-facility", function() {
        var facilityId = $(this).data("facility-id");
        if (!confirm(messages.confirmRemove)) {
            return;
        }
        $.post(adminBaseUrl + "/voidFacility.form", { receivingFacilityId: facilityId }, function(response) {
            if (response.status === "success") {
                window.location = adminPageUrl + "&locationId=" + encodeURIComponent(selectedLocationId);
            } else {
                showAdminMessage(response.message || messages.removeError, true);
            }
        }, "json").fail(function() {
            showAdminMessage(messages.removeError, true);
        });
    });

    $(document).on("click", ".transfer-admin-remove-service", function() {
        var serviceId = $(this).data("service-id");
        if (!confirm(messages.confirmRemove)) {
            return;
        }
        $.post(adminBaseUrl + "/voidService.form", { receivingServiceId: serviceId }, function(response) {
            if (response.status === "success") {
                $("#transfer-admin-services-table tr[data-service-id='" + serviceId + "']").remove();
                showAdminMessage(messages.removeSuccess, false);
            } else {
                showAdminMessage(response.message || messages.removeError, true);
            }
        }, "json").fail(function() {
            showAdminMessage(messages.removeError, true);
        });
    });
});
