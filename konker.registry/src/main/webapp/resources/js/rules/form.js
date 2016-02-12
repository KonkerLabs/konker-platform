$(document).ready(function() {
    $('input[type=radio][name=outgoingScheme]').change(function() {
        renderOutgoingFragment(this.value);
    });
});

function renderOutgoingFragment(scheme) {
    var base = urlTo('/rules/outgoing/');
    var url = base + scheme;

    fetchViewFragment(url);
}

function fetchViewFragment(fetchUrl) {
    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        success : function(data) {
            displayFragment(data);
        }
    });
}

function displayFragment(data) {
    $('#outgoingFragment').html(data);
}