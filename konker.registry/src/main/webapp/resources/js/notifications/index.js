var selectedMessageUUID = undefined;
var messageReadTimer = undefined;

var dataStore = {
    listMessages: function() {
        return axios.get("/notifications", { "responseType": "json" })
    },
    markAllRead: function() {
        return axios.post("/notifications", { "allRead": true }, { "responseType": "json" })
    },
    loadMessage: function(uuid) {
        return axios.get("/notifications/" + uuid, { "responseType": "json" })
    },
    markRead: function(uuid) {
        return axios.post("/notifications/" + uuid, { "unread": false }, { "responseType": "json" })
    },
    markUnread: function(uuid) {
        return axios.post("/notifications/" + uuid, { "unread": true }, { "responseType": "json" })
    }
}

function renderMessageList(messages, autoRead) {
    return new Promise(function(resolve, reject) {
        $('#notification-summary').empty();
        $.each(messages, function(index, message) {
            $('#notification-summary').append(
                '<a href="#" class="list-group-item notification-summary-item' + (message.unread ? ' message-unread' : '') + '" id="' + message.uuid + '"><p>' + message.subject + '</p>' +
                '<p><small>' + message.dateTime + '</small></p></a>');

        });

        $('.notification-summary-item').click({ 'autoRead': autoRead }, function(event) {
            selectMessage($(this).context.id, event.data.autoRead);
        });

        if (!$.isEmptyObject(messages)) {
            selectMessage(messages[0].uuid, autoRead);
        } else {
            renderEmptyMessage();
        }

        return resolve(messages);
    });
}


function clearActiveMessage() {
    if (messageReadTimer) {
        console.log("Clear timeout...")
        clearTimeout(messageReadTimer);
    }

    $('#notification-summary > .active').removeClass('active');

    selectedMessageUUID = undefined;
}

function selectMessage(uuid, autoRead) {
    clearActiveMessage();
    selectedMessageUUID = uuid;

    p = dataStore.loadMessage(uuid).then(function(x) {
        return renderMessage(x.data)
    });
    $('#' + uuid).addClass('active');

    if (autoRead > 0) {
        p.then(function(x) {
            messageReadTimer = setTimeout(function(uuid) {
                if ($('#' + uuid).hasClass('message-unread')) {
                    dataStore.markRead(uuid).then(function(response) {
                        return renderMessage(response.data)
                    }).then(function(msg) {
                        $('#' + msg.uuid).removeClass('message-unread')
                    });
                }
            }, 3000, selectedMessageUUID);
        });
    }
}

function renderEmptyMessage() {
    $('#notification-subject').html("");
    $('#notification-date').html("");
    $('#notification-body').html("No message selected");
    return Promise.resolve(message);
}

function renderMessage(message) {
    $('#notification-subject').html(message.subject);
    $('#notification-date').html(message.dateTime);
    $('#notification-body').html(message.body);
    if (message.unread) {
        $('#mark-unread-btn').hide();
        $('#mark-read-btn').show();
    } else {
        $('#mark-read-btn').hide();
        $('#mark-unread-btn').show();
    }
    return Promise.resolve(message);
}


$('#mark-read-btn').click(function() {
    clearTimeout(messageReadTimer);
    dataStore.markRead(selectedMessageUUID)
        .then(function(response) renderMessage(response.data))
        .then(function(msg) { $('#' + msg.uuid).removeClass('message-unread') });
});

$('#mark-unread-btn').click(function() {
    clearTimeout(messageReadTimer);
    dataStore.markUnread(selectedMessageUUID)
        .then(function(response) renderMessage(response.data))
        .then(function(msg) { $('#' + msg.uuid).addClass('message-unread') });
});

$('#mark-all-read-btn').click(function() {
    clearTimeout(messageReadTimer);
    dataStore.markAllRead().then(function(x) { renderMessageList(x.data, 3000) });
});

dataStore.listMessages().then(function(x) { renderMessageList(x.data, 3000) }).catch(function(error) { console.log(error); });
