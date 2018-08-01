$(document).ready(function () {

    function loadStatusInfo(divStatus) {
        var application = divStatus.attr('application');
        var guid = divStatus.attr('guid');

        var base = urlTo('/devices/' + application + '/' + guid + '/status');

        $.ajax({
            context: this,
            type: "GET",
            url: base,
            dataType: "json",
            timeout: 100000,
            beforeSend: function () {
            },
            success: function (data) {
                var status = data['status'];
                var deviceStatus = status.toLowerCase();

                html =
                    "<div class='badge-health " + deviceStatus + "'>" +
                    "        <div class='icon-badge-local " + deviceStatus + "-bg-icon'>" +
                    "            <img src='" + urlTo('/resources/konker/images/icon-health/' + deviceStatus + '-icon-health.svg') + "' class='icon-health'>" +
                    "        </div>" +
                    "        <label class='status-health'>" + status + "</label>" +
                    "</div>";

                divStatus.html(html);
            },
            complete: function () {
            }
        });
    }

    $(".device-status").each(function (index) {
        var divStatus = $(this);
        loadStatusInfo(divStatus);
    });

})