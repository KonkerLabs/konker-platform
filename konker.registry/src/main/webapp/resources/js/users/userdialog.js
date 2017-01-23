var isRecaptchaEnabled = false;
var recaptcha = "";

$(function() {
    var siteKey = $('#recaptcha').attr("data-sitekey");
    if (siteKey == "site-key") {
        $('#recaptcha').remove();
	} else {
        isRecaptchaEnabled = true;
	}
});

// tela de login
$('#btnLoginSend').on('click', function(e) {

	if (isRecaptchaEnabled) {
        recaptcha = grecaptcha.getResponse();
	}

	$('#recoverResultOk').hide();
	$('#recoverResultError').hide();
	
	$('.login-form-rcv input[type="text"]').removeClass('has-error');
	$('#recaptcha').removeClass('div-has-error');
	
	if ($('.login-form-rcv input[type="text"]').val() != "" && recaptcha != "") {
		e.preventDefault();
		var url = urlTo('/recoverpassword/email');
		
		var email = $('.login-form-rcv input[name=username]').val();
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
					$('#recoverResultOk').show();
					$('#btnLoginSend').prop('disabled',true);
				} else {
					$('#recoverResultError').show();
				}
				
			},
			complete : function() {
			}
		});
	}

	if ($('.login-form-rcv input[type="text"]').val() == "") {
		$('.login-form-rcv input[type="text"]').addClass('has-error');
	}
	if (recaptcha == "") {
		$('#recaptcha').addClass('div-has-error');
	}
});

// tela de recuperação de senha (ainda é acessada?)
$('#btnSend').on('click', function(e) {

    if (isRecaptchaEnabled) {
        recaptcha = grecaptcha.getResponse();
    }
	
	if ($('.login-form input[type="text"]').val() != "" && recaptcha != "") {
		$('.login-form input[type="text"]').removeClass('input-error');
		$('#recaptcha').removeClass('input-div-error');
		
		e.preventDefault();
		var url = urlTo('/recoverpassword/email');
		
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
	} 
	if ($('.login-form input[type="text"]').val() == "") {
		$('.login-form input[type="text"]').addClass('input-error');
	} 
	if (recaptcha == "") {
		$('#recaptcha').addClass('input-div-error');
	}
});
