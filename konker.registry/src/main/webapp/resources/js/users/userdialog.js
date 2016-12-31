$('#recoverPassword').on('click', function(e) {
    e.preventDefault();
    var url = urlTo('/recoverpassword');
    var email = $('input[name=username]').val();
    url = url + "?email=" + email;
    $.ajax({
        context : this,
        type : "GET",
        url : url,
        dataType: "html",
        timeout : 100000,
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