$(document).ready( function () {
	$(".switch input[type=hidden]").before($('<div class="slider round"></div>'));
});

/* PASSWORD INPUT */

$(document).ready( function () {
    $('.password-eye').each(function() {
        var input = $(this).parent().find('input');
        $(input).attr('type', 'password');
    });
});

$('.password-eye').mousedown(function() {
    var input = $(this).parent().find('input');
    $(input).attr('type', 'text');
});

$('.password-eye').mouseup(function() {
    var input = $(this).parent().find('input');
    $(input).attr('type', 'password');
});

$('.password-eye').mousemove(function() {
    var input = $(this).parent().find('input');
    $(input).attr('type', 'password');
});
