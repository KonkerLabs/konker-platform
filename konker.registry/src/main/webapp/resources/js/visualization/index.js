$('.date').datetimepicker({
	format: "DD/MM/YYYY HH:mm:ss"
});

$('button.btn-success').click(function() {
	findAndLoadDataChart();
});

$("<link/>", {
    rel: "stylesheet",
    type: "text/css",
    href: "resources/konker/css/pretty-json.css"
}).appendTo("head");
$.getScript( "resources/konker/scripts/json.formatter.js");

$('#exportCsv').click(function(e) {
	loadCSV();
});

function loadCSV() {
	$.ajax({
		context : this,
        type : "GET",
        url : urlTo('/visualization/csv/download'),
        contentType: "application/json",
        dataType: "json",
        timeout : 100000,
        data: $('#visualizationForm').serialize(),
        beforeSend : function(xhrObj) {
        },
        success : function(data) {
        },
        complete : function(data) {
        	$('#bufferCsv').attr('href', 'data:text/csv;charset=utf8,' + encodeURIComponent(data.responseText))
        		.attr('download', 'Events.csv');
        	$('#bufferCsv')[0].click()
        }
    });
}

function autoRefreshDataChart() {
    if (!$('#device').val() === false &&
        !$('#channel').val() === false &&
        !$('#metric').val() === false) {
        findAndLoadDataChart();		
    }
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
            var result;
            try {
                result = jQuery.parseJSON(data);
            } catch (e) {

            }

        	if (result.length > 0 && result[0].message != null) {
        		$('div .alert.alert-danger').removeClass('hide');
        		$('div .alert.alert-danger li').html(result[0].message);
        		$('#exportCsv').addClass('hide');
        	} else {
        		$('div .alert.alert-danger').addClass('hide');
        		
        		var tableData = "";
        		$.each(result, function(index, value) {
        			var json = JSON.stringify(value.payload).replace(/\\/g, '');
        			json = json.replace(/\"{/g, '{');
        			json = json.replace(/\}"/g, '}');
        			tableData = tableData + '<tr><td>'+value.timestampFormated+'</td><td class="json-data">'+json+'</td></tr>';
        		});
        		$("#data-event table tbody").html(tableData);
        		formatJson($("#isJsonFormatted").attr("checked"));
        		
        		if (result.length != 0) {
        			$('#exportCsv').removeClass('hide');
        		} 
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
		
		myInterval = setInterval(autoRefreshDataChart, 5000);
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

$('#device').change(function() {
    renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/loading/channel/', '#div-channel');
    clearMetricSelect();
    clearChartTableHideCsvButton();

});

function applyEventBindingsToChannel() {
	$('#channel').change(function() {
		renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/loading/metrics/', '#div-metric');
		clearChartTableHideCsvButton();
	});

}

function applyEventBindingsToMetric() {
	$('#metric').change(function() {
		findAndLoadDataChart();
	});
}

function selectFirstOption(selectName) {

    if ($("select[name=" + selectName + "] option").length === 2) {
        var value = $("select[name=" + selectName + "] option")[1].value;
        $("select[name=" + selectName + "]").val(value);
        $("select[name=" + selectName + "]").change();
    }

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