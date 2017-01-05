$.each($(".payload"), function () {
        var payload = $(this).html();
        var obj = JSON.parse(payload);
        $(this).html(JSON.stringify(obj, null, 4));
        $(this).css({'white-space': 'pre'})
    }
);