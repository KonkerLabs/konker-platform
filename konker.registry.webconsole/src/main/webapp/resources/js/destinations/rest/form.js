var controller = {
    addNewRow : function(trLast, rowCount, callback) {
        trNew = trLast.clone();
        trLast.after(trNew);
        this.applyNewIndex(trNew, rowCount);
        callback(trNew);
    },
    applyNewIndex : function(row, index) {
        row.find('select, input').each(function() {
            var input = $(this);
            var newId = input.attr('id').replace(/\d+/gi,index);
            var newName = input.attr('name').replace(/\d+/gi,index);
            if (input.attr('name').indexOf('headers') != -1) {
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
            this.reindexRows($('#headersBody').find('tr'));
        } else {
            $('#headersBody').find('tr input').val('');
        }
    },
    reindexRows : function(tableRows) {
        tableRows.each( function(index) {
            console.log(index);
            controller.applyNewIndex($(this), index);
        });
    },
}

$(document).ready(function() {

    $('.confirm-delete').on('click', function(e) {
        e.preventDefault();
        $('#removeItemModal').modal('show');
    });

    $('#btnYes').click(function() {
        $('#removeItemModal').modal('hide');
        $("input[type=hidden][name=_method]").val('delete');
        $('form').submit();
    });

    controller.tableBody = $('tbody');

    $('.add-header').on('click',function() {
        controller.addNewRow(
            $('#headersBody').find("tr:last"),
            $('#headersBody').find('tr').length,
            function(item){
                item.find('input[type=text]').each(function(input){
                    this.value = '';
                });
                item.find('button').on('click', removeHeaderRow);
            });
    });

    function removeHeaderRow() {
        var row = $(this).closest('tr');
        controller.removeRow(row, $('#headersBody').find('tr').length);
    }

    $('#headersBody').find('button').on('click', removeHeaderRow);

    var restDestinationType = $('input[type=radio][name=type]');

    restDestinationType.change(function() {
    	renderBodyFragment(this.value);
    });

    applyEventBindings(restDestinationType.filter('input:checked').val());

});

function renderBodyFragment(scheme) {
    var base = urlTo('/destinations/rest/' + $('#applicationName').val() + '/body/');
    var url = base + scheme;

    fetchViewFragment(scheme, url, $('#bodyFragment'));
}

function fetchViewFragment(scheme, fetchUrl, fragment) {
    var loadSpinner;

    $.ajax({
        context : this,
        type : "GET",
        url : fetchUrl,
        dataType: "html",
        timeout : 100000,
        beforeSend : function() {
            loadSpinner = setTimeout(function() {
                $("div.ajax-loading").addClass('show');
            }, 50);
        },
        success : function(data) {
        	fragment.html(data);
            applyEventBindings(scheme);
        },
        complete : function() {
            clearTimeout(loadSpinner);
            $("div.ajax-loading").removeClass('show');
        }
    });
}

function applyEventBindings(scheme) {
    switch (scheme) {
        default : break;
    }
}