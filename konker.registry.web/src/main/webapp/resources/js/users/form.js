var handleFileSelect = function(evt) {
    var files = evt.target.files;
    var file = files[0];
    var fileNameAndType = file.name.split(".")
    var fileType = fileNameAndType[fileNameAndType.length -1];
    var fileEncode = "base64";
    var basePath = "data:image/{fileType};{fileEncode},";

    if (files && file) {
        var reader = new FileReader();

        reader.onload = function(readerEvt) {
            var binaryString = readerEvt.target.result;
            document.getElementById("avatar").value =
            basePath
                .replace("{fileType}", fileType)
                .replace("{fileEncode}", fileEncode) +
            btoa(binaryString);
        };

        reader.readAsBinaryString(file);
    }
};

if (window.File && window.FileReader && window.FileList && window.Blob) {
    if (document.getElementById('filePicker') !== null)
        document.getElementById('filePicker').addEventListener('change', handleFileSelect, false);
} else {
    alert('The File APIs are not fully supported in this browser.');
}

// show and hide view and edit divs
var oldValues;

$(".btn-edit-user").click(function() {
	var panel = this.closest(".panel-user-fields");

	// save current values
	oldValues = $($(panel).find(".panel-edit-user")).clone(true, true);

	$(panel).find(".panel-view-user").hide();
    $(panel).find(".panel-change-plan").hide();
	$(panel).find(".panel-edit-user").show();
	$(".btn-edit-user").hide();

});

$(".btn-cancel-edit-user").click(function() {
	var panel = this.closest(".panel-user-fields");

	// restore previous values
	$(panel).find(".panel-edit-user").replaceWith(oldValues);

	$(panel).find(".panel-view-user").show();
    $(panel).find(".panel-change-plan").show();
	$(panel).find(".panel-edit-user").hide();
	$(".btn-edit-user").show();

});
