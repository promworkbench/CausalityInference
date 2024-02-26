package org.processmining.causalityinference.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.TimeUnit;
import org.processmining.xeslite.external.XFactoryExternalStore.MapDBDiskImpl;

/**
 * The purpose of this class is finding all the forbidden directions regarding
 * a given data log. A forbidden direction A --> B exists if A never be eventually 
 * followed by B (at least up to a vary small threshold). If A --> B be a forbidden 
 * direction then B can not be the cause of A.
 * @author qafari
 *
 */
public class ForbiddenDirections {
	private XLog log;
	private double threshold = 0;
	private Parameters params;
	private Map<String, Map<String, Integer>> eventuallyfollowCounts;  // (A, (B , c )) A happens before B in the same trace c times 
	private Map<String, Set<String>> forbiddenDirections; // (u, V) means \forall v \in V : u --> v is forbidden.
	private Collection<String> filterActNames;
	
	public ForbiddenDirections(XLog log) {
		this.log = log;
		eteventuallyfollowCounts();
		findForbiddenDirections();
	}
	
	public ForbiddenDirections(Parameters params) {
		log = params.getLog();
		this.filterActNames = params.getActivitiesToConsider();
		if (!filterActNames.isEmpty())
			cleanLog(log);
		eteventuallyfollowCounts();
		findForbiddenDirections();
		addAggAttForbiddenDirections();
	}
	
	/**
	 * for each aggregated attribute add a forbidden direction to every aggregated attribute that happens before (with no overlap).
	 * Moreover, for process workload, adds a forbidden direction to every other aggregated attribute.
	 */
	private void addAggAttForbiddenDirections() {
		// TODO Auto-generated method stub
		// find non overlapping timelines
		
		
	}
	/**
	 * If there are more than one time line in the data, then each entry of the list is a pair of time-line indices 
	 * such that the first one happens before the second one and they are non overlapping.
	 * In each pair we have:
	 * 			 offset2 - granularity2 - offset1  >= 0
	 * @return
	 */
	private LinkedList<Integer[]> pastFutureTimeLines() {
		// TODO Auto-generated method stub
		// unify the time units to milliseconds
		LinkedList<Integer[]> pairPastFuture = new LinkedList<>();
		
		LinkedList<Long[]> timeLines = new LinkedList<>();
		for (Object[] timeLine : params.getTimeLines()) {
			Long[] newTimeLine = new Long[3];
			newTimeLine[0] = params.getTiemUnitMillisec((TimeUnit) timeLine[0]);
			newTimeLine[1] = params.getWindowSize(timeLine[1].toString(), (TimeUnit) timeLine[0]);
			newTimeLine[2] = params.getWindowSize(timeLine[2].toString(), (TimeUnit) timeLine[0]);
			timeLines.add(newTimeLine);
		}
		
		for (int i = 0; i < timeLines.size() - 1; i++)
			for (int j = i+1; j < timeLines.size(); j++) {
				if (timeLines.get(j)[2] - timeLines.get(j)[1] - timeLines.get(i)[2]  >= 0) {
					Integer[] indices = new Integer[2];
					indices[0] = j;
					indices[1] = i;
					pairPastFuture.add(indices);
				}
				if (timeLines.get(i)[2] - timeLines.get(i)[1] - timeLines.get(j)[2]  >= 0) {
					Integer[] indices = new Integer[2];
					indices[0] = i;
					indices[1] = j;
					pairPastFuture.add(indices);
				}	
			}
		
		return pairPastFuture;
	}

	public void cleanLog(XLog oldLog) {
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XLog newLog = factory.createLog(oldLog.getAttributes());
		
		for (XTrace trace : oldLog)
		{
			XTrace newTrace = factory.createTrace(trace.getAttributes());
			
			for(XEvent event : trace)
			{
				XEvent newEvent = factory.createEvent(event.getAttributes());
				if (filterActNames.contains(XConceptExtension.instance().extractName(event)))
					newTrace.add(newEvent);
			}
			newLog.add(newTrace);
		}
		
		this.log = newLog;
	}
	
	public void setThreshold(double d) {
		if (d < 1 && d > 0)
			threshold = d;
	}
	
	/**
	 * This function compute the number of times that 
	 * each activity happens before the other one.
	 */
	public void eteventuallyfollowCounts() {
		initializeTheMap();
		for (XTrace trace : log) 
			for (int i = 0; i < trace.size() - 1; i++) 
				for (int j = i; j < trace.size(); j++) {
					String from = XConceptExtension.instance().extractName(trace.get(i));
					String to = XConceptExtension.instance().extractName(trace.get(j));
					int count =  eventuallyfollowCounts.get(from).get(to) + 1;
					eventuallyfollowCounts.get(from).remove(to);
					eventuallyfollowCounts.get(from).put(to, count);
				}			
	}
	
	public void initializeTheMap() {
		eventuallyfollowCounts = new HashMap<String, Map<String, Integer>>();
		Set<String> actNames = getAllActivityNames();
		
		for (String actName : actNames) {
			 Map<String, Integer> initCounts = new HashMap<String, Integer>();
			 for (String name : actNames)
				 initCounts.put(name, 0);
			 eventuallyfollowCounts.put(actName, initCounts); 
		}
	}
	
	public Set<String> getAllActivityNames() {
		Set<String> actNames = new HashSet<String>();
		for (XTrace trace : log) 
			for (XEvent event : trace) 
				actNames.add(XConceptExtension.instance().extractName(event));
		
		return actNames;
	}
	
	public void findForbiddenDirections() {
		forbiddenDirections = new HashMap<String, Set<String>>();
		for (String attName : eventuallyfollowCounts.keySet()) 
			forbiddenDirections.put(attName, new HashSet<String>());
		
		for (String from : eventuallyfollowCounts.keySet()) 
			for (String to : eventuallyfollowCounts.keySet()) {
				int forwardCount = eventuallyfollowCounts.get(from).get(to);
				int backwardCount = eventuallyfollowCounts.get(to).get(from);
				if (forwardCount > 0 && backwardCount <= (forwardCount * threshold))
					forbiddenDirections.get(to).add(from);
			}
	}
	
	public Map<String, Set<String>> getForbiddenDirections() {
		return forbiddenDirections;
	}
	
	public void printForbiddenDirections() {
		for (String from : forbiddenDirections.keySet()) {
			if (!forbiddenDirections.get(from).isEmpty())
				for (String to : forbiddenDirections.get(from))
					System.out.println(from + " --> " + to);
			System.out.println();
		}
	}
}
