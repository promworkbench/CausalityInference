package org.processmining.causalityinference.help;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class CountEvents {
	private Map<String, Integer> countEvents;
	private XLog log;
	
	public CountEvents(XLog log) {
		this.log = log;
	}
	
	public void count() {
		countEvents = new HashMap<String, Integer>();
		for (XTrace trace : log)
			for (XEvent event : trace) {
				String eventName = XConceptExtension.instance().extractName(event);
				int count = countEvents.containsKey(eventName) ? countEvents.get(eventName) : 0;
				countEvents.put(eventName, count + 1);
			}
	}
	
	public Map<String, Integer> getCountAllEvents() {
		return countEvents;
	}
	
	public Integer getCount(String eventName) {
		return countEvents.get(eventName);
	}
}