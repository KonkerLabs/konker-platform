$(document).ready( function () {
	$(".switch input[type=hidden]").before($('<div class="slider round"></div>'));
});

/* footer */

function locateFooter() {
    /* footer */
    var windowHeight = $(window).height();
    var docHeight = $('.wrapper').height();
    var headerHeight = $('.header').height();

    console.log('windowHeight: ' + windowHeight);
    console.log('docHeight: ' + docHeight);
    console.log('headerHeight: ' + headerHeight);

    if (windowHeight > docHeight + headerHeight + 80) {
        $('#footer').css('bottom', '0px');
    } else {
        $('#footer').css('bottom', 'auto');
    };
}

$(document).ready(function() {
    locateFooter();
});

$(window).resize(function() {
    locateFooter();
});