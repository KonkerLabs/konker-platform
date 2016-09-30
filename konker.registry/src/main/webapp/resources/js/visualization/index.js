$('.date').datetimepicker();

$('#device').change(function() {
	renderOutgoingFragment(this.value, '/visualization/loading/channel/', '#div-channel');
});

$('button').click(function() {
	renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/load', '#chart');
});

function renderOutgoingFragment(scheme, url, element) {
    var base = urlTo(url);
    var url = base;

    fetchViewFragment(scheme, url, element);
}

function fetchViewFragment(scheme, fetchUrl, element) {
    $.ajax({
        context : this,
        type : "POST",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        data: scheme,
        beforeSend : function() {
            showElement('#loading');
        },
        success : function(data) {
            displayFragment(element, data);
        },
        complete : function() {
            hideElement('#loading');
        }
    });
}

function displayFragment(element, data) {
    $(element).html(data);
}

function showElement(selector) {
    $(selector).show();
}

function hideElement(selector) {
    $(selector).hide();
}