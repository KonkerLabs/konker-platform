$(document).ready(function() {
    $('#transformationSteps').on('click','#btn-add',function() {
        var table = $(this).closest('table');
        addNewRow(table);
    });
    $('#transformationSteps').on('click','button.remove',function() {
        var row = $(this).closest('tr');
        removeRow(row);
    });
});

function addNewRow(table) {
    var allTrs = table.find('tbody').find('tr');
    var lastTr = allTrs[allTrs.length-1];
    var $clone = $(lastTr).clone();
    $clone.find('input').val('');
    table.append($clone);

    applyNewIndex($clone, allTrs.length);
}

function applyNewIndex(row,index) {
    row.find('input').each(function() {
        var input = $(this);
        var newId = input.attr('id').replace(/\d/gi,index);
        var newName = input.attr('name').replace(/\d/gi,index);
        input.attr('id',newId);
        input.attr('name',newName);
    });
}

function removeRow(tableRow) {
    var tableRowCount = tableRow.closest('tbody').find('tr').length;
    if (tableRowCount > 1) {
        tableRow.remove();
        reindexRows();
    }
}

function reindexRows() {
    var tableRows = $('#transformationSteps > tbody > tr');
    var rowCount = tableRows.length;
    tableRows.each( function(index) {
        applyNewIndex($(this),index);
    });
}