package org.processmining.dataTable;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.util.XsDateTimeConversion;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.xeslite.external.XFactoryExternalStore.MapDBDiskImpl;

/**
 * Given a time window t, the values of the following 
 * aggregated attributes are computed:
 * 
 * "Average service time Trace"
 * "Average waiting time Trace"
 * "Average service time Event : " + actName
 * "Average waiting time Event : " + actName
 * "Average service time Resource : " + resource
 * "Average waiting time Resource : " + resource
 * "Process workLoad" --> number of customers in the whole process
 * "Process number waiting" --> number of customers waiting in the whole process
 * "Number of active Event : " + actName --> number of active instances of a given event
 * "Number of waiting Event : " + actName --> number of customers waiting for an activity
 * "WorkLoad Resource : " + resource --> number of customers handled by a resource
 * "Number waiting for Resource : " + resource
 * 
 * We assume that we have start time of each activity.
 * The list of time related attributes for an event are as follows:
 *   - waiting time
 *   - start time
 *   - duration
 * 
 * ASSUMPTION : Event log contains at least one trace and the first trace 
 * contains at least one event.
 * @author qafari
 *
 */
public class AggregateAttributes {
	
	private XLog log;
	/**
	 * time window in milliseconds.
	 */
	private long window;
	
	/**
	 * a set of Maps of window to aggregated values. Each map is related to one of the
	 * mentioned attributes.
	 * Counting attributes
	 * The window is identified by its start time.
	 */
	private Map<String, Map<Long, Long>> aggregatedValues;
	
	/**
	 * a set of Maps of window to aggregated values. Each map is related to one of the
	 * mentioned attributes.
	 * Average duration attributes.
	 * The window is identified by its start time.
	 */
	private Map<String, Map<Long, Long>> aggregatedAvgValues;
	
	/**
	 * The set of all aggregated counting attribute names.
	 */
	private Set<String> attNames;
	
	/**
	 * The set of all aggregated averaging attribute names.
	 */
	private Set<String> avgAttNames;
	
	/**
	 * Minimum timestamp in the event log
	 */
	private Date minTimestamp;
	
	/**
	 * Maximum timestamp in the event log
	 */
	private Date maxTimestamp;
	
	/**
	 *  Timespan if past attributes are needed.
	 */
//	private long timespan;
	
	/**
	 * 
	 */
	private double timeUnit;

	
	public AggregateAttributes(Parameters params, double tiemUnitMillisec, long windowSize, long offset) {
		this.log = params.getLog();
		this.timeUnit = tiemUnitMillisec;
		window = windowSize;
		
		initiate();
		setValues();
	}
	
	/**
	 * Initiating aggregatedValues map;
	 */
	private void initiate() {		
		// gathering all the activity names and resources in the event log 
		//Set min and max timestamps
		Set<String> actNames = new HashSet<>();
		Set<String> resources = new HashSet<>();
		maxTimestamp = XTimeExtension.instance().extractTimestamp(log.get(0).get(0));
		minTimestamp = XTimeExtension.instance().extractTimestamp(log.get(0).get(0));
		for (XTrace trace : log) 
			for (XEvent event : trace) {
				// activity Names
				actNames.add(XConceptExtension.instance().extractName(event));
				// resources
				String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
				if (resource != null)
					resources.add(resource);
				// min and max timestamp
				Date startTime = getStartTimestamp(event);
				if (startTime.before(minTimestamp))
					minTimestamp = startTime;
				Date endTime = getEndTimestamp(event);
				if (endTime.after(maxTimestamp))
					maxTimestamp = endTime;
			}
//		System.out.println(minTimestamp);
//		System.out.println(maxTimestamp);
		initiateAggregatedValues(actNames, resources);
		initiateAggregatedAvgValues(actNames, resources);	
	}
	
	private void initiateAggregatedAvgValues(Set<String> actNames, Set<String> resources) {
		aggregatedAvgValues = new HashMap<>();
		// Set avgAttNames (all the counting aggregated attribute names)
		avgAttNames = new HashSet<>();
		
		avgAttNames.add("Average service time Trace");
		avgAttNames.add("Average waiting time Trace");
		for (String name : actNames) {
			avgAttNames.add("Average service time Event : " + name);
			avgAttNames.add("Average waiting time Event : " + name);
		}
		for (String resource : resources) {
			avgAttNames.add("Average service time Resource : " + resource);
			avgAttNames.add("Average waiting time Resource : " + resource);
		}
		
		//Adding maps to the aggregatedValues
		for (String attName : avgAttNames) {
			Map<Long, Long> map = new HashMap<>();
			Long end = (this.maxTimestamp.getTime() - this.minTimestamp.getTime()) / window;
			for (Long i = 0L; i <= end; i++ )
				map.put(i, 0L);
			aggregatedAvgValues.put(attName, map);			
		}	
	}

