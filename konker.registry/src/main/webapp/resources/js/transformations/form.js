var controller = {
    addNewRow : function(trLast, rowCount, callback) {
        trNew = trLast.clone();
        trLast.after(trNew);
        this.applyNewIndex(trNew, rowCount + 1);
        callback(trNew);
    },
    applyNewIndex : function(row, index) {

    	row.find('select, input, button, tr').each(function() {
            var input = $(this);
            var newId = 'undefined';
            if(input.attr('id') != undefined){
                if(input.attr('id').indexOf('.headers') != -1){
                       newId = input.attr('id').substring(0, 6).replace(/\d+/gi, index -1)
                        + input.attr('id').substring(6, input.attr('id').length).replace(/\d+/gi, index -1);
                } else {
                    newId = input.attr('id').replace(/\d+/gi, index -1);
                }
                input.attr('id', newId);
            }

            if(input.attr('name') != undefined){
                var newName = input.attr('name').replace(/\d+/gi, index -1);
                if(input.attr('name').indexOf('headers') != -1){

                    var firstIndex = $(this).attr('name').substring(0, 8).replace(/\d+/gi, index -1);
                    var secondIndex = $(this).attr('name').substring(8, $(this).attr('name').length)
                        .replace(/\d+/gi, index -1);

                    newName = firstIndex + secondIndex;
                }

                input.attr('name',newName);
            }

            if(input.attr('data-target') != undefined){
                input.attr('data-target', input.attr('data-target').replace(/\d+/gi, index-1));
            }

        });
    },
    removeRow : function(tableRow, rowCount, callback) {
        if (rowCount > 1) {
            tableRows = tableRow.parent();
            tableRow.remove();
            this.tableRowCount -= 1;
            this.reindexRows(tableRows);
        } else {
            tableRow.find('input').each(function(index, row){
                row.value = '';
            });
        }
        if(callback != undefined){
            callback();
        }
    },
    reindexRows : function(tableRows) {
        tableRows.each( function(index) {
            index++;
            controller.applyNewIndex($(this), index);
        });
    },
}

$(document).ready(function() {
    controller.tableBody = $('tbody');
    $('.transformationSteps .btn-add').on('click', function() {
        controller.addNewRow(
            $(this).parent().parent().parent().find('div.step:last'),
            $(this).parent().parent().parent().find('div.step').length,
            function(item){
                item.find('input[type=text]').each(function(input){
                    this.value = '';
                });
                item.find('button.remove-step').each(function(index, item){
                    $(item).on('click', function(){
                        var row = $(this).closest('div');
                        controller.removeRow(
                            row,
                            $(this).parent().parent().parent().parent().parent().parent().find('div.step').length,
                            function(){
                                $(this).parent().parent().parent().parent().parent().find('tr.header-line').each(function(index, headerRow){
                                    controller.removeRow(headerRow, $(this).parent().parent().parent().find('tr.header-line').length);
                                });
                            }
                        );
                    });
                });

                item.find('button.add-header').each(function(index, item){
                    $(item).on('click', function(){
                         controller.addNewRow(
                                    $(this).parent().parent().find("tr:last"),
                                    $(this).parent().parent().parent().find('tr.header-line').length +1,
                                    function(item){
                                        item.find('input[type=text]').each(function(input){
                                            this.value = '';
                                        });
                                        item.find('button.remove-header').on('click', function(){
                                            var row = $(this).closest('tr');
                                            controller.removeRow(row, $(this).parent().parent().parent().find('tr.header-line').length);
                                        });
                                    });
                    });
                });
                if(item.find('tr.restheaders tr.header-line').length > 1){
                    item.find('tr.restheaders tr.header-line').each(function(tr){
                        if(item.find('tr.restheaders tr.header-line').length > 1){
                            this.remove();
                        }
                    });
                }
            });
    });
    $('.add-header').on('click', function() {
        controller.addNewRow(
            $(this).parent().parent().find("tr:last"),
            $(this).parent().parent().parent().find('tr.header-line').length,
            function(item){
                item.find('input[type=text]').each(function(input){
                    this.value = '';
                });
                item.find('button.remove-header').on('click', function(){
                    var row = $(this).closest('tr');
                    controller.removeRow(row, $(this).parent().parent().parent().find('tr.header-line').length);
                });
            });
    });
    $('.remove-step').on('click', function() {
        var row = $(this).closest('div');
        controller.removeRow(row,  $(this).parent().parent().length);
    });
     $('.remove-header').on('click', function() {
        var row = $(this).closest('tr');
        controller.removeRow(row, $(this).parent().parent().parent().find('tr.header-line').length);
     });


    
    $('#btnYes').click(function() {
    	$('#removeItemModal').modal('hide');
    	$("input[type=hidden][name=_method]").val('delete');
    	$('form').submit();
    });
});