package org.processmining.dataTable;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.causalityinference.parameters.Parameters;

public class NormalAttributes {
	
	/**
	 * Returns the value of the following attributes.
	 * 
	 * trace attributes
	 * 
	 * event attributes
	 * 		how many times a specific event has happened by now
	 * 		resource
	 * 		duration
	 * 		timestamp
	 * 		next activity
	 * 		previous activity
	 * 
	 * result of a choice place 	
	 */
	
	private Parameters params;
	
	private double timeUnit;
	
	public NormalAttributes(Parameters params) {
		this.params = params;
		timeUnit = params.getTiemUnitMillisec(params.getTimeUnit());
	}
	
	
	//TODO what if there is an attribute with the same name in both trace and event level?
	public Object getValue(XTrace trace, XEvent event, String attName) {
		// Predefined event attributes
		if (attName.equals("Resource"))
			return XOrganizationalExtension.instance().extractResource(event);
		if (attName.equals("Activity name"))
			return XConceptExtension.instance().extractName(event);
		if (attName.equals("Timestamp"))
			return XTimeExtension.instance().extractTimestamp(event).getTime();
		if (attName.equals("Number of execution")) {
			int count = 0;
			String actName1 = XConceptExtension.instance().extractName(event);
			for (XEvent e : trace) {				
				String actName2 = XConceptExtension.instance().extractName(e);
				if (actName1.equals(actName2))
					count++;
			}
			return count;
		}
//		XAttributeMap eMap = event.getAttributes();
//		for (String att : eMap.keySet())
//			System.out.println(att + "   " + eMap.get(att).toString());
		// Predefined trace attribute
		if (attName.equals("Sub Model Attribute")) {
			XAttributeMap traceMap = trace.getAttributes();
			if (traceMap.containsKey("Sub model duration")) {
				System.out.println("time " + getValue(traceMap.get("Sub model duration")).toString());
				return Math.round((((Double.valueOf(getValue(traceMap.get("Sub model duration")).toString())) /timeUnit) * 100.0) / 100.0);
			}
		}
			
		
		// Predefined event attribute
		if (attName.equals("Activity Duration")) {
			XAttributeMap eventMap = event.getAttributes();
			if (eventMap.containsKey("activityduration"))
				return Math.round((((Long.valueOf(getValue(eventMap.get("activityduration")).toString())) /timeUnit) * 100.0) / 100.0);
		}
		
		// Predefined event attribute
		if (attName.equals("Elapsed Time")) {
			Date startDate = XTimeExtension.instance().extractTimestamp(trace.get(0));
			Date endDate = XTimeExtension.instance().extractTimestamp(event);
			long startTime = startDate.getTime();
			long endTime = endDate.getTime();
			return Math.round((((endTime - startTime) /timeUnit) * 100.0) / 100.0);
		}	
		
		// Predefined event attribute
		if (attName.equals("Remaining Time")) {
			Date endDate = XTimeExtension.instance().extractTimestamp(trace.get(trace.size() - 1));
			Date startDate = XTimeExtension.instance().extractTimestamp(event);
			long startTime = startDate.getTime();
			long endTime = endDate.getTime();
			return Math.round((((endTime - startTime) /timeUnit) * 100.0) / 100.0);
		}	
		
		// Predefined event attribute
		if (attName.equals("Next Activity")) {
			int idx = 0;
			for (XEvent e : trace) {
				if (e.equals(event))
					break;
				idx++;
			}
			if (idx < trace.size() - 2)
				return XConceptExtension.instance().extractName(trace.get(idx + 1));
			else 
				return "Not set";
		}
		
		// Predefined event attribute
		if (attName.equals("Previous Activity")) {
			int idx = 0;
			for (XEvent e : trace) {
				if (e.equals(event))
					break;
				idx++;
			}
			if (idx > 0 && idx < trace.size() - 1)
				return XConceptExtension.instance().extractName(trace.get(idx - 1));
			else 
				return "Not set";
		}		
		
		// Predefined trace attributes
		if (attName.equals("number logMove")) {
			XAttributeMap traceMap = trace.getAttributes();
			if (traceMap.containsKey("number_logMove"))
				return getValue(traceMap.get("number_logMove"));
		}
		
		// Predefined trace attributes
		if (attName.equals("number modelMove")) {
			XAttributeMap traceMap = trace.getAttributes();
			if (traceMap.containsKey("number_modelMove"))
				return getValue(traceMap.get("number_modelMove"));
		}		
		
		// Predefined trace attributes
		if (attName.equals("Trace Duration")) {
			Date startDate = XTimeExtension.instance().extractTimestamp(trace.get(0));
			Date endDate = XTimeExtension.instance().extractTimestamp(trace.get(trace.size() - 1));
			long startTime = startDate.getTime();
			long endTime = endDate.getTime();
			return Math.round((((endTime - startTime) /timeUnit) * 100.0) / 100.0);
		}
		
		// Predefined trace attributes
		if (attName.equals("Trace Delay")) {
			Date startDate = XTimeExtension.instance().extractTimestamp(trace.get(0));
			Date endDate = XTimeExtension.instance().extractTimestamp(trace.get(trace.size() - 1));
			long startTime = startDate.getTime();
			long endTime = endDate.getTime();
			if ((endTime - startTime) <= params.getTraceDelayThreshold())
				return "onTime";
			else
				return "delayed";
		}
		
		// if attName is a trace attribute name
		XAttributeMap traceMap = trace.getAttributes();
		if (traceMap.containsKey(attName))
			return getValue(traceMap.get(attName));
		
		// if attName is an event attribute name
		XAttributeMap eventMap = event.getAttributes();
			if (eventMap.containsKey(attName))
				return getValue(eventMap.get(attName));
			
			return "*";
		
	}
	
	private Object getValue(XAttribute xAttrib) 
	{
		if (xAttrib instanceof XAttributeBoolean)
			return((XAttributeBoolean)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeContinuous)
			return((XAttributeContinuous)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeDiscrete)
			return((XAttributeDiscrete)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeTimestamp)
			return((XAttributeTimestamp)xAttrib).getValue();
		else if (xAttrib instanceof XAttributeLiteral)
			return((XAttributeLiteral)xAttrib).getValue();

		return null;
	}

}
