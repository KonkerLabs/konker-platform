$(document).ready(function() {
    var outgoingSchemeControl = $('input[type=radio][name=outgoingScheme]');

    outgoingSchemeControl.change(function() {
        renderOutgoingFragment(this.value);
    });

    applyEventBindings(outgoingSchemeControl.filter('input:checked').val());
});

function renderOutgoingFragment(scheme) {
    var base = urlTo('/routes/outgoing/');
    var url = base + scheme;

    fetchViewFragment(scheme, url);
}

function fetchViewFragment(scheme, fetchUrl) {
    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        beforeSend : function() {
            showElement('#loading');
        },
        success : function(data) {
            displayFragment(data);
            applyEventBindings(scheme);
        },
        complete : function() {
            hideElement('#loading');
        }
    });
}

function displayFragment(data) {
    $('#outgoingFragment').html(data);
}

function applyEventBindings(scheme) {
    switch (scheme) {
        case "sms" :
            $('input[type=radio][name=outgoingSmsMessageStrategy]').change(function() {
                applySmsFragmentControlState(this);
            });
            break;
        default : break;
    }
}

function applySmsFragmentControlState(selected) {
    switch (selected.value) {
        case "forward" :
            $('#outgoingSmsMessageTemplate').prop('disabled', true);
            break;
        default :
            $('#outgoingSmsMessageTemplate').prop('disabled', false);
            break;
    }
}

function showElement(selector) {
    $(selector).show();
}

function hideElement(selector) {
    $(selector).hide();
}