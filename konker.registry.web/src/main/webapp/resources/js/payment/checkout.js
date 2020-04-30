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
        }
    }

    Iugu.createPaymentToken(this, cardTokenResponse);
    return false;
});

$(".btn-cancel").click(function() {
    $('#formCancel').submit();

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