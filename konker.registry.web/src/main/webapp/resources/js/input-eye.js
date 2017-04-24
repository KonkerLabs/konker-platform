/* PASSWORD INPUT */

$(document).ready( function () {
    $('.password-eye').each(function() {
        var input = $(this).parent().find('input');
        $(input).attr('type', 'password');
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

    // KRMVP-463

    $('form').submit(function() {
        var form = this;
        $(form).find('input[type="text"].password-input').each(function() {
            var input = this;
            $(input).each(function() {
                if ($(input).val() === ' ') {
                    $(input).val('');
                };
            });
        });
    });

    $('input[type="text"].password-input').each(function(){
        var input = this;
        if ($(input).val() === '') {
            $(input).val(' ');
        };
    });

    $('input[type="text"].password-input').focusin(function(){
        var input = this;
        if ($(input).val() === ' ') {
            $(input).val('');
        };
    });

    $('input[type="text"].password-input').focusout(function(){
        var input = this;
        if ($(input).val() === '') {
            $(input).val(' ');
        };
    });

});