	private void initiateAggregatedValues(Set<String> actNames, Set<String> resources) {
		aggregatedValues = new HashMap<>();
		// Set attNames (all the counting aggregated attribute names)
		attNames = new HashSet<>();
		attNames.add("Process workLoad");
		attNames.add("Process number waiting");
		for (String name : actNames) {
			attNames.add("Number of active Event : " + name);
			attNames.add("Number of waiting Event : " + name);
		}
		if (!resources.isEmpty())
			for (String resource : resources) {
				attNames.add(resource);
				attNames.add("WorkLoad Resource : " + resource);
				attNames.add("Number waiting for Resource : " + resource);
			}
		
		//Adding maps to the aggregatedValues
		for (String attName : attNames) {
			Map<Long, Long> map = new HashMap<>();
			Long end = (this.maxTimestamp.getTime() - this.minTimestamp.getTime()) / window;
			for (Long i = 0L; i <= end; i++ )
				map.put(i, 0L);
			aggregatedValues.put(attName, map);
		}		
	}
	
	/**
	 *   //TODO add new attNames including deviation, ...
	 * @param attName
	 * @return true if the attName is a trace aggregated attribute name.
	 
	public boolean isTraceAttName(String attName) {
		if (attName.equals("Process workLoad"))
			return true;
		if (attName.equals("Process number waiting"))
			return true;
		if (attName.equals("Average service time trace"))
			return true;
		if (attName.equals("Average waiting time trace"))
			return true;
		
		return false;
	} */

	/**
	 * @param event
	 * @return End timestamp of event (duration + start timestamp)
	 */
	private Date getEndTimestamp(XEvent event) {
		Date timestamp = XTimeExtension.instance().extractTimestamp(event);
		XAttributeMap eventMap = event.getAttributes();
		
		if (!eventMap.containsKey("activityduration")) 
			return timestamp;
		
		long duration = Long.parseLong(eventMap.get("activityduration").toString());
		long milliseconds = timestamp.getTime();
		
		return new Date(duration + milliseconds);
	}
	
	/**
	 * @param event
	 * @return start of waiting timestamp of event (start timestamp - waiting time)
	 */
	private Date getWaitingTimestamp(XEvent event) {
		Date timestamp = XTimeExtension.instance().extractTimestamp(event);
		XAttributeMap eventMap = event.getAttributes();
		
		if (!eventMap.containsKey("waitingDuration")) 
			return null;
		
		long waitingDuration = Long.parseLong(eventMap.get("waitingDuration").toString());
		if (waitingDuration == 0) 
			return null;
		
		long milliseconds = timestamp.getTime();
		
		return new Date( milliseconds - waitingDuration);
	}
	
	/**
	 * @param event
	 * @return start timestamp
	 */
	private Date getStartTimestamp(XEvent event) {
		return XTimeExtension.instance().extractTimestamp(event);
	}
	
