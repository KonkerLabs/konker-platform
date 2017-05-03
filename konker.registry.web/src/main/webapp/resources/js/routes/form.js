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
    var loadSpinner;

    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        beforeSend : function() {
            loadSpinner = setTimeout(function() {
                $("div.ajax-loading").addClass('show');
            }, 50);
        },
        success : function(data) {
            displayFragment(data);
            applyEventBindings(scheme);
        },
        complete : function() {
            clearTimeout(loadSpinner);
            $("div.ajax-loading").removeClass('show');
        }
    });
}

function displayFragment(data) {
    $('#outgoingFragment').html(data);
}

function applyEventBindings(scheme) {
    switch (scheme) {
        default : break;
    }
}

function showElement(selector) {
    $(selector).show();
}

function hideElement(selector) {
    $(selector).hide();
}

$('.confirm-delete').on('click', function(e) {
    e.preventDefault();
    $('#removeItemModal').modal('show');
});


$('#btnYes').click(function() {
  	$('#removeItemModal').modal('hide');
    $("input[type=hidden][name=_method]").val('delete');
    $('form').submit();
});