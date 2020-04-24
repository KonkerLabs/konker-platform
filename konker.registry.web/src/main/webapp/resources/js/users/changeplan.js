Iugu.setAccountID($('#iuguAccountId').val());
Iugu.setTestMode($('#iuguTestMode').val());

$('#formData').submit(function(evt) {
    var form = $(this);
    var cardTokenResponse = function(data) {
        if (data.errors) {
            console.log("Erro ao salvar cartão: " + JSON.stringify(data.errors));
        } else {
            $("#cardToken").val( data.id );
            form.get(0).submit();
            // var url = urlTo('/me/changePlan');
            // $.ajax({
            //     context : this,
            //     type : "POST",
            //     url : url,
            //     dataType: "html",
            //     timeout : 100000,
            //     data: $(form).serialize(),
            //     success : function(data) {
            //         console.log(data);
            //     }
            // });
        }
    }

    Iugu.createPaymentToken(this, cardTokenResponse);
    return false;
});

$('.btn-upgrade-now').click(function() {
    var selectedPlan = $(this).closest("div").find("input").val();
    $('#plan').val(selectedPlan);
    $('.planSelected').html(selectedPlan);
});

$(".btn-upgrade-now").click(function() {
	$(".panel-plans").hide();
    $(".panel-payment-way").show();

});

$("#card-number").blur(function() {
    $(this).mask("0000-0000-0000-0000")
});

$("#card-expiration").blur(function() {
    $(this).mask("00/00")
});

$("#billing-cep").blur(function() {
    $(this).mask("00000-000")
});