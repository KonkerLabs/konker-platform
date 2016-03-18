$(document).ready(function() {
    $('input[type=radio][name=outgoingScheme]').change(function() {
        renderOutgoingFragment(this.value);
    });
});

function renderOutgoingFragment(scheme) {
    var base = urlTo('/routes/outgoing/');
    var url = base + scheme;

    fetchViewFragment(url);
}

function renderCustomTemplate(elem) {
    if (elem.value == "forward") {
        $('#outgoingSmsMessageTemplate').prop( "disabled", true );
//        $('#outgoingSmsMessageTemplate').prop( "value", null );
    } else if (elem.value == "custom") {
        $('#outgoingSmsMessageTemplate').prop( "disabled", false );
    }
}

function fetchViewFragment(fetchUrl) {
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
        },
        complete : function() {
            hideElement('#loading');
        }
    });
}

function displayFragment(data) {
    $('#outgoingFragment').html(data);
}

function showElement(selector) {
    $(selector).show();
}

function hideElement(selector) {
    $(selector).hide();
}