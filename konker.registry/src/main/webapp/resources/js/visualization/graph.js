function getByPath(o, s) {
    s = s.replace(/\[(\w+)\]/g, '.$1');
    s = s.replace(/^\./, '');
    var a = s.split('.');
    for (var i = 0, n = a.length; i < n; ++i) {
        var k = a[i];
        if (k in o) {
            o = o[k];
        } else {
            return;
        }
    }
    return o;
}

var insertLinebreaks = function (d) {
    var el = d3.select(this);
    var words = d3.select(this).text().split(' ');
    el.text('');

    for (var i = 0; i < words.length; i++) {
        var tspan = el.append('tspan').text(words[i]);
        tspan.attr('x', 0).attr('dy', '16');
    }
};

function improveChart() {

    // break lines
    $('.nv-x .tick text').each(insertLinebreaks);
    $('.nv-axisMaxMin-x text').each(insertLinebreaks);
    $('.nv-x .nv-axislabel').attr('y', 60);

}

var graphService = {
    data : [],
    buildChart : function() {
        nv.addGraph(function() {

            var chart;
            var controller = graphService;

            chart = nv.models.lineChart()
                .options({
                    duration: 200,
                    useInteractiveGuideline: true
                });

            chart.noData(controller.noDataMessage);

            chart.margin({"bottom":120, "right": 50})

            chart.xAxis
              .axisLabel(controller.xAxisLabel)
              .rotateLabels(0)
              .showMaxMin(false)
              .tickFormat(function(d) {
                return d3.time.format('%d/%m/%Y %X')(new Date(d))
              });
            chart.yAxis
                .axisLabel(controller.field)
                .tickFormat(function(d) {
                    if (d == null) {
                        return 'N/A';
                    }
                    return d3.format(',.2f')(d);
                })
            ;
            controller.chart = chart;

            d3.select('#chart svg')
                .datum(controller.data)
                .call(controller.chart);
            nv.utils.windowResize(controller.chart.update);
            
            improveChart();

            return controller.chart;
        });
    },
    prepare : function(data) {
        var points = [];
        data.reverse();

        for (var i = 0; i < data.length; i++) {
            var value = getByPath(JSON.parse(data[i].payload),graphService.field)

            if (!isNaN(value)) {
                var d = new Date(0);
                d.setUTCSeconds(data[i].timestamp / 1000)
                points.push({x: d, y: value});
            }
        }

        return [
            {
                values : points,
                key : graphService.field,
                color : graphService.lineColor
            }
        ];
    },
    update : function(field,data) {
        if (this.chart == null || this.field != field) {
            this.field = field;
            this.buildChart();
        }

        this.data = this.prepare(data);
        // Update the SVG with the new data and call chart
        d3.select('#chart svg').datum(this.data).call(this.chart);
        nv.utils.windowResize(this.chart.update);
        improveChart();
        
    },
    setup : function(lineColor,xAxisLabel,noDataMessage) {
        this.lineColor = lineColor;
        this.xAxisLabel = xAxisLabel;
        this.noDataMessage = noDataMessage;
    }
}