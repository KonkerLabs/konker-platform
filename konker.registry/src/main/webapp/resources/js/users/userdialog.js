$('#btnSend').on('click', function(e) {
    e.preventDefault();
    var url = urlTo('/recoverpassword');

    var recaptcha = grecaptcha.getResponse();
    var email = $('input[name=username]').val();
    var json = {"email" : email, "recaptcha": recaptcha}
    
    $.ajax({
        context : this,
        type : "POST",
        url : url,
        contentType: "application/json",
        dataType: "json",
        timeout : 100000,
        data: JSON.stringify(json),
        beforeSend : function() {
        },
        success : function(data) {
        	var result = jQuery.parseJSON(data);
        	
        	if (result == true) {
        		$('#sendMailModal').modal('show');
        	} else {
        		$('#noExistUserModal').modal('show');
        	}

        },
        complete : function() {
        }
    });
});


$('#btnYes').click(function() {
  	$('#removeItemModal').modal('hide');
    $("input[type=hidden][name=_method]").val('delete');
    $('form').submit();
});