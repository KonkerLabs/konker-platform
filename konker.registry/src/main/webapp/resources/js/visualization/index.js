$('.date').datetimepicker({
	format: "DD/MM/YYYY HH:mm:ss"
});

$('#device').change(function() {
	renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/loading/channel/', '#div-channel');
	clearMetricSelect();
	clearChartTableHideCsvButton();
});

$('button.btn-success').click(function() {
	findAndLoadDataChart();
});

function loadCSV() {
	$.ajax({
		context : this,
        type : "POST",
        url : urlTo('/visualization/csv/download'),
        contentType: "application/json",
        dataType: "json",
        timeout : 100000,
        data: $('#dataResult').val(),
        beforeSend : function(xhrObj) {
        },
        success : function(data) {
        },
        complete : function(data) {
        	$('#exportCsv').removeClass('hide');
        	$('#exportCsv').attr('href', 'data:text/csv;charset=utf8,' + encodeURIComponent(data.responseText))
        		.attr('download', 'Events.csv');
        }
    });
}

function findAndLoadDataChart() {
	var url = urlTo('/visualization/load/');
    $.ajax({
        context : this,
        type : "GET",
        url : url,
        dataType: "html",
        timeout : 100000,
        data: $('#visualizationForm').serialize(),
        beforeSend : function() {
            showElement('#loading');
        },
        success : function(data) {
        	var result = jQuery.parseJSON(data);
        	
        	if (result.length > 0 && result[0].message != null) {
        		$('div .alert.alert-danger').removeClass('hide');
        		$('div .alert.alert-danger li').html(result[0].message);
        	} else {
        		$('div .alert.alert-danger').addClass('hide');
        		$('#dataResult').val(data);
        		
        		var tableData = "";
        		$.each(result, function(index, value) {
        			var json = JSON.stringify(value.payload).replace(/\\/g, '');
        			json = json.replace(/\"{/g, '{');
        			json = json.replace(/\}"/g, '}');
        			tableData = tableData + '<tr><td>'+value.timestampFormated+'</td><td>'+json+'</td></tr>';
        		});
        		$("#data-event table tbody").html(tableData);
        		loadCSV();
        	}

        	graphService.update($('#metrics select').val(),result);
        },
        complete : function() {
            hideElement('#loading');
        }
    });
}

var myInterval;
$('#online').click(function() {
	if ($(this).is(':checked')) {
		$('.date input').attr('disabled', true);
		
		myInterval = setInterval(findAndLoadDataChart, 5000);
	} else {
		$('.date input').attr('disabled', false);
		clearInterval(myInterval);
	}
});
$('#online').click();

function renderOutgoingFragment(scheme, url, element) {
    var url = urlTo(url);

    fetchViewFragment(scheme, url, element);
}

function fetchViewFragment(scheme, fetchUrl, element) {
    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        data: scheme,
        beforeSend : function() {
            showElement('#loading');
        },
        success : function(data) {
            displayFragment(element, data);
            applyEventBindingsToChannel();
            applyEventBindingsToMetric();
        },
        complete : function() {
            hideElement('#loading');
        }
    });
}

function applyEventBindingsToChannel() {
	$('#channel').change(function() {
		renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/loading/metrics/', '#div-metric');
		
		clearChartTableHideCsvButton();
	});
}

function applyEventBindingsToMetric() {
	$('#metrics select').change(function() {
		findAndLoadDataChart();
	});
}

function clearChartTableHideCsvButton() {
	$('#chart svg').html("")
	$("#data-event table tbody").html("");
	$('#exportCsv').addClass('hide');
}

function clearMetricSelect() {
	$('#metric').html('<option value="">Select an option...</option>');
}

function displayFragment(element, data) {
    $(element).html(data);
}

function showElement(selector) {
    $(selector).show();
}

function hideElement(selector) {
    $(selector).hide();
}