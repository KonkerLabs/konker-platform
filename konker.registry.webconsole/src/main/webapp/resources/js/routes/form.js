$(document).ready(function() {

	// incoming
    var incomingSchemeControl = $('input[type=radio][name=incomingScheme]');

    incomingSchemeControl.change(function() {
        renderIncomingFragment(this.value);
    });

    applyEventBindings(incomingSchemeControl.filter('input:checked').val());

	// outgoing
    var outgoingSchemeControl = $('input[type=radio][name=outgoingScheme]');

    outgoingSchemeControl.change(function() {
        renderOutgoingFragment(this.value);
    });

    applyEventBindings(outgoingSchemeControl.filter('input:checked').val());

});

function renderIncomingFragment(scheme) {
    var base = urlTo('/routes/' + $('#applicationName').val() + '/incoming/');
    var url = base + scheme;

    fetchViewFragment(scheme, url, $('#incomingFragment'));
}

function renderOutgoingFragment(scheme) {
    var base = urlTo('/routes/' + $('#applicationName').val() + '/outgoing/');
    var url = base + scheme;

    fetchViewFragment(scheme, url, $('#outgoingFragment'));
}

function fetchViewFragment(scheme, fetchUrl, fragment) {
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
        	fragment.html(data);
            applyEventBindings(scheme);
        },
        complete : function() {
            clearTimeout(loadSpinner);
            $("div.ajax-loading").removeClass('show');
        }
    });
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