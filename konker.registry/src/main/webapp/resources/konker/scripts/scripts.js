jQuery(document).ready(function() {

    /*
        Form validation
    */
    $('.login-form input[type="text"], .login-form input[type="password"], .login-form textarea').on('focus', function() {
    	$(this).removeClass('has-error');
    });

    $('.login-form input[type="text"], .login-form textarea').on('focus', function() {
        $(this).removeClass('has-error');
    });
    
	$('.login-form').on('submit', function(e) {
	
		$(this).find('input[type="text"], input[type="password"], textarea').each(function(){
			if( $(this).val() == "" ) {
				e.preventDefault();
				$(this).addClass('has-error');
			}
			else {
				$(this).removeClass('has-error');
			}
		});
	
	});
    
});
