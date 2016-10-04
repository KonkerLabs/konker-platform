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

//            chart.margin({"bottom":200})

            chart.xAxis
              .axisLabel(controller.xAxisLabel)
//              .rotateLabels(-45)
              .tickFormat(function(d) {
                return d3.time.format('%x %X')(new Date(d))
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

            d3.select('#graph svg')
                .datum(controller.data)
                .call(controller.chart);
            nv.utils.windowResize(controller.chart.update);

            return controller.chart;
        });
    },
    prepare : function(data) {
        var points = [];

        for (var i = 0; i < data.length; i++) {
            var value = getByPath(data[i].payload,graphService.field)

            if (!isNaN(value)) {
                points.push({x: new Date(data[i].ts), y: value});
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
        d3.select('#graph svg').datum(this.data).call(this.chart);
        nv.utils.windowResize(this.chart.update);
    },
    setup : function(fetchUrl,lineColor,xAxisLabel) {
        this.fetchUrl = fetchUrl;
        this.lineColor = lineColor;
        this.xAxisLabel = xAxisLabel;
    }
}