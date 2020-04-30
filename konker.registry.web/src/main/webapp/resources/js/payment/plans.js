$('.btn-upgrade-now').click(function(e) {
    var selectedPlan = $(this).closest("div").find("input").val();
    $('#plan').val(selectedPlan);

    if (selectedPlan != "Education") {
        $('#kit').val(false);
        $('#formData').submit();

    } else {
        e.preventDefault();
        $('#kitModal').modal('show');
    }
});

$('#buyKit').click(function() {
    $('#kit').val(true);
    $('#formData').submit();
});

$('#noKit').click(function() {
    $('#kit').val(false);
    $('#formData').submit();
});