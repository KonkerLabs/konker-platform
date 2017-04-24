var toggle = false;
$("#isJsonFormatted").click(function () {
    $("#isJsonFormatted").attr("checked", !toggle);
    toggle = !toggle;
    formatJson(toggle);
});

function formatJson(isFormatted) {
    $.each($(".json-data"), function () {
            var payload = $(this).html();
            if (isFormatted) {
                if($(this).attr("text")) {
                    // json already formatted
                    return;
                }
                try {
                    var result = JSON.parse(payload);
                } catch (e) {

                }
                $(this).attr("text", payload);
                var node = new PrettyJSON.view.Node({
                    el: $(this),
                    data: result
                });
                node.expandAll();
            }
            else {
                var unformattedJSON = $(this).attr("text");
                $(this).html(unformattedJSON);
            }
        }
    );
}