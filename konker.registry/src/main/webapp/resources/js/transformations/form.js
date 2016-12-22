var controller = {
    tableRowCount : $('#transformationSteps > tbody > tr').length,
    addNewRow : function() {
        trLast = this.tableBody.find("tr:last"),
        trNew = trLast.clone();
        trLast.after(trNew);

        this.tableRowCount += 1;

        this.applyNewIndex(trNew, this.tableRowCount - 1);
    },
    applyNewIndex : function(row, index) {

    	row.find('input').each(function() {
            var input = $(this);
            var newId = input.attr('id').replace(/\d+/gi,index);
            var newName = input.attr('name').replace(/\d+/gi,index);
            input.attr('id',newId);
            input.attr('name',newName);
        });
    },
    removeRow : function(tableRow) {
        if (this.tableRowCount > 1) {
            tableRow.remove();
            this.tableRowCount -= 1;
            this.reindexRows();
        }
    },
    reindexRows : function() {
        var tableRows = $('#transformationSteps > tbody > tr');
        tableRows.each( function(index) {
            controller.applyNewIndex($(this), index);
        });
    },
}

$(document).ready(function() {
    controller.tableBody = $('tbody');

    $('#transformationSteps').on('click','#btn-add',function() {
        controller.addNewRow();
    });

    $('#transformationSteps').on('click','button.remove',function() {
        var row = $(this).closest('tr');
        controller.removeRow(row);
    });
    
    $('#btnYes').click(function() {
    	$('#removeItemModal').modal('hide');
    	$("input[type=hidden][name=_method]").val('delete');
    	$('form').submit();
    });
});