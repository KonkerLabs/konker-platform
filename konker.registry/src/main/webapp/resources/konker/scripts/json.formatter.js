$.each($(".json-data"), function () {
        var payload = $(this).html();
        var obj = JSON.parse(payload);

        var node = new PrettyJSON.view.Node({
        el:$(this),
        data:obj
        });

        node.expandAll();
    }
);