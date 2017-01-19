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
    if (!$('#channel').val() === false &&
        !$('#metric').val() === false) {
        findAndLoadDataChart();
        loadIncomingEvents();
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
            $("img .loading-chart").removeClass('hide');
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

function renderOutgoingFragment(scheme, url, element) {
    var url = urlTo(url);

    fetchViewFragment(scheme, url, element);
}

function beautifierJson() {
    formatJson($("#isJsonFormatted").is(':checked'));
}

function fetchViewFragment(scheme, fetchUrl, element) {
    var loadSpinner;
    
    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        data: scheme,
        beforeSend : function() {
            //loadSpinner = setTimeout(function() {
            //    $("div.ajax-loading").addClass('show');
            //}, 50);
        },
        success : function(data) {
            displayFragment(element, data);
            applyEventBindingsToMetric();
            beautifierJson();
        },
        complete : function() {
            //clearTimeout(loadSpinner);
            //$("div.ajax-loading").removeClass('show');
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
        renderOutgoingFragment($('#visualizationForm').serialize(), '/visualization/loading/metrics/', '#div-metric');
        autoRefreshDataChart();
    });
}

function loadIncomingEvents() {
    var deviceGuid = $('#deviceGuid').val();
    renderOutgoingFragment('', '/devices/' + deviceGuid + '/events/incoming', '#incoming');
}

function selectFirstOption(selectName) {

    if ($("select[name=" + selectName + "] option").length === 2) {
        var value = $("select[name=" + selectName + "] option")[1].value;
        $("select[name=" + selectName + "]").val(value);
        $("select[name=" + selectName + "]").change();
    }

}

function clearChartTableHideCsvButton() {
    $('#chart').addClass('hide');
	$('#chart svg').html("");
	$("#data-event table tbody").html("");
	$('#exportCsv').addClass('hide');
	$('#onlineRow').addClass('hide');
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
            
            autoRefreshDataChart();
            myInterval = setInterval(autoRefreshDataChart, 5000);
        } else {
            $('input.date').attr('disabled', false);
            $('#updateChartBtn').removeClass('hide');
            
            clearInterval(myInterval);
        }
    }
}


$(document).ready(function() {

    applyEventBindingsToChannel();
    applyEventBindingsToMetric();
    autoRefreshDataChart();

    $('.date').datetimepicker({
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
        autoRefreshDataChart();
    });

});