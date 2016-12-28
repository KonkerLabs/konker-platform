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
    document.getElementById('filePicker').addEventListener('change', handleFileSelect, false);
} else {
    alert('The File APIs are not fully supported in this browser.');
}