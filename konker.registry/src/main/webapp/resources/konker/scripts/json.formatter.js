var toggle = false;
$("#isJsonFormatted").click(function () {
    $("input[type=checkbox]").attr("checked", !toggle);
    toggle = !toggle;
    formatJson(toggle);
});

function formatJson(isFormatted) {
    $.each($(".json-data"), function () {
            var payload = $(this).html();
            if (isFormatted) {
                var obj = JSON.parse(payload);
                $(this).attr("text", payload);
                var node = new PrettyJSON.view.Node({
                    el: $(this),
                    data: obj
                });
                node.expandAll();
            }
            else {
                var old = $(this).attr("text");
                $(this).html(old);
            }
        }
    );
}