var selectedMessageUUID = undefined;

function refreshSummary(messages) {
	$('#notification-summary').empty();
	$.each(messages, function(index, message) {
		$('#notification-summary').append(
	    		  '<a href="javascript:loadMessage(\'' + message.uuid + '\')" class="list-group-item notification-summary-item" id="' + message.uuid +'"><p>' + message.subject  + '</p>' +
                  '<p><small>' + message.dateTime + '</small></p></a>');
	    }
	);
	loadMessage(messages[0].uuid);
}

function loadMessage(uuid) {
	selectedMessageUUID = uuid;
	$('#notification-summary > .active').removeClass('active');
	$.ajax("/notifications/" + uuid, {
		"dataType": "json",
		"success": function(x) {
			renderMessage(x);
		} 
	});
}

function renderMessage(message) {
	$('#'+message.uuid).addClass('active');

	$('#notification-subject').empty();
	$('#notification-subject').append(message.subject);

	$('#notification-date').empty();
	$('#notification-date').append(message.dateTime);

	
	$('#notification-body').empty();
	$('#notification-body').append(message.body);
	
}

function markRead(uuid) {
	$.ajax("/notifications/" + uuid, {
		"dataType": "json",
		"data": {"unread": false},
		"success": function(x) {
			renderMessage(x);
		} 
	});
}

function markUnread(uuid) {
	$.ajax("/notifications/" + uuid, {
		"dataType": "json",
		"data": {"unread": false},
		"success": function(x) {
			renderMessage(x);
		} 
	});
}

$('#mark-read-button').click(function() {
	alert('x');
	markRead(selectedMessageUUID);
});

$('#mark-unread-button').click(function() {
	markUnread(selectedMessageUUID);
});


$.ajax("/notifications", {
	"dataType": "json",
	"success": function(x) {
		refreshSummary(x);
	} 
});

