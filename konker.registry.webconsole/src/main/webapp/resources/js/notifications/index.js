var selectedMessageUUID = undefined;
var messageReadTimer = undefined;

var notificationDataStore = {
    listMessages: function() {
        return axios.get(urlTo("/notifications"), { "responseType": "json" })
    },
    markAllRead: function() {
        return axios.post(urlTo("/notifications"), { "allRead": true }, { "responseType": "json" })
    },
    loadMessage: function(uuid) {
        return axios.get(urlTo("/notifications/" + uuid), { "responseType": "json" })
    },
    markRead: function(uuid) {
        return axios.post(urlTo("/notifications/" + uuid), { "unread": false }, { "responseType": "json" })
    },
    markUnread: function(uuid) {
        return axios.post(urlTo("/notifications/" + uuid), { "unread": true }, { "responseType": "json" })
    },
    sendTestMessage: function(subject, body) {
        return axios.put(urlTo("/notifications"), { "subject": subject, "body": body }, { "responseType": "json" })
    }
}

function renderMessageList(messages, autoRead) {
    return new Promise(function(resolve, reject) {
        $('#notification-summary').empty();
        $.each(messages, function(index, message) {
            $('#notification-summary').append(Mustache.render($('#notification-header-template').html(), $.extend({}, message, mustacheTranslation)));
            $('#' + message.uuid).toggleClass('notification-unread', message.unread);
        });

        $('.notification-summary-item').click({ 'autoRead': autoRead }, function(event) {
            selectMessage($(this).context.id, event.data.autoRead);
        });

        if (!$.isEmptyObject(messages)) {
            selectMessage(messages[0].uuid, autoRead);
        } else {
            renderEmptyMessage();
        }

        notificationStatus.unmarkNewNotifications();

        return resolve(messages);
    });
}


function clearActiveMessage() {
    if (messageReadTimer) {
        clearTimeout(messageReadTimer);
    }

    $('#notification-summary > .active').removeClass('active');

    selectedMessageUUID = undefined;
}

function selectMessage(uuid, autoRead) {
    clearActiveMessage();
    selectedMessageUUID = uuid;

    p = notificationDataStore.loadMessage(uuid).then(function(x) {
        return renderMessage(x.data)
    });
    $('#' + uuid).addClass('active');

    if (autoRead > 0) {
        p.then(function(x) {
            messageReadTimer = setTimeout(function(uuid) {
                if ($('#' + uuid).hasClass('notification-unread')) {
                    notificationDataStore.markRead(uuid).then(function(response) {
                        return renderMessage(response.data)
                    }).then(function(msg) {
                        $('#' + msg.uuid).removeClass('notification-unread')
                    });
                }
            }, 3000, selectedMessageUUID);
        });
    }
}

function renderEmptyMessage() {
    $('#notification-target').html("");
}

function renderMessage(message) {
    $('#notification-target').html(Mustache.render($('#notification-template').html(), $.extend({}, message, mustacheTranslation)));

    $('#mark-read-btn').click(function() {
        clearTimeout(messageReadTimer);
        notificationDataStore.markRead(selectedMessageUUID)
            .then(function(response) { renderMessage(response.data) })
            .then(function(msg) { $('#' + selectedMessageUUID).removeClass('notification-unread') });
    });

    $('#mark-unread-btn').click(function() {
        clearTimeout(messageReadTimer);
        notificationDataStore.markUnread(selectedMessageUUID)
            .then(function(response) { renderMessage(response.data) })
            .then(function(msg) { $('#' + selectedMessageUUID).addClass('notification-unread') });
    });

    if (message.unread) {
        $('#mark-unread-btn').hide();
        $('#mark-read-btn').show();
    } else {
        $('#mark-read-btn').hide();
        $('#mark-unread-btn').show();
    }
    return Promise.resolve(message);
}


function notificationSetup() {

    $('#mark-all-read-btn').click(function() {
        clearTimeout(messageReadTimer);
        notificationDataStore.markAllRead().then(function(x) { renderMessageList(x.data, 3000) });
    });

    notificationDataStore.listMessages().then(function(x) { renderMessageList(x.data, 3000) });
}

notificationSetup();