	/**
	 * Setting the aggregated values for all the attributes per window.
	 * each window is reachable by its start time.
	 */
	public void setValues() {
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				Long[] wins = findWondows(event);
				updateResourceWorkLoad(event, wins);
				updateNumEvent(event, wins);
				updateNumWaitingEvent(event, wins);
				updateNumWaitingResource(event, wins);
				updateNumwaiting(wins);
				updateAvgServiceTimeEvent(event, wins);
				updateAvgWaitingTimeEvent(event, wins);
				updateAvgServiceTimeResource(event, wins);
				updateAvgWaitingTimeResource(event, wins);
			}
			updateProcessWorkLoad(trace);
			updateAvgServiceTime(trace);
			updateAvgWaitingTime(trace);
		}
		
		computeAverage();
	}
	
	private void updateNumWaitingResource(XEvent event, Long[] wins) {
		String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
		if (wins[0] != -1L && resource != null) {
			for (long w = wins[0]; w <= wins[3]; w++) {
				Long n = aggregatedValues.get("Number waiting for Resource : " + resource).get(w) + 1;
				aggregatedValues.get("Number waiting for Resource : " + resource).remove(w);
				aggregatedValues.get("Number waiting for Resource : " + resource).put(w, n);
			}
		}
	}

	/**
	 * Add the waiting time of the event to the resource that is doing it.
	 * @param event
	 * @param wins
	 */
	private void updateAvgWaitingTimeResource(XEvent event, Long[] wins) {
		String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
		
		if (resource == null || wins[0] == -1) 
			return;
			
		for (long w = wins[0]; w <= wins[3]; w++) {
//			System.out.println("waiting: " + durationEvent(event));
//			System.out.println(aggregatedAvgValues.get("avgWaitingTime_" + resource).get(w));
			long n = aggregatedAvgValues.get("Average waiting time Resource : " + resource).get(w) + waitingTime(event);
			aggregatedAvgValues.get("Average waiting time Resource : " + resource).remove(w);
			aggregatedAvgValues.get("Average waiting time Resource : " + resource).put(w, n);
		}
	}

	/**
	 * Add the duration of the event to the resource that is doing it.
	 * @param event
	 * @param wins
	 */
	private void updateAvgServiceTimeResource(XEvent event, Long[] wins) {
		String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
		
		if (resource == null)
			return;
		
		for (long w = wins[1]; w <= wins[2]; w++) {
//			System.out.println(durationEvent(event));
//			System.out.println(aggregatedAvgValues.get("avgServiceTime_" + resource).get(w));
			long n = 0;
			if (durationEvent(event) != null)
				n = aggregatedAvgValues.get("Average service time Resource : " + resource).get(w) + durationEvent(event);
			else
				n = aggregatedAvgValues.get("Average service time Resource : " + resource).get(w) + 0;
			aggregatedAvgValues.get("Average service time Resource : " + resource).remove(w);
			aggregatedAvgValues.get("Average service time Resource : " + resource).put(w, n);
		}	
	}

	/**
	 * by now the values in the aggregatedAvgValues are sum of the requested attribute values.
	 * Here the sum is divided by the number of proper attribute.
	 */
	private void computeAverage() {
//		for (String attName : aggregatedAvgValues.keySet())
//			System.out.println(attName);
		for (String attName : aggregatedAvgValues.keySet()) {
			// sum of trace waiting time divided by the number of people in the process
			if (attName.equals("Average waiting time Trace")) {
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
						long wtt = valueDivideBytimeUnit(attName,w) / aggregatedValues.get("Process workLoad").get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, wtt);
					}
				}
			// sum of trace service time divided by the number of people in the process
			} else if (attName.equals("Average service time Trace")) {
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
//					System.out.println(w);
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
//						System.out.println(w);
//						System.out.println(attName);
//						System.out.println(aggregatedAvgValues.get(attName).get(w));
						long stt = valueDivideBytimeUnit(attName,w) / aggregatedValues.get("Process workLoad").get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, stt);
					}
				}	
			// sum of the duration of an event in a window by the number of active events
			} else if (attName.length() >= "Average service time Event : ".length() && attName.substring(0, "Average service time Event : ".length()).equals("Average service time Event : ")) {
				String name = "Number of active Event : " + attName.substring("Average service time Event : ".length(), attName.length());
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
						long ste = valueDivideBytimeUnit(attName,w) / aggregatedValues.get(name).get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, ste);
					}
				}
			// sum of the waiting time of an event is divided by the number of active ones in that window
			} else if (attName.length() >= "Average waiting time Event : ".length() && attName.substring(0, "Average waiting time Event : ".length()).equals("Average service time Event : ")) {
				String name = "Number of waiting event : " + attName.substring("Average waiting time Event : ".length(), attName.length());
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
						long ste = valueDivideBytimeUnit(attName,w) / aggregatedValues.get(name).get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, ste);
					}
				}
			// sum of the waiting time of a resource in a window is divided by its workload
			} else if (attName.length() >= "Average waiting time Resource : ".length() && attName.substring(0, "Average waiting time Resource : ".length()).equals("Average waiting time Resource : ")) {
				String name = "Number waiting for Resource : " + attName.substring("Average waiting time Resource : ".length(), attName.length());
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
//						System.out.println(attName + " " + w + " " + name + " " + aggregatedAvgValues.get(attName).get(w) + " " + aggregatedValues.get("workLoad_" + name).get(w));
						long ste = valueDivideBytimeUnit(attName,w) / aggregatedValues.get(name).get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, ste);
					}
				}
			// sum of the Service time of a resource in a window is divided by its workload
			} else if (attName.length() >= "Average service time Resource : ".length() && attName.substring(0, "Average service time Resource : ".length()).equals("Average service time Resource : ")) {
				String name = "WorkLoad Resource : " + attName.substring("Average service time Resource : ".length(), attName.length());
				Set<Long> keys = copySet(aggregatedAvgValues.get(attName).keySet());
				for (Long w : keys) {
					if (aggregatedAvgValues.get(attName).get(w) > 0) {
//						System.out.println(attName + " " + w + " " + name + " " + aggregatedAvgValues.get(attName).get(w) + " " + aggregatedValues.get("workLoad_" + name).get(w));
						long ste = valueDivideBytimeUnit(attName,w) / aggregatedValues.get(name).get(w);
						aggregatedAvgValues.get(attName).remove(w);
						aggregatedAvgValues.get(attName).put(w, ste);
					}
				}
			}
		}
	}
	
	/**
	 * return the value of attribute attName in window w from aggregatedAvgValues after dividing by timeUnit (result are rounded to two decimal places)
	 * @param attName
	 * @param w
	 * @return
	 */
	public long valueDivideBytimeUnit(String attName, long w) {
		return Math.round(((aggregatedAvgValues.get(attName).get(w) /timeUnit) * 100.0) / 100.0);
	}

	private Set<Long> copySet(Set<Long> keySet) {
		Set<Long> set = new HashSet<Long>();
		for (long l : keySet)
			set.add(l);
		
		return set;
	}

	private void updateAvgWaitingTime(XTrace trace) {
		long startIndex = getIndex(getStartTimestamp(trace.get(0)));
		long endIndex = endIndex(getEndTimestamp(trace.get(trace.size() - 1)));
		for (long w =startIndex; w <= endIndex; w++) {
			long d = aggregatedAvgValues.get("Average waiting time Trace").get(w) + waitingTimeTrace(trace);
			aggregatedAvgValues.get("Average waiting time Trace").remove(w);
			aggregatedAvgValues.get("Average waiting time Trace").put(w, d);
		}
	}

	private Long waitingTimeTrace(XTrace trace) {
		long waitingTime = 0;
		for (XEvent event : trace) 
			waitingTime = waitingTime + waitingTime(event);
		
		return waitingTime;
	}

	private void updateAvgServiceTime(XTrace trace) {
		long startW = getIndex(getStartTimestamp(trace.get(0)));
		long endW = endIndex(getEndTimestamp(trace.get(trace.size() - 1)));
		for (long w =startW; w <= endW; w++) {
			long d = aggregatedAvgValues.get("Average service time Trace").get(w) + durationTrace(trace);
			aggregatedAvgValues.get("Average service time Trace").remove(w);
			aggregatedAvgValues.get("Average service time Trace").put(w, d);
		}
	}
	
	/**
	 * @param trace
	 * @return the duration of the trace in milliseconds
	 */
	private Long durationTrace(XTrace trace) {
		long startTime = getStartTimestamp(trace.get(0)).getTime();
		long endTime = getEndTimestamp(trace.get(trace.size() - 1)).getTime();
		return endTime - startTime;
	}

	private void updateProcessWorkLoad(XTrace trace) {
		long startW = getIndex(getStartTimestamp(trace.get(0)));
		long endW = endIndex(getEndTimestamp(trace.get(trace.size() - 1)));
		for (long w =startW; w <= endW; w++) {
			Long d = aggregatedValues.get("Process workLoad").get(w) + 1;
//			aggregatedValues.get("Process workLoad").remove(w);
			aggregatedValues.get("Process workLoad").put(w, d);
		}
	}

	private long getIndex(Date timestamp) {
			return getIndex(timestamp.getTime());
	}
	
	long getIndex(Long time) {
		if ((time - minTimestamp.getTime()) / window  < 0 )
			return 0;
		
		return ((time - minTimestamp.getTime()) / window );
	}
	
	private long endIndex(Date endTimestamp) {
		if ((endTimestamp.getTime() - minTimestamp.getTime()) % window == 0)
			return ((endTimestamp.getTime() - minTimestamp.getTime()) / window ) - 1;
		else
			return ((endTimestamp.getTime() - minTimestamp.getTime()) / window );
}


	private void updateAvgWaitingTimeEvent(XEvent event, Long[] wins) {
		String actName = XConceptExtension.instance().extractName(event);
		if (wins[0] != -1) {
			for (long w = wins[0]; w <= wins[3]; w++) {
//				System.out.println(actName);
//				System.out.println(aggregatedAvgValues.get("avgWaitingTimeEvent_" + actName).get(w));
//				System.out.println(" waiting time: " + waitingTime(event));
				long d = aggregatedAvgValues.get("Average waiting time Event : " + actName).get(w) + waitingTime(event);
//				aggregatedAvgValues.get("Average waiting time Event : " + actName).remove(w);
				aggregatedAvgValues.get("Average waiting time Event : " + actName).put(w, d);
			}
		}
	}
	
	/**
	 * Returns the waiting time of an event. 
	 * ASSUMPTION : waiting time of an event is stored in the event as "waitingTime".
	 * @param event
	 * @return waiting time in millisecond
	 */
	private Long waitingTime(XEvent event) {
		XAttributeMap eventMap = event.getAttributes();
		if (eventMap.containsKey("waitingDuration")) 
			return Long.parseLong(eventMap.get("waitingDuration").toString());
		
		return -1L;
	}

	private void updateAvgServiceTimeEvent(XEvent event, Long[] wins) {
		String actName = XConceptExtension.instance().extractName(event);
		for (long w = wins[1]; w <= wins[2]; w++) {
//			System.out.println(durationEvent(event));
//			System.out.println(aggregatedAvgValues.get("avgServiceTime_" + actName));
//			System.out.println(aggregatedAvgValues.get("avgServiceTime_" + actName).get(w));
			long d = 0;
			if (durationEvent(event) != null)
				d = aggregatedAvgValues.get("Average service time Event : " + actName).get(w) + durationEvent(event);
			else 
				d = aggregatedAvgValues.get("Average service time Event : " + actName).get(w) + 0;
//			aggregatedAvgValues.get("Average service time Event : " + actName).remove(w);
			aggregatedAvgValues.get("Average service time Event : " + actName).put(w, d);
		}
	}
	
	/**
	 * @param event
	 * @return the duration of the event in millisecond
	 */
	private Long durationEvent(XEvent event) {
		XAttributeMap eventMap = event.getAttributes();
		
  		if (!eventMap.containsKey("activityduration")) 
			return null;
		
		return Long.parseLong(eventMap.get("activityduration").toString());
	}
	
	/**
	 * update the number of waiting customers in the process.
	 * @param wins
	 */
	private void updateNumwaiting(Long[] wins) {
		if (wins[0] != -1L) {
			for (long w = wins[0]; w <= wins[3]; w++) {
				Long n = aggregatedValues.get("Process number waiting").get(w) + 1;
//				aggregatedValues.get("Process number waiting").remove(w);
				aggregatedValues.get("Process number waiting").put(w, n);
			}
		}
	}

	private void updateNumWaitingEvent(XEvent event, Long[] wins) {
		String actName = XConceptExtension.instance().extractName(event);
		if (wins[0] != -1L) {
			for (long w = wins[0]; w <= wins[3]; w++) {
				Long n = aggregatedValues.get("Number of waiting Event : " + actName).get(w) + 1;
				aggregatedValues.get("Number of waiting Event : " + actName).remove(w);
				aggregatedValues.get("Number of waiting Event : " + actName).put(w, n);
			}
		}
		
	}

	private void updateNumEvent(XEvent event, Long[] wins) {
		String actName = XConceptExtension.instance().extractName(event);
		String name = "Number of active Event : " + actName;
		for (long w = wins[1]; w <= wins[2]; w++) {
			Long n = aggregatedValues.get(name).get(w) + 1;
			aggregatedValues.get(name).remove(w);
			aggregatedValues.get(name).put(w, n);
		}
	}

	/**
	 * Update the resource workload in the proper windows.
	 * @param event
	 * @param wins
	 */
	private void updateResourceWorkLoad(XEvent event, Long[] wins) {
		String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
		
		if(resource == null)
			return;
		
		for (long w = wins[1]; w <= wins[2]; w++) {
			Long n = aggregatedValues.get("WorkLoad Resource : " + resource).get(w) + 1;
	//		aggregatedValues.get("WorkLoad Resource : " + resource).remove(w);
			aggregatedValues.get("WorkLoad Resource : " + resource).put(w, n);
		}	
	}

	/**
	 * wins[0] first window where waiting starts for this event
	 * wins[3] the window where waiting ends for this event
	 * wins[1] is the first window that event is active there
	 * wins[2] the last window that event is active there
	 * If time point is in the middle of a window then the time window is counted 
	 * for two categories; e.g., both active and waiting.
	 */
	private Long[] findWondows(XEvent event) {
		Long[] wins = new Long[4];
		
		// wins[1] : starting window
		Date timestamp = XTimeExtension.instance().extractTimestamp(event);
		long milliseconds = timestamp.getTime() - minTimestamp.getTime();
		wins[1] = milliseconds / window;
		
		// wins[0] : waiting window
		Date wt = getWaitingTimestamp(event);
		if (wt == null) 
			wins[0] = -1L; // No waiting time is recorded.
		else {
			wins[0] = (wt.getTime() - minTimestamp.getTime()) / window;
		}
		
		// w[4] : end waiting time
		if (milliseconds % window == 0)
			wins[3] = milliseconds / window - 1;
		else
			wins[3] = milliseconds / window ;
		
		Date et = getEndTimestamp(event);
//		System.out.println(et.getTime());
//		System.out.println(minTimestamp.getTime());
//		System.out.println(et.getTime() % window);
//		System.out.println(window);
		if (et.getTime() % window == 0)
			wins[2] = (et.getTime() - minTimestamp.getTime()) / window - 1;
		else
			wins[2] = ((et.getTime() - minTimestamp.getTime()) / window );

		
		return wins;
	}
	
	public void setWindow(long l) {
		window = l;
	}
	
	public int getNumIndices() {
		Map<Long, Long> oneRow = new HashMap<>();
		for (String str : aggregatedValues.keySet()) {
			oneRow = aggregatedValues.get(str);
			break;
		}
		
		return oneRow.size();
	}
	
	/**
	 * @param attName
	 * @param event
	 * @return returns the value of attName in the window which includes timestamp of the event.
	 * (timestamp is considered as a start timestamp)
	 */
	public Object getAttValue(String attName, XEvent event, long offset) {
		Date timestamp = XTimeExtension.instance().extractTimestamp(event);
		timestamp = new Date(timestamp.getTime() - offset);
		
		long index = getIndex(timestamp);
		String actName = XConceptExtension.instance().extractName(event);
		String name = null;

		name = getAttname(attName, event);
			
		if (aggregatedAvgValues.containsKey(name))
			return aggregatedAvgValues.get(name).get(index);
		if (aggregatedValues.containsKey(name))
			return aggregatedValues.get(name).get(index);
		return null;
	}
	
	/**
	 * 
	 * @param attName
	 * @param event
	 * @return the complete attribute name that can be found in the aggregatedValues or aggregatedAvgValues.
	 */
	public String getAttname(String attName, XEvent event) {
		// actName
		String actName = XConceptExtension.instance().extractName(event);
		// resource
		String resource = org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event);
		if (attName.equals("Number of active Event"))
			return "Number of active Event : " + actName;
		if (attName.equals("Number of waiting Event"))
			return "Number of waiting Event : " + actName;
		if (attName.equals("Average service time Event"))
			return "Average service time Event : " + actName;
		if (attName.equals("Average waiting time Event"))
			return "Average waiting time Event : " + actName;
		
		if (attName.equals("Resource workLoad"))
			return "WorkLoad Resource : " + resource;
		if (attName.equals("Number waiting for Resource"))
			return "Number waiting for Resource : " + resource;
		if (attName.equals("Average service time Resource"))
			return "Average service time Resource : " + resource;
		if (attName.equals("Average waiting time Resource"))
			return "Average waiting time Resource : " + resource;
		
		return attName;
	}

	/**
	 * @param attName
	 * @param trace
	 * @return returns the value of attName in the window which includes timestamp 
	 * of the last event in the trace.
	 * (timestamp is considered as a start timestamp)
	 */
	public Object getAttValue(String attName, XTrace trace, long offset) {
		if (trace.size() < 1)
			return null;
		
		Date timestamp = XTimeExtension.instance().extractTimestamp(trace.get(trace.size() - 1));
		timestamp = new Date(timestamp.getTime() - offset);
		
		long index = getIndex(timestamp);
		if (aggregatedAvgValues.containsKey(attName))
			return aggregatedAvgValues.get(attName).get(index);
		if (aggregatedValues.containsKey(attName))
			return aggregatedValues.get(attName).get(index);
		
		return null;
	}
	
	/**
	 * @param attName
	 * @param timeStamp in Milliseconds
	 * @return returns the value of attName in the window which includes the timestamp.
	 */
	public Object getAttValue(String attName, Long time) {
		long index = getIndex(time);
		if (aggregatedAvgValues.containsKey(attName))
			return aggregatedAvgValues.get(attName).get(index);
		if (aggregatedValues.containsKey(attName))
			return aggregatedValues.get(attName).get(index);
		
		return null;
	}
	
	/**
	 * returns all attribute names.
	 * @return attNames \cup avgAttName
	 */
	public Set<String> getAttNames() {
		Set<String> allAttNames = new HashSet<>();
		for (String attName : attNames)
			allAttNames.add(attName);
		for (String attName : avgAttNames)
			allAttNames.add(attName);
		
		return allAttNames;
	}
	
	public Map<Long, Long> get(String attName) {
		
		if (aggregatedAvgValues.containsKey(attName))
			return aggregatedAvgValues.get(attName);
		else
			return aggregatedValues.get(attName);
	}
	
	public Date getMinTimestamp() {
		return minTimestamp;
	}
	
	public Date getMaxTimestamp() {
		return maxTimestamp;
	}
	
	/**
	 * @param startTime
	 * @return
	 */
	public Map<String, Object> getProcessLevelAttValues(Long startTime, long timeSpan) {
		Map<String, Object> instance = new HashMap<>();
		
		String name = "Number of active Event"; 
		String bothTime = "no";
		
		for (String attName : aggregatedValues.keySet())
			if (attName.contains(name))
				instance.put(attName, getAttValue(attName, startTime));
		for (String attName : aggregatedAvgValues.keySet())
			if (attName.contains(name))
				instance.put(attName, getAttValue(attName, startTime));
		if (bothTime.equals("yes")) {
			for (String attName : aggregatedValues.keySet())
				if (attName.contains(name))
					instance.put(attName + " ( 2 )", getAttValue(attName, startTime + timeSpan));
			for (String attName : aggregatedAvgValues.keySet())
				if (attName.contains(name))
					instance.put(attName + " ( 2 )", getAttValue(attName, startTime + timeSpan));
		}
		
		//add class att
		instance.put("Process workLoad", getAttValue("Process workLoad", startTime));
		instance.put("Process workLoad ( 2 )", getAttValue("Process workLoad", startTime + timeSpan));
		
		return instance;
	}
	
	// ------------------------------- Test -----------------------------
	// set the event log
	public XLog setupEventLog() {
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XLog log = factory.createLog();
		
		XTrace t1 = trace();
		log.add(t1);
		XTrace t2 = trace();
		log.add(setTimeStampsT2(t2));
		XTrace t3 = trace();
		log.add(setTimeStampsT3(t3));
		
		setResources(log);
		return log;
	}
	
	// second trace happened 1/2 day before first trace
	private XTrace setTimeStampsT2(XTrace t2) {
		XsDateTimeConversion dateConvertor = new XsDateTimeConversion();
		String strA = "2020-01-01T13:00:00.000+01:00";
		String strB = "2020-01-03T13:00:00.000+01:00";
		String strC = "2020-01-06T01:00:00.000+01:00";
		Date tsA = null;
		Date tsB = null;
		Date tsC = null;
		XTimeExtension.instance().assignTimestamp(t2.get(0), dateConvertor.parseXsDateTime(strA)); // timestamp A
		XTimeExtension.instance().assignTimestamp(t2.get(1), dateConvertor.parseXsDateTime(strB)); // timestamp B
		XTimeExtension.instance().assignTimestamp(t2.get(2), dateConvertor.parseXsDateTime(strC)); // timestamp C
	    
	    return t2;
	}
	
	// Third trace happened 1 day after the first trace.
	private XTrace setTimeStampsT3(XTrace t3) {
		XsDateTimeConversion dateConvertor = new XsDateTimeConversion();
		String strA = "2020-01-03T01:00:00.000+01:00";
		String strB = "2020-01-05T01:00:00.000+01:00";
		String strC = "2020-01-07T13:00:00.000+01:00";
		Date tsA = null;
		Date tsB = null;
		Date tsC = null;
		XTimeExtension.instance().assignTimestamp(t3.get(0), dateConvertor.parseXsDateTime(strA)); // timestamp A
		XTimeExtension.instance().assignTimestamp(t3.get(1), dateConvertor.parseXsDateTime(strB)); // timestamp B
		XTimeExtension.instance().assignTimestamp(t3.get(2), dateConvertor.parseXsDateTime(strC)); // timestamp C
	    
	    return t3;
	}
	
	/**
	 * Setting the first trace (and the body of all traces)
	 * Each trace is in the form <A, B, C>
	 * A starts at 01.01.2020 takes 1 day waiting time 0
	 * B starts at 04.01.2020 takes 2 days waiting time 1 day
	 * C starts at 06.01.2020 takes 1 day waiting time 1/2 day
	 * @return forst trace
	 */
	private XTrace trace() {
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XTrace t = factory.createTrace();
		String[] actNames = {"A", "B", "C"};
		for (String actName : actNames) {
			t.add(event(actName));
		}
		return t;
	}
	
	private XEvent event(String name) {
		XsDateTimeConversion dateConvertor = new XsDateTimeConversion();
		XEvent e = new XEventImpl();
		XConceptExtension.instance().assignName(e, name); // activity name
		String str = timestamp(name);
		XTimeExtension.instance().assignTimestamp(e, dateConvertor.parseXsDateTime(str)); // timestamp
	    XAttributeMap mapa1 = e.getAttributes();
	    long wt = waitingTime(name);
		XAttributeDiscreteImpl wta1 = new XAttributeDiscreteImpl("waitingDuration", wt); // waiting time 0
		mapa1.put("waitingDuration", wta1);
		long d = durationTime(name);
		XAttributeDiscreteImpl dta1 = new XAttributeDiscreteImpl("activityduration", d); // duration time 1 day = 86400000 ms
		mapa1.put("activityduration", dta1);
		
		return e;
	}
	
	public void setResources(XLog log) {
		for (XTrace trace : log)
			for (XEvent event : trace)
				XOrganizationalExtension.instance().assignResource(event, "Jili");
		
		XOrganizationalExtension.instance().assignResource(log.get(0).get(0), "Bob");
		XOrganizationalExtension.instance().assignResource(log.get(0).get(1), "Bob");
		XOrganizationalExtension.instance().assignResource(log.get(1).get(0), "Bob");
	}

	private long durationTime(String name) {
		if (name.equals("A"))
			return 86400000; // A
		if (name.equals("B"))
			return 86400000 * 2;  // B
		return 86400000; // C
	}

	private long waitingTime(String name) {
		if (name.equals("A"))
			return 0; // A
		if (name.equals("B"))
			return 86400000;  // B
		return 86400000/2; // C
	}

	private String timestamp(String name) {
		if (name.equals("A"))
			return "2020-01-02T01:00:00.000+01:00"; // A
		if (name.equals("B"))
			return "2020-01-04T01:00:00.000+01:00";  // B
		return "2020-01-06T13:00:00.000+01:00"; // C
	}
	
	public void printLog() {
		for(XTrace trace : log) {
			System.out.println("trace");
			for (XEvent event : trace)
				printEvent(event);
		}
	}
	
	private void printEvent(XEvent event) {
		System.out.println("act name : " + XConceptExtension.instance().extractName(event));
//		System.out.println("resource : " + org.deckfour.xes.extension.std.XOrganizationalExtension.instance().extractResource(event));
		System.out.println("timestamp : " + XTimeExtension.instance().extractTimestamp(event));
		XAttributeMap eventMap = event.getAttributes();
		System.out.println("duration : " + eventMap.get("activityduration").toString());
		System.out.println("waiting time : " + eventMap.get("waitingDuration").toString());
	}

	public AggregateAttributes() {
		this.log = setupEventLog();
		setWindow(43200000 );   // 12 h  
		printLog();
		initiate();
	}

	public XLog getLog() {
		return log;
	}
	
	public void printAttValues() {
		for (String attName : aggregatedValues.keySet()) {
			System.out.println(attName);
			String str = new String();
			for (Long l : aggregatedValues.get(attName).keySet())
				str = str + l + " : " + aggregatedValues.get(attName).get(l) + " // ";
			System.out.println(str);
		}
		
		System.out.println("\n\n");
		
		for (String attName : aggregatedAvgValues.keySet()) {
			System.out.println(attName);
			String str = new String();
			for (Long l : aggregatedAvgValues.get(attName).keySet())
				str = str + l + " : " + aggregatedAvgValues.get(attName).get(l) + " // ";
			System.out.println(str);
		}
	}
	
	public static void main(String[] arfs) {
		
		AggregateAttributes a = new AggregateAttributes();
		XsDateTimeConversion dateConvertor = new XsDateTimeConversion();
		
		Date ds = a.getStartTimestamp(a.getLog().get(0).get(1));
		if (!ds.equals(dateConvertor.parseXsDateTime("2020-01-04T01:00:00.000+01:00")))
			System.out.println("start time is not correct");
		
		Date dw = a.getWaitingTimestamp(a.getLog().get(0).get(1));
		if (!dw.equals(dateConvertor.parseXsDateTime("2020-01-03T01:00:00.000+01:00")))
			System.out.println("waiting time is not correct");
		
		Date de = a.getEndTimestamp(a.getLog().get(0).get(1));
		if (!de.equals(dateConvertor.parseXsDateTime("2020-01-06T13:00:00.000+01:00")))
			System.out.println("end time is not correct");
		
		long l1 = a.getIndex(XTimeExtension.instance().extractTimestamp(a.getLog().get(0).get(0)));
		System.out.println("start window first trace : " + l1);
		
		long l2 = a.endIndex(a.getEndTimestamp(a.getLog().get(0).get(2)));
		System.out.println("end window first trace : " + l2);
	
		Long[] w = a.findWondows(a.getLog().get(0).get(1));
		for (long l : w)
			System.out.println(l);
		
		w = a.findWondows(a.getLog().get(0).get(0));
		for (long l : w)
			System.out.println(l);
			
		a.setValues();
		a.printAttValues();
		
		Map<String, Object> instance = a.getProcessLevelAttValues(a.getMinTimestamp().getTime() + 10, 3600000);
		for (String attName : instance.keySet())
			System.out.println(attName + "  " + instance.get(attName).toString());
	}
}
