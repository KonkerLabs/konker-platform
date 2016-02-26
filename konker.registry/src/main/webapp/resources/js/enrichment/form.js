function addParameter() {
    var table = document.getElementById("parameters");
    var row = table.insertRow(table.rows.length);
    var cell1 = row.insertCell(0);
    var cell2 = row.insertCell(1);
    cell1.innerHTML = "Type a key....";
    cell2.innerHTML = "Type a value....";
    row.attr("contenteditable", true);

}


