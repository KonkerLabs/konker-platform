var selectedMessageUUID = undefined;
var messageReadTimer = undefined;

var notificationStatusDataStore = {
    checkNewNotifications: function() {
        return $.getJSON(urlTo("/notification-status"));
    },
    unmarkNewNotifications: function() {
        return $.ajax(urlTo("/notification-status"), {
            "data": JSON.stringify({ "hasNewNotifications": false }),
            "dataType": "json",
            "type": "POST",
            "contentType": "application/json"
        });
    }
}

var notificationStatus = {
    refresh: function() {
        notificationStatusDataStore.checkNewNotifications().then(function(data) {
            // new template
            if (data.hasNewNotifications) {
                $('#notification-icon-off').hide();
                $('#notification-icon-on').show();
            } else {
                $('#notification-icon-off').show();
                $('#notification-icon-on').hide();
            }

        }, function(error) { console.log(error) });
    },
    unmarkNewNotifications: function() {
        notificationStatusDataStore.unmarkNewNotifications().then(function(data) { $('#notification-icon-on').hide();
            $('#notification-icon-off').show(); }, function(error) { console.log(error) });
    }
}

notificationStatus.refresh();
setInterval(notificationStatus.refresh, 60000);
