function loadCSV() {
	$.ajax({
		context : this,
        type : "GET",
        url : urlTo('/devices/visualization/csv/download'),
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
    if (!$('#channel').val() === false &&
        !$('#metric').val() === false) {
        findAndLoadDataChart();
        loadIncomingEvents();
        loadOutgoingEvents();
    }
}

function findAndLoadDataChart() {
	var url = urlTo('/devices/visualization/load/');
    $.ajax({
        context : this,
        type : "GET",
        url : url,
        dataType: "html",
        timeout : 100000,
        data: $('#visualizationForm').serialize(),
        beforeSend : function() {
            $("div .loading-chart").removeClass('hide');
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

        		if (result.length != 0) {
        			$('#exportCsv').removeClass('hide');
                    $('#chart').removeClass('hide');
                    $('#onlineRow').removeClass('hide');
                    // Used to identify outliers
                    // var outliers = data_filter(result);
                    // graphService.update($('#metric option:selected').val(), outliers);
                    graphService.update($('#metric option:selected').val(),result);
                }
        	}

        },
        complete : function() {
            $("div .loading-chart").addClass('hide');

            if ($('.nv-noData').length) {
                clearChartTableHideCsvButton();
            }
        }
    });
}

function beautifierJson() {
    formatJson($("#isJsonFormatted").is(':checked'));
}

function fetchMetricViewFragment(scheme, fetchUrl, element) {
    var loadSpinner;
    
    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        data: scheme,
        beforeSend : function() {
            loadSpinner = setTimeout(function() {
                $("div.ajax-loading").addClass('show');
            }, 50);
        },
        success : function(data) {
            displayFragment(element, data);
            applyEventBindingsToMetric();
            autoRefreshDataChart();
        },
        complete : function() {
            clearTimeout(loadSpinner);
            $("div.ajax-loading").removeClass('show');
        }
    });
}

function fetchEventsViewFragment(scheme, fetchUrl, element) {

    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        data: scheme,
        beforeSend : function() {
        },
        success : function(data) {
            displayFragment(element, data);
            beautifierJson();
        },
        complete : function() {
        }
    });
    
}


function applyEventBindingsToMetric() {
	$('#metric').change(function() {
	    clearChartTableHideCsvButton();
		findAndLoadDataChart();
	});
}

function applyEventBindingsToChannel() {
    $('#channel').change(function() {
        clearChartTableHideCsvButton();

        var scheme = $('#visualizationForm').serialize();
        var url = '/devices/visualization/loading/metrics/';
        var element = '#div-metric';

        fetchMetricViewFragment(scheme, urlTo(url), element);

    });
}

function loadIncomingEvents() {
    var deviceGuid = $('#deviceGuid').val();

    var scheme = $('#visualizationForm').serialize();
    var url = '/devices/' + deviceGuid + '/events/incoming';
    var element = '#incoming';

    fetchEventsViewFragment(scheme, urlTo(url), element);
}

function loadOutgoingEvents() {
    var deviceGuid = $('#deviceGuid').val();

    var scheme = $('#visualizationForm').serialize();
    var url = '/devices/' + deviceGuid + '/events/outgoing';
    var element = '#outgoing';

    fetchEventsViewFragment(scheme, urlTo(url), element);
}

function clearChartTableHideCsvButton() {
    $('#chart').addClass('hide');
	$('#chart svg').html("");
	$('#exportCsv').addClass('hide');
}

function displayFragment(element, data) {
    $(element).html(data);
}

var chartRefreshService = {
    myInterval : null,
    processOnlineClick : function() {
        if ($('#online').is(':checked')) {
            $('input.date').attr('disabled', true);
            $('#updateChartBtn').addClass('hide');

            $('#dateStart').val('');
            $('#dateEnd').val('');

            autoRefreshDataChart();
            myInterval = setInterval(autoRefreshDataChart, 5000);
        } else {
            $('input.date').attr('disabled', false);
            $('#updateChartBtn').removeClass('hide');

            if (typeof myInterval !== 'undefined') {
                clearInterval(myInterval);
            }
        }
    }
}

$(document).ready(function() {

    applyEventBindingsToChannel();
    applyEventBindingsToMetric();
    autoRefreshDataChart();

    $('#dateStart').datetimepicker({
        format: "DD/MM/YYYY HH:mm:ss"
    });
    $('#dateStartGroup').datetimepicker({
        format: "DD/MM/YYYY HH:mm:ss"
    });

    $('#dateEnd').datetimepicker({
        format: "DD/MM/YYYY HH:mm:ss"
    });
    $('#dateEndGroup').datetimepicker({
        format: "DD/MM/YYYY HH:mm:ss"
    });

    // CSV
    $('#exportCsv').click(function(e) {
        loadCSV();
    });

    // Auto update
    chartRefreshService.processOnlineClick();

    $('#online').click(function() {
        chartRefreshService.processOnlineClick();
    });

    $('#updateChartBtn').click(function() {
        clearChartTableHideCsvButton();
        autoRefreshDataChart();
    });

});