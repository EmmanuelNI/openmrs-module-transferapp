jQuery(function($) {
    var config = window.transferProfileConfig || {};
    var saveUrl = config.saveUrl;
    var messages = config.messages || {};

    function showProfileMessage(message, isError) {
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

    $("#transfer-profile-form").on("submit", function(event) {
        event.preventDefault();
        var $form = $(this);
        var $submit = $form.find("button[type='submit']");
        $submit.prop("disabled", true);

        $.post(saveUrl, $form.serialize(), function(response) {
            if (response && response.status === "success") {
                showProfileMessage(messages.saveSuccess, false);
            } else {
                var message = response && response.message ? response.message : messages.saveError;
                showProfileMessage(message, true);
            }
        }, "json").fail(function() {
            showProfileMessage(messages.saveError, true);
        }).always(function() {
            $submit.prop("disabled", false);
        });
    });
});
