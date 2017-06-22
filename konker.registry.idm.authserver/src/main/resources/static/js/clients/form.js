var urlToRemove = '';
$('.confirm-delete').on('click', function(e) {
    e.preventDefault();
     urlToRemove = this.getAttribute("href");
    $('#removeItemModal').modal('show');
});


$('#btnYes').click(function() {
  	$('#removeItemModal').modal('hide');
    window.location.href = urlToRemove
});

$('#btnNo').click(function() {
  	$('#removeItemModal').modal('hide');
});