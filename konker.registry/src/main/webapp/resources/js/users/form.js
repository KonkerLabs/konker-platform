$('.confirm-delete').on('click', function(e) {
    e.preventDefault();
    $('#removeItemModal').modal('show');
});


$('#btnYes').click(function() {
  	$('#removeItemModal').modal('hide');
    $("input[type=hidden][name=_method]").val('delete');
    $('form').submit();
});

$('#btn-avatar-edit, #btn-avatar-save').click(function(e){
    e.preventDefault();
    $('#user-avatar').toggleClass('hide');
    $('#user-avatar').toggleClass('show');
    $('#user-avatar-edit').toggleClass('hide');
    $('#user-avatar-edit').toggleClass('show');
});

$('#btn-account-edit, #btn-account-save').click(function(e){
    e.preventDefault();
    $('#user-account').toggleClass('hide');
    $('#user-account').toggleClass('show');
    $('#user-account-edit').toggleClass('hide');
    $('#user-account-edit').toggleClass('show');
});

$('#btn-password-edit, #btn-password-save').click(function(e){
    e.preventDefault();
    $('#user-password').toggleClass('hide');
    $('#user-password').toggleClass('show');
    $('#user-password-edit').toggleClass('hide');
    $('#user-password-edit').toggleClass('show');
});

/*$('#btn-save-all').click(function(e){
    e.preventDefault();
    if(UserValidation.validate()){
    }
    $('form').post();
});*/

var UserValidation = {}
UserValidation.validate = function(){
    if($('#old-password').val() == ''){
        alert('old password error');
        return false;
    }
    if($('#new-password').val() == ''){
        alert('new password error');
        return false;
    }
    if($('#password').val() == ''){
        alert('password error');
        return false;
    }
    if($('#now-password').val() != $('#password').val()){
        alert('password difference error');
        return false;
    }

    return true;
}
