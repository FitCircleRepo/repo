function onSaveProf()
{
	
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	$('#submitbtn').attr('disabled', true);
	var name=$("#name").val();
	var dob=$("#dob").val();
	//tempdate =  $.datepicker.parseDate('dd/mm/yy', dob);
	//dob = $.format.date(tempdate, 'dd/M/yy');
	
	var phone=$("#phone").val();
	
	//var gender=$("#gender").val();
	var gender = $("input[name=Gender]:checked").val();
	var weight=$("#weight").val();
	var heightfeet=$("#Height_Feet").val();
	var heightinch=$("#Height_Inch").val();

	var height = heightfeet+"."+heightinch;
	//var sessionid = querystring("sessionid");
	var roomId = querystring("roomid");
	
	var jsonObj = new Object();
	jsonObj.roomId = roomId[0];
	jsonObj.name = name;
	jsonObj.dob=dob;
	jsonObj.phone = phone;
	jsonObj.gender = gender;
	jsonObj.weight = weight;
	jsonObj.height = height;
	jsonObj.heightfeet = heightfeet;
	jsonObj.heightinches = heightinch;
	//jsonObj.sessionid = sessionid[0];
	
	 $.ajax({url: "../handler", data: {"operation":"saveProf","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});
 
	 return false;
}

function getData()
{
	
	var sessionid = querystring("roomid");
	var sid = sessionid[0];
	 $.ajax({url: "../handler", data: {"operation":"getdata","sessionid":sid},success: function(result){		
			 console.log(JSON.parse(result));
			 result = JSON.parse(result);
			 $("#name").val(result[0]).prop('disabled', true);
			 //var d = $.datepicker.parseDate('dd/mm/yy', result[1]);
			 //$("#dob").val($.datepicker.formatDate('dd/mm/yy',d)).prop('disabled', true);
			 $("#phone").val(result[2]).prop('disabled', true);
			 $("input[name=Gender][value="+result[3]+"]").attr('checked','checked');
			 $('input[type="radio"]:not(:checked)').prop('disabled',true);
			 $("#weight").val(result[5]).prop('disabled', true);
			 //$("#Height_Feet").val(result[4].split(".")[0]).prop('disabled', true);
			 //$("#Height_Inch").val(result[4].split(".")[1]).prop('disabled', true);
			 
			 
			 //retdata [Abu, 03 Mar, 2015 00:00:00, 54321234567, Male, 5 5, 55]
			 
			 
			 
	 }});
}


/*
function getPlanList()
{
	var currRunDist = $("#Running_Event").val();
	var eventPlanDate = $("#planeventdate").val();
	
	var a  = new Date(eventPlanDate);
	var b = new Date();
	var weekDiff = Math.round((a-b)/ 604800000);
	
	jQuery(function( $ ){ // DOM is now ready
		//get the plan list on onchage event of date field
    $("#planeventdate").on("change", function(){ 
    	
    });

	});

	var planObj = new Object();
	planObj.currRunDist = currRunDist;
	planObj.eventPlanDate = eventPlanDate;
	planObj.weekDiff = weekDiff;
	
	$.ajax({url:"../handler",data:{"operation":"getPlanList","data":JSON.stringify(planObj)},success:function(result)
	{
	     console.	log(result);
	     //$("#eventDistance").append('<option value=1></option>');
	}});
}*/


jQuery(function( $ )
{ // DOM is now ready
		//get the plan list on onchage event of date field
   $("#datepicker").change(function(){ 
	   console.log('inside change');
	   		if($("#Running_Event").val() != "")
	   		{
	   				var currRunDist = $("#Running_Event").val();
    				var eventPlanDate = $("#datepicker").val();
    				var formType = "fi45-runnerz";
    				var a  = $.datepicker.parseDate('dd/mm/yy', eventPlanDate);
    				var b = new Date();
    				var weekDiff = Math.ceil((a-b)/ 604800000);
    				console.log(weekDiff);
    				var planObj = new Object();
    				planObj.currentRun = currRunDist;
    				planObj.eventPlanDate = eventPlanDate;
    				planObj.weeks = weekDiff;
    				planObj.form_identifier = formType;
    				planObj.emailID = $("#encemail").val();
    				
    				$.ajax({url:"../handler",data:{"operation":"getRunPlanOptions","data":JSON.stringify(planObj)},success:function(result)
    				{
    					result = JSON.parse(result);
    					//console.log(result);
    					if(result.length!=0)
    					{
    						//$('#Event_Distance')[0].options.length = 0;
    						$('#Event_Distance').empty();
    						$("#Event_Distance").append($('<option>').text('Select').attr('value', ''));
	    				    for ( var i = 0; i < result.length; i++ )
	    					{
	    				    	//console.log('data '+result[i][0]);
	    				    	
	    				    	$("#Event_Distance").append($('<option>').text(result[i][0]).attr('value', result[i][0]));
	    					}
    					}
    					else{
    						$('#Event_Distance')[0].options.length = 0;
    						//alert("Sorry ! The target is too agressive. Please change your target date");
    						sweetAlert("Repick your date!", "Your selected date is too aggressive.");
    					}
    					
    					
    				}});
	   		}
	   		else
	   		{
	   			//alert("Please enter current running distance");
	   			sweetAlert("Hey!", "Please enter your current running distance.");
	   		}
	});


	$("#Running_Event").change(function(){
		console.log($(this).val());
		if($(this).val() == "20-30 Min")
		{
			$("#timetaken input").prop('disabled',true);
		}
		else
		{
			$("#timetaken input").prop('disabled',false);
		}
		});
	
	$('#Running_Event_Option').change(function()
	{
		if($('#Running_Event_Option').val() == "no")
		{
			var currRunDist = $("#Running_Event").val();
			var goalplanObj = new Object();
			goalplanObj.currentRun = currRunDist;
			goalplanObj.weeks = "12";
			
			$.ajax({url:"../handler",data:{"operation":"getGoalPlanOptions","data":JSON.stringify(goalplanObj)},success:function(result)
				{
					result = JSON.parse(result);
					
					//console.log(result);
					if(result.length!=0)
					{
						//$('#Event_Distance')[0].options.length = 0;
						$('#goal').empty();
						$("#goal").append($('<option>').text('Select').attr('value', ''));
    				    for ( var i = 0; i < result.length; i++ )
    					{
    				    	//console.log('data '+result[i][0]);
    				    	
    				    	$("#goal").append($('<option>').text(result[i][0]).attr('value', result[i][0]));
    					}
					}
					else{
						$('#goal')[0].options.length = 0;
						//alert("Sorry ! The target is too agressive. Please change your target date");
						sweetAlert("Repick your date!", "Your selected date is too aggressive.");
					}
					
					
				}});
		}
	});
	
	
});


function editProf()
{
	$("#name").prop('disabled', false);
	$("#dob").prop('disabled', false);
	$("#phone").prop('disabled', false);
	$('input[type="radio"]').prop('disabled',false);
	$("#weight").prop('disabled', false);
	$("#Height_Feet").prop('disabled', false);
	$("#Height_Inch").prop('disabled', false);
	$("#submitbtn").show();
	$("#editprof").hide();
	console.log(this)
	return false;
}



function getQuerystringNameValue(name)
{
    // For example... passing a name parameter of "name1" will return a value of "100", etc.
    // page.htm?name1=100&name2=101&name3=102

    var winURL = window.location.href;
    var queryStringArray = winURL.split("?");
    var queryStringParamArray = queryStringArray[1].split("&");
    var nameValue = null;

    for ( var i=0; i<queryStringParamArray.length; i++ )
    {           
        queryStringNameValueArray = queryStringParamArray[i].indexOf('=')+1;

        if ( name == queryStringNameValueArray[0] )
        {
            nameValue = queryStringNameValueArray[1];
        }                       
    }

    return nameValue;
}

function querystring(key) {
   var re=new RegExp('(?:\\?|&)'+key+'=(.*?)(?=&|$)','gi');
   var r=[], m;
   while ((m=re.exec(document.location.search.replace('&amp;','&'))) != null) r.push(m[1]);
   return r;
}


function fitScore()
{
	var email="abu@webaroo.com";
	var uid = "506";
	var roomId = "123456";
	//var email = getQuerystringNameValue("email");
	//var uid = getQuerystringNameValue("uid");
	//var roomId = getQuerystringNameValue("roomId");
	var weight=$("#weight").val();
	var heightfeet=$("#Height_Feet").val();
	var heightinch=$("#Height_Inch").val();
	var height = heightfeet+" "+heightinch;
	var pushups = $("#pushups").val();
	var squats = $("#squats").val();
	var globejumps = $("#globejumps").val();
	var lowplank = $("#lowplank").val();
	var onemiletime = $("#onemiletime").val();
	
	 $.ajax({url: "../handler", data: {"operation" : "saveProf","roomId":roomId,"pushups":pushups,"squats":squats,"globejumps":globejumps,"lowplank":lowplank,"onemiletime":onemiletime,"weight":weight,"height":height,"email":email,"uid":uid},success: function(result){		
		
			 console.log("Success");
		 
	}});
}

function getPlan()
{
	var plan = $("#plansValue").val();
	 $.ajax({url: "../handler", data: {"operation" : "getPlan","roomId":roomId,"email":email,"uid":uid,"plan":plan},success: function(result){		
		
		 console.log("Success");

}});
}


function allocateFi45RunnerzPlan()
{
	if($('#Running_Event_Option').val() == "yes")
	{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	var runPlan=$("#Event_Distance").val();
	var currrunPlan = $("#Running_Event").val();
	var eventPlanDate = $("#datepicker").val();
	//var eventDate = $.datepicker.parseDate('dd/mm/yy', eventPlanDate);
	var jsonObj = new Object();
	jsonObj.currrunPlan=currrunPlan;
	jsonObj.runPlan = runPlan;
	jsonObj.eventDate = eventPlanDate;
	jsonObj.planType = 'fi45 Runnerz';
	//jsonObj.eventType = $('#Running_Event_Option').val();
	var sessionid = $.cookie("sessionid");
	jsonObj.sessionid = sessionid;
	console.log(jsonObj);
	 $.ajax({url: "../handler", data: {"operation":"allocatePlan","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});	
	}
	else
		{
		$("#loadingImage").show();
		$('form').attr('disabled', true);
		var runPlan=$("#goal").val();
		var currrunPlan = $("#Running_Event").val();
		var jsonObj = new Object();
		jsonObj.currrunPlan=currrunPlan;
		jsonObj.runPlan = runPlan;
		jsonObj.planType = 'fi45 Runnerz';
		//jsonObj.eventType = $('#Running_Event_Option').val();
		var sessionid = $.cookie("sessionid");
		jsonObj.sessionid = sessionid;
		
		console.log(jsonObj);
		 $.ajax({url: "../handler", data: {"operation":"allocatePlan","data":JSON.stringify(jsonObj)},success: function(result){
			 $('form').slideUp();
			 $('#successmsg').show();
			 $("#loadingImage").hide();
				 console.log(result);
		 },error:function(result){
			 $('form').slideUp();
			 $('#errormsg').show();
			 $("#loadingImage").hide();
		 }});
		}
	 return false;
}

function allocateFatBlasterRunnerzPlan()
{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	var runPlan=$("#Plan_Name").val();
	
	var jsonObj = new Object();
	jsonObj.runPlan = runPlan;
	jsonObj.planType = 'Fat Blaster';
	var sessionid = $.cookie('sessionid');
	jsonObj.sessionid = sessionid;
	console.log(jsonObj);
	 $.ajax({url: "../handler", data: {"operation":"allocatePlan","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});	
	 
	 return false;
}

function redirect() {
	page = $('#selectPage').val()+$('#encemail').val();
	location.href=page;
}

function redirectToSuccess() {
	//page = $('#selectPage').val()+$('#encemail').val();
	//location.href=page;
	location.href="FitTestSuccess.html";
}

function createProfile()
{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	$('#submitbtn').attr('disabled', true);
	var name=$("#name").val();
	var dob=$("#dob").val();
	//tempdate =  $.datepicker.parseDate('dd/mm/yy', dob);
	//dob = $.format.date(tempdate, 'dd/M/yy');
	
	var phone=$("#phone").val();
	
	//var gender=$("#gender").val();
	var gender = $("input[name=Gender]:checked").val();
	var weight=$("#weight").val();
	var heightfeet=$("#Height_Feet").val();
	var heightinch=$("#Height_Inch").val();	
	var height = heightfeet+"."+heightinch;
	//var sessionid = querystring("sessionid");
	var roomId = querystring("roomid");
	
	var jsonObj = new Object();
	jsonObj.roomId = roomId[0];
	jsonObj.name = name;
	jsonObj.dob=dob;
	jsonObj.phone = phone;
	jsonObj.gender = gender;
	jsonObj.weight = weight;
	jsonObj.height = height;
	jsonObj.heightfeet = heightfeet;
	jsonObj.heightinches = heightinch;
	//jsonObj.sessionid = sessionid[0];
	
	 $.ajax({url: "../handler", data: {"operation":"createProf","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});
	
	
	
	 
	 return false;
}



function nutritionData()
{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	$('#submitbtn').attr('disabled', true);
	var goal=$("#Goal").val();
	var preferred_cuisine=$("#Preferred_Cuisine").val();
	
	var preference=$("#Preference").val();
	var current_physical_activity=$("#Current_Physical_Activity").val();
	var exercise_frequency = $("#Exercise_Frequency").val();
	var supplements=$("#Supplements").val();
	var nutritional_supplements=$("#Nutritional_Supplements").val();
	
	var current_diet=$("#Current_diet").val();	
	var medical_history=$("#Medical_History").val();
    var food_allergy=$("#Food_Allergy").val();		
	
	
	var roomId = querystring("roomid");
	
	var jsonObj = new Object();
	jsonObj.roomId = roomId[0];
	jsonObj.goal = goal;
	jsonObj.preferred_cuisine=preferred_cuisine;
	jsonObj.preference=preference;
	jsonObj.current_physical_activity = current_physical_activity;
	jsonObj.exercise_frequency = exercise_frequency;
	jsonObj.supplements = supplements;
	jsonObj.nutritional_supplements = nutritional_supplements;
	jsonObj.current_diet = current_diet;
	jsonObj.medical_history = medical_history;
	jsonObj.food_allergy = food_allergy;
	
	
	 $.ajax({url: "../handler", data: {"operation":"nutritionData","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});
	
		 
	 return false;
}

function progressTrack()
{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
	$('#submitbtn').attr('disabled', true);
		
	var arms = $("#arms").val();
	var hip = $("#hip").val();
	var waist = $("#waist").val();
	var thighs = $("#thighs").val();
	var weight = $("#weight").val();
	var gender = $("#gender").val();	
	var roomId = querystring("roomid");
	
	var jsonObj = new Object();
	jsonObj.roomId = roomId[0];
	jsonObj.arms = arms;
	jsonObj.hip = hip;
	jsonObj.waist = waist;
	jsonObj.thighs = thighs;
	jsonObj.weight = weight;
	jsonObj.gender = gender;
		
	 $.ajax({url: "../handler", data: {"operation":"progressTrack","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});
	
		 
	 return false;
}

function getFitData()
{
	$("#loadingImage").show();
	$('form').attr('disabled', true);
   	var roomId = querystring("roomid");

	var age = $('#age').val();
	var gender = $('#gender').val();
	var heightfeet = $('#heightfeet').val();
	var heightinches = $('#heightinches').val();
	var weight = $('#weight').val();
	var pushups = $('#pushups').val();
	var squats = $('#squats').val();
	var lowplankminutes = $('#lowplankminutes').val();
	var lowplankseconds = $("#lowplankseconds").val();
	var globejump = $('#globejump').val();
	var onemileminutes = $('#onemileminutes').val();
	var onemileseconds = $('#onemileseconds').val();
	
	
	var jsonObj = new Object();
	jsonObj.roomId = roomId[0];
	jsonObj.age = age;
	jsonObj.gender = gender;
	jsonObj.heightfeet = heightfeet;
	jsonObj.heightinches = heightinches;
	jsonObj.weight = weight;
	jsonObj.pushups = pushups;
	jsonObj.squats = squats;
	jsonObj.lowplankminutes = lowplankminutes;
	jsonObj.lowplankseconds = lowplankseconds;
	jsonObj.globejump = globejump;
	jsonObj.onemileminutes = onemileminutes;
	jsonObj.onemileseconds = onemileseconds;
	console.log(jsonObj);
	 $.ajax({url: "../handler", data: {"operation":"fittest","data":JSON.stringify(jsonObj)},success: function(result){
		 $('form').slideUp();
		 $('#successmsg').show();
		 $("#loadingImage").hide();
			 //console.log(result);
	 },error:function(result){
		 $('form').slideUp();
		 $('#errormsg').show();
		 $("#loadingImage").hide();
	 }});	
	 
	 return false;
}