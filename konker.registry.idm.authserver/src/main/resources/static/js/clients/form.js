$('.confirm-delete').on('click', function(e) {
    e.preventDefault();
    $('#removeItemModal').modal('show');
});


$('#btnYes').click(function() {
  	$('#removeItemModal').modal('hide');
    $("input[type=hidden][name=_method]").val('delete');
    $('form').submit();
});