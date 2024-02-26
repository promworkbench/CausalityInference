package org.processmining.causalityinference.help;

public class GenerateEventLogDifferentNoiseFunctions {
	public final static String TEXT = "This plugin is devoted to generating event logs with different noise funtions."
			+ "The event logs are generated based on the IT company process introducd in the \"motivation example\" section of the following paper:<br/>"
			+ "Feature Recommendation for Structural\r\n" + 
			"Qafari, M.S. and van der Aalst, W., 2021. Feature Recommendation for Structural Equation Model Discovery in Process Mining."
			+ "<br/>"
			+ "<br/>"
			+ "How to use: <br/>"
			+ "1 - Use \" generate IT event log with selected noise intervals\". Use the wizard to set the upper bound and lower bound of each noise function.<br/>"
			+ "2 - Generate Petrinet model of the event log using \"mine petrinet with inductive miner\" plugin.<br/>"
			+ "3 - Generate replay results by applying \"replay a log on Petri net for vonformance checking\" on the event log and Petrinet model.<br/>"
			+ "4 - Use the \"root cause analysis using structural equation model\" with the following inputs and setting:<br/>"
			+ "inputes:<br/>"
			+ "--- event log<br/>"
			+ "--- Petri net model<br/>"
			+ "--- replay results<br/>"
			+ "setting:<br/>"
			+ "Use \"trace situation\" tab for setting target and descriptive features. <br/>"
			+ "--- In the \"select the dependent attribute name\" drop down select \"implementation phase duration person day\".<br/>"
			+ "--- In the \"select relevant trace or choise attribute\" drop down select \"complexity\".<br/>"
			+ "--- In the \"selsect relevent activities\" drop down select \"business case development\", \"product backlog\", and \" Team charter\".<br/>"
			+ "--- In the \"select relevant event attributes\" select \"duration person day\", \"priority\", and \"team size\". <br/>"
			+ "Create table! <br/>"
			+ "Use \"causal graph setting\" tab to generate the PAG and the SEM. <br/>"
			+ "--- Do the search.<br/>"
			+ "--- Add directions.<br/>"
			+ "--- Generate final graph.<br/>"
			+ "--- Do the estination.<br/>"
			+ "<br/>"
			+ "<br/>"
			+ "Well done!<br/>"
			+ "If you have any problem using these plugins, contact me (m.s.qafari@pads.rwth-aachen.de";

}
