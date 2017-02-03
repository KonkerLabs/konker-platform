function get_segments(data) {
    var segments = {};
    if (data.length > 1){
        var previousData = null;
        for (var i = data.length - 1; i >= 0; i--) {
            var currentData = data[i];
            if (previousData == null) {
                previousData = currentData;
            } else {
                var segment = currentData["timestamp"] - previousData["timestamp"];
                segments[segment] = {
                    "d1": previousData,
                    "d2": currentData
                };
                previousData = currentData;
            }
        }
    }
    return segments;
}

function get_outliers(segments) {
    var median = math.median(Object.keys(segments));
    var values = Object.keys(segments)
    var q1 = d3.quantile(values, 0.25);
    var q3 = d3.quantile(values, 0.75);
    var iqr = q3 - q1;
    var outlier = median + 2.5 * iqr;

    var keys = Object.keys(segments);
    var outliers = {};
    for (var i = 0; i <  keys.length; i++) {
        if (keys[i] > outlier) {
            outliers[keys[i]] = segments[keys[i]];
        }
    }

    return outliers;
}

function data_filter(data) {
    var segments = get_segments(data);
    var outliers = get_outliers(segments);
    return filter(outliers, data);
}

function filter(outliers, data) {
    var result = []

    var set = [];
    var previousIsOutlier = false;
    for (var i = 0; i < data.length; i++) {
        if (i < data.length - 1) {
            var distance =  data[i]["timestamp"] - data[i + 1]["timestamp"];
            if (outliers[distance] != undefined) {
                if (!previousIsOutlier) {
                    set.push(data[i]);
                    result.push(set);
                    set = [];
                } else {
                    result.push(set);
                    set = [];

                    // last outlier
                    if (i == data.length - 2){
                        result.push([data[i]]);
                        result.push([data[i + 1]]);
                    }
                }
                set.push(data[i + 1]);
                i = i + 1;
                previousIsOutlier = true;
            } else {
                set.push(data[i]);
                previousIsOutlier = false;
            }
        }
    }

    return result;
}