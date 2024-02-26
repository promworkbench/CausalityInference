package org.processmining.dataTable;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.causalityinference.parameters.ActivityGrouperAttName;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;

/**
 * When the dependent situation feature is of event type, the events are grouped
 * based on their "Resource", "Timestamp", "Duration", or "activity name" (grouper attribute) value.
 * It returns true if the event belong to the group and false o.w..
 * 
 * An event belong to a group if:
 *  - Timestamp
 *  	params : acceptable days, min and max time in day.
 *  	Event occurs in the given days (depValues) between the given times (min and max) 
 *  	are considered in the group. 
 * 	- Resource
 * 		params : a set of resources
 * 		Events with the give resources are considered in the group.
 *  - Activity name
 * 		params : a set of activity names
 * 		Events with the give activity names are considered in the group.
 *  - Duration
 *  	params : min and max duration
 * 		Events with the duration between min and max are considered in the group.
 */	

/**
 * possible values for depActGrouperAttName = {ActivityName, Resource, Timestamp, Duration}
 */

public class EventGrouper {
	
	/**
	 *  Identify which attribute is used for grouping the events.
	 */
	ActivityGrouperAttName grouperAttName; 
	
	/**
	 *  The set of attribute values which are used for grouping the events.
	 *  It can includes;
	 *  	activity names
	 *      resources
	 *      week days
	 */
	private Set<String> values;
	
	private String minThreshold;
	private String maxThreshold;
	
	private SituationType situationType;
	
	public EventGrouper(Parameters params) {
		grouperAttName = params.getGrouperAttName();
		if (params.getGrouperAttValues() != null)
			values = params.getGrouperAttValues();
		if (params.getMinThreshold() != null)
			minThreshold = params.getMinThreshold();
		if (params.getMaxThreshold() != null)
			maxThreshold = params.getMaxThreshold();
		
		situationType = params.getSituationType();
	}
	
	public boolean isEventAnClassEvent(XEvent event) {
		if (event == null)
			return false;
		// check if the event contain the target choice attribute
		if (situationType.equals(SituationType.CS)) {
			XAttributeMap attMap = event.getAttributes();
			for (String attName : attMap.keySet()) 
				if (values.contains(attName))
					return true;
			
			return false;
		}
		
		if (grouperAttName.equals(ActivityGrouperAttName.AN)) {
			String attName = XConceptExtension.instance().extractName(event);
			if (values.contains(attName))
				return true;
		}
		
		if (grouperAttName.equals(ActivityGrouperAttName.R)) {
			String resource = XOrganizationalExtension.instance().extractResource(event);
			if (resource == null) {
				System.out.println(" --> An event withour resource <-- ");
				return false;
			}
			if (values.contains(resource))
				return true;
		}
		
		if (grouperAttName.equals(ActivityGrouperAttName.TS)) {
			if ((values != null && isWantedDay(event)) || values == null)
				return isTimeStampInTheInterval(event);
		}
		
		if (grouperAttName.equals(ActivityGrouperAttName.D)) {
			return isDurationInTheInterval(event);
		}
		return false;
	}

	private boolean isTimeStampInTheInterval(XEvent event) {
		
		// timestamp format example : Mon Jan 05 00:06:05 CET 2015
		String time = XTimeExtension.instance().extractTimestamp(event).toString();
		String[] part = time.split(" ");
		String[] hmValue = part[3].split(":");
		String[] hmThresholdMin = minThreshold.split(":");
		String[] hmThresholdMax = maxThreshold.split(":");
		if ((Integer.valueOf(hmValue[0]) >= Integer.valueOf(hmThresholdMin[0])) ||
				((Integer.valueOf(hmValue[0]) == Integer.valueOf(hmThresholdMin[0])) &&
						(Integer.valueOf(hmValue[1]) >= Integer.valueOf(hmThresholdMin[1])) &&
						(Integer.valueOf(hmValue[0]) <= Integer.valueOf(hmThresholdMax[0])) ||
						((Integer.valueOf(hmValue[0]) == Integer.valueOf(hmThresholdMax[0])) &&
								(Integer.valueOf(hmValue[1]) <= Integer.valueOf(hmThresholdMax[1]))) ))
						return true;
		return false;
	}

	/**
	 * @param event
	 * @return true if the duration of the event is in the given interval. O.w. false.
	 */
	private boolean isDurationInTheInterval(XEvent event) {
		XAttributeMap map = event.getAttributes();
		long value;
		if (map.containsKey("Activity duration"))
			value = ((XAttributeDiscrete)map.get("Activity duration")).getValue();
		else {
			System.out.println(" --> Event without activity duration <-- ");
			return false;
		}
		if (value >= (long) Double.parseDouble(minThreshold) && value <= (long) Double.parseDouble(maxThreshold))
			return true;
		
		return false;
	}


	private boolean isWantedDay(XEvent event) {
		Date date = XTimeExtension.instance().extractTimestamp(event);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		String weekday = getWeekday(dayOfWeek);
		
		if (values.contains(weekday))
			return true;
		else
			return false;
	}


	private String getWeekday(int dayOfWeek) {
		if (dayOfWeek == 1)
			return "Sun";
		if (dayOfWeek == 2)
			return "Mon";
		if (dayOfWeek == 3)
			return "Tue";
		if (dayOfWeek == 4)
			return "Wed";
		if (dayOfWeek == 5)
			return "the";
		if (dayOfWeek == 6)
			return "Fri";
		if (dayOfWeek == 7)
			return "Sat";

		return null;
	}
	
	public ActivityGrouperAttName getGrouperAttname() {
		return grouperAttName;
	}
	
	// --------------------------- Test Code -----------------------
	
	public static void main(String[] atgs) {
		// set event log
		AggregateAttributes a = new AggregateAttributes();
		XLog log = a.setupEventLog();
		a.setResources(log);
		log = a.getLog();
		
		// when grouper att is activityName
		Parameters param = new Parameters();
		param.setGrouperAttName(ActivityGrouperAttName.AN);
		Set<String> set = new HashSet<>();
		set.add("A");
		param.setGrouperAttValues(set);
		EventGrouper eg = new EventGrouper(param);
		System.out.println("out1");

		
		// when grouper att is resource
		param.setGrouperAttName(ActivityGrouperAttName.R);
		
		set = new HashSet<>();
		set.add("Jili");
		param.setGrouperAttValues(set);
		EventGrouper egR = new EventGrouper(param);
		
		// when grouper att is Duration
		param.setGrouperAttName(ActivityGrouperAttName.D);
		param.setGrouperAttValues(null);
		param.setMinThreshold("86300000");
		param.setMaxThreshold("86500000");
		EventGrouper egD = new EventGrouper(param);
		
		
		
	}
	
}