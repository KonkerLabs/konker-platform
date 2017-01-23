var controller = {
    addNewRow : function(trLast, rowCount, callback) {
        trNew = trLast.clone();
        trLast.after(trNew);
        this.applyNewIndex(trNew, rowCount + 1);
        callback(trNew);
    },
    applyNewIndex : function(row, index) {

    	row.find('select, input').each(function() {
            var input = $(this);
            var newId = input.attr('id').replace(/\d+/gi,index);
            var newName = input.attr('name').replace(/\d+/gi,index);
            if(input.attr('name').indexOf('headers') != -1){
                newName = input.attr('name').split('headers')[0] +
                "headers" +
                input.attr('name').split('headers')[1].replace(/\d+/gi,index);
            }
            input.attr('id',newId);
            input.attr('name',newName);
        });
    },
    removeRow : function(tableRow, rowCount) {
        if (rowCount > 1) {
            tableRows = tableRow.parent();
            tableRow.remove();
            this.tableRowCount -= 1;
            this.reindexRows(tableRows);
        }
    },
    reindexRows : function(tableRows) {
        tableRows.each( function(index) {
            controller.applyNewIndex($(this), index);
        });
    },
}

$(document).ready(function() {
    controller.tableBody = $('tbody');

    $('#transformationSteps').on('click','#btn-add',function() {
        controller.addNewRow(
            controller.tableBody.find("tr.restparams:last"),
            $('#transformationSteps > tbody > tr.restparams').length,
            function(item){
                item.find('input[type=text]').each(function(input){
                    this.value = '';
                });
                item.find('button').on('click', function(){
                    var row = $(this).closest('tr');
                    controller.removeRow(row, $('#transformationSteps > tbody > tr.restparams').length);
                });
            });
    });
    $('.add-header').on('click',function() {
        controller.addNewRow(
            $(this).parent().parent().find("tr:last"),
            $('#restheaders').find('tr').length -1,
            function(item){
                item.find('input[type=text]').each(function(input){
                    this.value = '';
                });
                item.find('button').on('click', function(){
                    var row = $(this).closest('tr');
                    controller.removeRow(row, $('#restheaders').find('tr').length -1);
                });
            });
    });
    $('#transformationSteps').on('click','button.remove',function() {
        var row = $(this).closest('tr');
        controller.removeRow(row, $('#transformationSteps > tbody > tr.restparams').length -1);
    });
     $('#transformationStepHeaders').on('click','button.remove',function() {
        var row = $(this).closest('tr');
        controller.removeRow(row, $('#restheaders').find('tr').length -1);
     });


    
    $('#btnYes').click(function() {
    	$('#removeItemModal').modal('hide');
    	$("input[type=hidden][name=_method]").val('delete');
    	$('form').submit();
    });
});