Iugu.setAccountID($('#iuguAccountId').val());
Iugu.setTestMode($('#iuguTestMode').val() == 'true');

$('#formData').submit(function(evt) {
    var form = $(this);
    var cardTokenResponse = function(data) {
        if (data.errors) {
            console.log("Erro ao salvar cartão: " + JSON.stringify(data.errors));
            if (data.errors.hasOwnProperty('number') || data.errors.hasOwnProperty('record_invalid')) {
                $("#errorMessage").html("Numero do cartão inválido");

            } else if (data.errors.hasOwnProperty('last_name')) {
                $("#errorMessage").html("Nome inválido");

            } else if (data.errors.hasOwnProperty('expiration')) {
                $("#errorMessage").html("Data de expiração inválida");

            } else if (data.errors.hasOwnProperty('verification_value')) {
                $("#errorMessage").html("CVV inválido");

            }  else {
                $("#errorMessage").html("Erro ao finalizar o pagamento! Tente novamente mais tarde.");
            }
            $("#modalError").show();
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

$("#btn-close").click(function() {
    $("#modalError").hide();
});