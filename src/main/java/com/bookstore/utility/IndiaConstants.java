package com.bookstore.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndiaConstants {
	
	public static final String INDIA = "INDIA";
	
	public static final Map<String, String> mapOfIndianStates = new HashMap<String, String>(){
			{
		       put("WB" , "West Bengal");
		       put("BLR" , "Bengaluru");
		       put("Andhra Pradesh" , "AP");
		       put("Assam" , "AS");
		       put("Bihar" , "BR");
		       put("Chhattisgarh" , "CG");
		       put("Goa" , "GA");
		       put("Gujarat" , "GJ");
		       put("Madhya Pradesh" , "MP");
		       put("Maharashtra" , "MH");
		       put("Uttar Pradesh" , "UP");
		       put("Tamil Nadu" , "TN");
		       put("Uttarakhand" , "UK");
		       put("Delhi" , "DL");
		       put("Karnataka" , "KA");
		       put("Kerala" , "KL");
		       put("Orissa" , "OR");
			}
			
	};
	
	public final static List<String> listOfIndiaStateCode = new ArrayList<>(mapOfIndianStates.keySet());
	public final static List<String> listOfIndiaStateName = new ArrayList<>(mapOfIndianStates.values());
}
