function getSegments(data) {
    var segments = [];
    
    for (var i = 1; i < data.length; i++) {
        segments.push(data[i - 1]["timestamp"] - data[i]["timestamp"]);
        console.log(data[i - 1]["timestamp"] - data[i]["timestamp"]);
    }

    segments.sort(function(a,b) { return a - b; });

    return segments;
}

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
}

function data_filter(data) {
    var segments = getSegments(data);
    var outlierLimit = getOutlierLimit(segments);
    return filter(outlierLimit, data);
}

function filter(outlierLimit, data) {
    var result = []
    var set = [];

    set.push(data[0]);
    result.push(set);

    for (var i = 1; i < data.length; i++) {
        if (i < data.length - 1) {
            var distance =  data[i - 1]["timestamp"] - data[i]["timestamp"];
            if (distance > outlierLimit) {
                set = [];
                set.push(data[i]);
                result.push(set);
            } else {
                set.push(data[i]);
            }
        }
    }

    console.log('results: ' + result.length);
    
    return result;
}