function checkBillingAddress()
{
	
   if($("#theSameAsShippingAddress").is(":checked"))
	   {
	      $(".billingAddressClass").prop("disabled", true);
	   }
   else
	   {
	       $(".billingAddressClass").prop("disabled", false);
	   }
}

function checkPasswordMatch()
{
   const password = $("#txtNewPassword").val();
   const confirmPasswprd = $("#txtConfirmPassword").val();
   
   if(password == "" && confirmPasswprd == "")
	   {
	      $("#checkPasswordMatch").html("");
	      $("#updateUserInfoButton").prop("diabled", false);
	   }
   else {
	   if(password != confirmPasswprd){
		   $("#checkPasswordMatch").html("Password do not match");
		   $("#updateUserInfoButton").prop("diabled", true);
	   }
	   else {
		   $("#checkPasswordMatch").html("Passwords match!");
		   $("#updateUserInfoButton").prop("diabled", false);
	   }
		   
   }
	   
}

$(document).ready( function() {
	$(".cartItemQty").on('change', function()
	{
		var id = this.id;
		$("#update-item-"+id).css('display','inline-block');
		
	});
	
	$('#theSameAsShippingAddress').on('click', checkBillingAddress);
	$("#txtNewPassword").keyup(checkPasswordMatch);
	$("#txtConfirmPassword").keyup(checkPasswordMatch);
});