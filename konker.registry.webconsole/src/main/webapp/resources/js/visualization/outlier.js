function DataFilter(data) {

	var obj = {};
	var segments;
	var outliers;

	function getSegments(data) {
	    var segments = [];

	    for (var i = 1; i < data.length; i++) {
	        segments.push(data[i - 1]["timestamp"] - data[i]["timestamp"]);
	    }

	    segments.sort(function(a,b) { return a - b; });

	    return segments;
	};

	function getOutlierLimit(values) {
	    var median = d3.median(values);
	    var q3 = d3.quantile(values, 0.75);

	    if (values.length < 10) {
	        // no outliers: small sample
	        outlier = d3.max(values) + median;
	    } else {
	        outlier = median + 10.0 * q3;
	    }

	    return outlier;
	};

	function filter(outlierLimit, data) {
	    var resultSegments = [];
	    var resultOutliers = [];
	    var set = [];

	    set.push(data[0]);
	    resultSegments.push(set);

	    for (var i = 1; i < data.length; i++) {
	        if (i < data.length - 1) {
	            var distance =  data[i - 1]["timestamp"] - data[i]["timestamp"];
	            if (distance > outlierLimit) {
	                set = [];
	                set.push(data[i]);
	                resultSegments.push(set);

	                setOut = [];
	                setOut.push(data[i]);
	                setOut.push(data[i - 1]);
	                resultOutliers.push(setOut);
	            } else {
	                set.push(data[i]);
	            }
	        }
	    }

	    segments = resultSegments;
	    outliers = resultOutliers;
	}

    var allSegments = getSegments(data);
    var outlierLimit = getOutlierLimit(allSegments);
    filter(outlierLimit, data);

    obj.segments = segments;
    obj.outliers = outliers;

    return obj;

}
