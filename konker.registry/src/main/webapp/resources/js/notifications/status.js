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
            $('#notification-icon').toggleClass("new-notifications", data.hasNewNotifications)
        }, function(error) { console.log(error) });
    },
    unmarkNewNotifications: function() {
        notificationStatusDataStore.unmarkNewNotifications().then(function(data) { $('#notification-icon').removeClass("new-notifications") }, function(error) { console.log(error) });
    }
}

notificationStatus.refresh();
setInterval(notificationStatus.refresh, 60000);