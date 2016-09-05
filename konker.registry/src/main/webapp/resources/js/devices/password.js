$(document).ready(function() {
    $('#password').hover(function () {
        $(this).attr('type', 'text');
    }, function () {
        $(this).attr('type', 'password');
    });
});

$('#btnYes').click(function() {
  	$('#confirmModal').modal('hide');
    $('form').submit();
});