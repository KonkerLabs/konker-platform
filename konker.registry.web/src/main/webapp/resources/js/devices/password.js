$(document).ready(function() {
	$('#password').hover(function() {
		$(this).attr('type', 'text');
	}, function() {
		$(this).attr('type', 'password');
	});
	
	var value = $('#password').prop("value");
	if(value){
		$('#passwordModal').modal('show');
	}
});

$('#btnYes').click(function() {
	$('#confirmModal').modal('hide');
	$('form').submit();
});