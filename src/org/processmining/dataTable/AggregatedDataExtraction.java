package org.processmining.dataTable;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.processmining.causalityinference.algorithms.SituationFeatureTableVisualizer;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.causalityinference.parameters.TimeUnit;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;

/**
 * Given an event-log, a time window (in milliseconds), and a 
 * target feature and a set of independent features, we create 
 * a target sensitive tabular aggDataset of the aggregated attributes.
 * We do the following two steps:
 * 	1- a situation event log of the prefixes of the traces that 
 * 	end with the event containing the target feature (or the 
 *	 situation log = log, if the target feature is a trace attribute).
 *	2- we extract the aggData out of the situations. Each situation will
 *	convert to a row in the aggData table. 
 */
public class AggregatedDataExtraction {
	private XLog log;
	
	public final static String notAllowedChars=".()&!|=<>-+*/% ";
	
	/**
	 * an event log in which each trace is a prefix of the log.
	 *    - event situation
	 *    		prefix ends with the event of target feature
	 *    - trace situation
	 *    		prefixes are the whole traces
	 *    //TODO add choice situation!
	 */
	private XLog situationLog;
	
	/**
	 * The time window for the aggregated attributes.
	 */
	private long window;
	
	/**
	 * The type of the situation. 
	 * 	- Trace situation
	 * 	- Event situation
	 * 	- Choice situation
	 */
	private SituationType situationType = SituationType.TS;
	
	/**
	 * The map including all the aggregated values for all the attribute
	 * names and all the time windows. for current attribute values.
	 */
	private LinkedList<AggregateAttributes> aggData;
	
	/**
	 * The map including all the aggregated values for all the attribute
	 * names and all the time windows. for past attribute values.
	 */
//	private AggregateAttributes aggDataPast;
	
	/**
	 * The grouper of the events that contain the target
	 * attribute (in case of event situation).
	 */
	private EventGrouper eventGroup;
	
	/**
	 * The target attribute name.
	 */
	private String targetAttName;
	
	/**
	 * The final tabular aggData. Each row of the table is stored in the form of a
	 * map of attribute names and values.
	 */
	private LinkedList<Map<String, Object>> tabularData;
	
	/**
	 * In case of evaluation, The testData include 30 percent of the data and 
	 * trainData includes 70 percent. //TODO percentages may change ;)
	 */
	private LinkedList<Map<String, Object>> testData;
	
	/**
	 * In case of evaluation, The testData include 30 percent of the data and 
	 * trainData includes 70 percent. //TODO percentages may change ;)
	 */
	private LinkedList<Map<String, Object>> trainData;
	
	/**
	 * (aggregated attributes)
	 * The set of trace attribute names (aggregated attributes). In case of trace situation, includes
	 * target attribute name also.
	 */	
	Set<String> aggTraceAttNames; 

	/**
	 * (aggregated attributes)
	 * The set of event attribute names (aggregated attributes). In case of event or choice situation, 
	 * also may include target attribute name.
	*/ 
	private Set<String> aggEventAttNames; 
	
	/**
	 * (normal attributes)
	 * The set of trace attribute names (normal attributes). In case of trace situation, includes
	 * target attribute name also.
	 */	
	Set<String> traceAttNames; 

	/**
	 * (normal attributes)
	 * The set of event attribute names (normal attributes). In case of event or choice situation, 
	 * also may include target attribute name.
	*/ 
	private Set<String> eventAttNames; 
	
	/**
	 * The set of activity names of the events that their attribute values have
	 * to be extracted.
	 */
	private Set<String> activitiesToConsider;
	
	/**
	 * The set of  attribute names. In case of event or choice situation, 
	 * also may include target attribute name.
	 * 
	private Set<String> attNames;*/
	
	/**
	 * The set of parameters for creating the table
	 */
	private Parameters params;
	
	/**
	 * Indicate the type of each attribute.
	 */
	private Map<String, Type> attTypes = new HashMap<String, Type>();
	
	/**
	 * Include the min and max value of the discrete, continues, and timestamp 
	 * attributes in the tabular data.
	 */
	private Map<String, Object[]> minMax = new HashMap<String, Object[]>();
	
	/**
	 * A map from boolean and categorical attribute names to a set including all of their values;
	 */
	private Map<String, Set<String>> literalValues = new HashMap<String, Set<String>>();
	
	/**
	 * JScrollPanel including the tabular data for visualization
	 */
	private JScrollPane tablePanel;
	
	/**
	 * If the tabularData has any missing value
	 */
	private boolean hasMissingValue;
	
	/**
	 * creates an intermediate data-table // TODO get ride of it!
	 */
	TableCreatorOneHot tbch = null;
	
	/**
	 * creates the panel that includes that table / TODO get ride of it! or make it local
	 */
	SituationFeatureTableVisualizer te;
	
	public AggregatedDataExtraction(Parameters params) {
		this.log = params.getLog();
		this.params = params;
		initiate();
	}
	
	public void initiate() {	
		setSituationType(params.getSituationType());
		setTraceAttNames(params.getSelectedNormalTraceAttNames());
		setEventAttNames(params.getSelectedNormalEventAttNames());
		setAggTraceAttNames(params.getSelectedAggTraceAttNames());
		setAggEventAttNames(params.getSelectedAggEventAttNames());
		setActivitiesToConsider(params.getActivitiesToConsider());
		setTargetAttName(params.getClassAttName());
		if (situationType.equals(SituationType.ES) || situationType.equals(SituationType.CS))
			eventGroup = new EventGrouper(params);
		
		if (situationType.equals(SituationType.TS))
			if (params.getSetOfAggAttNames().contains(targetAttName)) {
				if (aggTraceAttNames == null)
					aggTraceAttNames = new HashSet<String>();
				aggTraceAttNames.add(targetAttName);
			}
			else {
				if (traceAttNames == null) {
					traceAttNames = new HashSet<>();
					traceAttNames.add(targetAttName);
				} else
					traceAttNames.add(targetAttName);
			}
		
		if (aggAttIsSelected() || params.getSituationType().equals(SituationType.PL))
			aggData = params.getAggData();
		
/**		if (params.isPastIncluded())
			aggDataPast = params.getAggDataPast(); */
		
		if (params.getIndepChoice()) 
			addChoiceToEventAtts();
		
		if (params.getIndepSubmodel())
			addSubModelToTraceAtts();
		// TODO add class attName to the proper att Name set fpr choice situation
	}
	
	/**
	 * If Sub<odel attribute check box is selected, then the sub_model is
	 *  added to the trace attributes. 
	 */
	private void addSubModelToTraceAtts() {
		if (traceAttNames == null)
			traceAttNames = new HashSet<>(); 
		
		traceAttNames.add("Sub model duration");
		
	}

	/**
	 * If Choice Independent attribute check box is selected, then the selected 
	 * choice placess are added to the event attributes
	 */
	private void addChoiceToEventAtts() {
		if (params.getSelectedORplace() != null && !params.getSelectedORplace().isEmpty()) {
			if (eventAttNames == null)
				eventAttNames = new HashSet<>();
			for (Place place : params.getSelectedORplace())
				eventAttNames.add(place.getLabel());
		}	
	}

	/**
	 * creates a situation log in which at the last event of each 
	 * situation an attribute called situation=true is added.
	 */
	private void setSituationLog() {
		//Trace situation
		if (situationType.equals(SituationType.TS)) {
			situationLog = log;
			return;
		}
		//Event or choice situation
		else if ((situationType.equals(SituationType.ES))  || (situationType.equals(SituationType.CS))){
			for (XTrace trace : log){
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						XAttributeMap tmap = event.getAttributes();
						XAttributeBoolean att = new XAttributeBooleanImpl("situation", true);
						tmap.put("situation", att);
					}
				}
			}
			situationLog = log;
		}	
		
		System.out.println("situation log is created!");
	}
/**	
	 * Creates the situation log with the proper prefixes.
	 * //TODO add the choice situation!
	 
	private void setSituationLog() {
		//Trace situation
		if (situationType.equals(SituationType.TS)) {
			situationLog = log;
			return;
		}
		//Event situation
		if (situationType.equals(SituationType.ES)) {
			MapDBDiskImpl factory = new MapDBDiskImpl();
			situationLog = factory.createLog(log.getAttributes());
			int i = 0;
			for (XTrace trace : log)
			{
				System.out.println(i);
				i++;
				XTrace newTrace = factory.createTrace(trace.getAttributes());
				for (XEvent event : trace) {
					XEvent newEvent = factory.createEvent(event.getAttributes());
					newTrace.add(newEvent);
					if (eventGroup.isEventAnClassEvent(event)) {
						XTrace t = copy(newTrace);
						situationLog.add(t);
					}
				}
			}
		}
		//Choice situation
		MapDBDiskImpl factory = new MapDBDiskImpl();
		situationLog = factory.createLog(log.getAttributes());
		int i = 0;
		for (XTrace trace : log)
		{
			System.out.println(i);
			i++;
			XTrace newTrace = factory.createTrace(trace.getAttributes());
			for (XEvent event : trace) {
				XEvent newEvent = factory.createEvent(event.getAttributes());
				newTrace.add(newEvent);
				if (eventGroup.isEventAnClassEvent(event)) {
					XTrace t = copy(newTrace);
					situationLog.add(t);
				}
			}
		}
		
	} 
		
	private XTrace copy(XTrace trace) {
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XTrace newTrace = factory.createTrace(trace.getAttributes());
		for (XEvent event : trace) {
	//		XEvent newEvent = factory.createEvent(event.getAttributes());
	//		newTrace.add(newEvent);
			newTrace.add(event);
		}

		return newTrace;
	}	*/
	
	/**
	 * Extracting and generating the tabular aggData set (tabularData).
	 */
	public void extractData() {
		setSituationLog();	
//		System.out.println(targetAttName);
//		System.out.println(situationType);
		if (situationType.equals(SituationType.TS))
			traceSituationDataExtraction();
		else if (situationType.equals(SituationType.ES))
			eventDataExtraction();
		else if (situationType.equals(SituationType.CS))
			eventDataExtraction(); //choice situaiton can be handled exactly the same as event situation
		else 
			processLevelDataExtraction();
		
		removeForbiddenCharacters();
		
		if (!tabularData.isEmpty()) 
			for (Map<String, Object> instance : tabularData) 
				doUpdate(instance);
		
		if (params.getSituationType().equals(SituationType.PL))
			divideToTestAndTrain(0.7);
		
		createTable();
	}

	private void divideToTestAndTrain(double t) {
		testData = new LinkedList<>();
		trainData = new LinkedList<>();
		Collections.shuffle(tabularData);
		int num = (tabularData.size()*30) /100;
		
		for (int i = 0; i < num; i++) 
			testData.add(tabularData.get(i));
		for (int i = num; i < tabularData.size(); i++) 
			trainData.add(tabularData.get(i));
		
	
	}

	/**
	 * Remove spaces from attribute names and 
	 */
	private void removeForbiddenCharacters() {
		LinkedList<Map<String, Object>> newTabularData = new LinkedList();
		
		if (!tabularData.isEmpty())  {
			for (Map<String, Object> instance : tabularData) {
				Map<String, Object> newInstance = new HashMap<>();
				for (String attName : instance.keySet()) {
					newInstance.put(replaceNotAllowedStrings(attName), instance.get(attName));
				}
				newTabularData.add(newInstance);
			}
			tabularData = newTabularData;
		}
	}

	/**
	 * removes the not allowed char for the consistency
	 */
   	public String replaceNotAllowedStrings(String str) {
  		
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}

	/**
	 * Process level situation.
	 * Extracting and generating the tabular aggData set (tabularData).
	 */
	private void processLevelDataExtraction() {
		tabularData = new LinkedList<Map<String, Object>>();
		
		Set<String> attNames = getProcessLevelAttNames();
		
		Date startDate = aggData.get(0).getMinTimestamp();
		Long startTime = startDate.getTime() + getOffset(params.getTimeLines().get(0)); 
		Date endDate = aggData.get(0).getMaxTimestamp();
		Long endTime = endDate.getTime() - getOffset(params.getTimeLines().get(0)); 
		long step = getTimeGranularity(params.getTimeLines().get(0));
		
		for (long time = startTime; time <= endTime; time+=step) {
			Map<String, Object> instance = new HashMap<>();
			int idx = 0;
			for (; idx < aggData.size(); idx++) {
				for (String attName : attNames) {
					int i = idx + 1;
					String name = attName;
					if (idx != 0)
						name = name + "_" + i;
					instance.put(name, aggData.get(idx).getAttValue(attName, time - getOffset(params.getTimeLines().get(idx))));
				}
			}
			tabularData.add(instance);
		}
	} 
	
	/**
	 * if situation type == process level
	 * @return the set of all aggregated attribute names.
	 */
	private Set<String> getProcessLevelAttNames() {
		Set<String> attNames = new HashSet<>();
		if (params.getTraceAttsPL() != null)
			attNames.addAll(params.getTraceAttsPL());
		if (params.getReourcesToConsiderPL() != null && params.getResorceAttsPL() != null)
			for (String resAttName : params.getResorceAttsPL())
				for (String resName : params.getReourcesToConsiderPL())
					attNames.add(resAttName + " : " + resName);
		if (params.getEventAttsPL() != null && params.getActivitiesToConsciderPL() != null)
			for (String eventAttName : params.getEventAttsPL())
				for (String actName : params.getActivitiesToConsciderPL())
					attNames.add(eventAttName + " : " + actName);
		
		attNames.add(params.classAttributeNameNoReplacement());
		
		return attNames;
	}

	/**
	 * 
	 * @return true if at least one aggreagated feature has been selected as
	 * class or independent feature. O.w. false.
	 */
	private boolean aggAttIsSelected() {
		if(params.getSetOfAggAttNames().contains(targetAttName))
			return true;
		
		if (aggEventAttNames != null && !aggEventAttNames.isEmpty())
			return true;
		
		if (aggTraceAttNames != null && !aggTraceAttNames.isEmpty())
			return true;
		
		return false;
	}

	/**
	 * Event situation.
	 * Extracting and generating the tabular aggData set (tabularData).

	 */
	private void eventDataExtraction() {
		tabularData = new LinkedList<Map<String, Object>>();
		
		for (XTrace trace : situationLog) {
			Map<String, Object> instance = new HashMap<>();
			extractTraceAttValues(trace, instance);
			for (XEvent event : trace) {
				String actName = XConceptExtension.instance().extractName(event);
				if (activitiesToConsider.contains(actName)) {
					extractValuesOneEvent(trace, event, instance);
				}	
				
				if (isLastEventOfASituation(event)) {
					extratClassAttValue(trace, event, instance);
					if (instance.containsKey(classAttributeName()) && instance.get(classAttributeName()) != null) {
						tabularData.add(copy(instance));
					}
				}
			}
		}
	}
	
	/**
	 * @param event
	 * @return true if the event contain attribute "situation"
	 * i.e., is the last event of a situation.
	 * O.w., false.
	 */
	private boolean isLastEventOfASituation(XEvent event) {
		XAttributeMap emap = event.getAttributes();
		for (String attName : emap.keySet()) {
			if (attName.equals("situation"))
				return true;
		} 
		
		return false;
	}

	/**
	 *  Update the value of the min and max att value (minMax) with one instance.
	 * @param AttName
	 * @param instance
	 */
	private void updateMinMax(String attName, Map<String, Object> instance) {
		Type type = attTypes.get(attName);
		if (type.equals(Type.CONTINUOS) || type.equals(Type.DISCRETE))
			if (!minMax.containsKey(attName)) {
				Object[] o = new Object[2];
				o[0] = instance.get(attName);
				o[1] = instance.get(attName);
				minMax.put(attName, o);
			} else {
				
				if (type.equals(Type.CONTINUOS))
					if ((double)instance.get(attName) < (double)minMax.get(attName)[0])
						minMax.get(attName)[0] = instance.get(attName);
					else
						if ((double)instance.get(attName) > (double)minMax.get(attName)[1])
							minMax.get(attName)[1] =instance.get(attName);
				
				if (type.equals(Type.DISCRETE)) 
					if (instance.get(attName) instanceof Long) {
						if ((long)instance.get(attName) < (long)minMax.get(attName)[0])
							minMax.get(attName)[0] = instance.get(attName);
						else
							if ((long)instance.get(attName) > (long)minMax.get(attName)[1])
								minMax.get(attName)[1] = instance.get(attName);
					} else {
						if ((int)instance.get(attName) < (int)minMax.get(attName)[0])
							minMax.get(attName)[0] = instance.get(attName);
						else
							if ((int)instance.get(attName) > (int)minMax.get(attName)[1])
								minMax.get(attName)[1] = instance.get(attName);
					}	
			}	
	}
	
	/**
   	 * This function updates the literalValueNDc and attTypes according to the new instance.
   	 * @param attName
   	 * @param newInstanceNDC
   	 */
   	public void doUpdate(Map<String, Object> newInstanceNDC) {
   		for (String attName : newInstanceNDC.keySet()) {
   			if (newInstanceNDC.get(attName)!=null) { 	   			
   				updateTypes(attName, newInstanceNDC);
   				updateMinMax(attName, newInstanceNDC);
   			}		
   		}
   	}
   	
   	/**
   	 * Updating the Types of attributes if it does not contain the current attribute type.
   	 * Gather the values of the categorical attributes. (literalValues)
   	 * @param attName
   	 * @param instance
   	 */
   	private void updateTypes(String attName, Map<String, Object> instance) {
   		if (!attTypes.containsKey(attName) ) {
  			attTypes.put(attName, generateDataElement(instance.get(attName)));
  		}

   		if ((instance.get(attName) instanceof String) || attTypes.get(attName).equals(Type.BOOLEAN) || attTypes.get(attName).equals(Type.DISCRETE)){
   			Set<String> valueSet = literalValues.get(attName);
   			if (valueSet == null)	{
   				valueSet = new HashSet<String>();
   				literalValues.put(attName,valueSet);
   			}
   			valueSet.add(instance.get(attName).toString());
  		}
	}
   	
   	/**
   	 * Event situation
   	 * Add the clssattName and its value to the instance
   	 * @param trace
   	 * @param instance
   	 */
	private void extratClassAttValue(XTrace trace, XEvent event, Map<String, Object> instance) {
		if (params.getSetOfAggAttNames().contains(targetAttName))
			instance.put(classAttributeName(), aggData.get(0).getAttValue(targetAttName, event, getOffset(params.getTimeLines().get(0))));
		else {
			NormalAttributes normalAtts = new NormalAttributes(params);
			instance.put(classAttributeName(), normalAtts.getValue(trace, event, targetAttName));
		}
		
		// TODO choice situation if needed ;)
		
	}
	
	public long getOffset(Object[] timeLine) {
		return params.getWindowSize(timeLine[2].toString(), (TimeUnit) timeLine[0]);
	}
	
	public long getTimeGranularity(Object[] timeLine) {
		return params.getWindowSize(timeLine[1].toString(), (TimeUnit) timeLine[0]);
	}

	private Map<String, Object> copy(Map<String, Object> instance) {
		Map<String, Object> newInstance = new HashMap<>();
		for (String attName : instance.keySet())
			newInstance.put(attName, instance.get(attName));
		
		return newInstance;
	}

	/**
	 * Trace situation.
	 * Extracting and generating the tabular aggData set (tabularData).

	 */
	private void traceSituationDataExtraction() {
		tabularData = new LinkedList<Map<String, Object>>();
		
		for (XTrace trace : situationLog) {
			Map<String, Object> instance = new HashMap<>();
			extractTraceAttValues(trace, instance);
			if (activitiesToConsider != null || params.getSelectedORplace() != null)
				extractEventAttValues(trace, instance);
			if (instance.containsKey(classAttributeName()) && instance.get(classAttributeName()) != null) {
				tabularData.add(instance);
			}
		}
	}
	
	/**
	 * Extracting the value the attributes of trace situations of a given trace.
	 * The trace aggregated values related to the first time stamp in the trace is collected.
	 * @param instance
	 */
	private void extractTraceAttValues(XTrace trace, Map<String, Object> instance) {
		// extract the values of the aggregated attributes
		if (aggTraceAttNames != null)
			for (String attName : aggTraceAttNames) {
				int idx = 0;
				for (; idx < aggData.size(); idx++) {
					String name = attName;
					int i = idx + 1;
					if (idx != 0)
						name = attName + "_" + i;
					instance.put(name, aggData.get(idx).getAttValue(attName, trace, getOffset(params.getTimeLines().get(0))));
				}
			}		
		
		// extract the values of the normal attributes
		NormalAttributes normalAtts = new NormalAttributes(params);
		if (traceAttNames != null)
			for (String attName : traceAttNames) 
				instance.put(attName, normalAtts.getValue(trace, trace.get(0), attName)); // the second argument does not play any role
	}
	
	/**
	 * Trace situation
	 * Extracting the value of event situations of a given trace.
	 * The event aggregated values related to the time stamp of the event is collected.
	 * @param trace, instance
	 */
	private void extractEventAttValues(XTrace trace, Map<String, Object> instance) {		
		//Extract the choice and normal att values
		for (XEvent event : trace) {
			String actName = XConceptExtension.instance().extractName(event);
			if (params.getSelectedORplace() != null)
				extractValuesChoiceAtts(trace, event, instance);
			if (activitiesToConsider != null && activitiesToConsider.contains(actName)) 
				extractValuesOneEvent(trace, event, instance);
		}
	}
	
	/**
	 * extract the values of the choice places that have been selected from the event.
	 * @param trace
	 * @param event
	 * @param instance
	 */
	private void extractValuesChoiceAtts(XTrace trace, XEvent event, Map<String, Object> instance) {
		NormalAttributes normalAtts = new NormalAttributes(params);
		for (Place place : params.getSelectedORplace()) 
			instance.put("Choice_" + place.getLabel(), normalAtts.getValue(trace, event, "Choice_" + place.getLabel()));
		
	}

	private void extractValuesOneEvent(XTrace trace, XEvent event, Map<String, Object> instance) {
		String actName = XConceptExtension.instance().extractName(event);		
		if (aggEventAttNames != null)
			for (String attName : aggEventAttNames) {
				int idx = 0;
				for (; idx < aggData.size(); idx++) {
					String name = attName + " : " + actName;
					int i = idx + 1;
					if (idx != 0)
						name = name + "_" + i;
					instance.put(name, aggData.get(idx).getAttValue(attName + " : " + actName, event, getOffset(params.getTimeLines().get(0))));
				}
			}
		// extract the values of the normal attributes
		if (eventAttNames != null) {
			NormalAttributes normalAtts = new NormalAttributes(params);
			for (String attName : eventAttNames) 
				instance.put(attName + " : " + actName, normalAtts.getValue(trace, event, attName));
		}		
	}
	
/**	public void extractProcessLevelData() {
		tabularData = new LinkedList<Map<String, Object>>();
		
		long startTime = params.getAggData().getMinTimestamp().getTime() + params.getTimeSpane();
		long endTime = params.getAggData().getMaxTimestamp().getTime();
		for (long time = startTime; time <= endTime; time += params.getWindow()) {
			Map<String, Object> instance = new HashMap<>();
			if (params.getTraceAttsPL() != null)
				for (String attName : params.getTraceAttsPL()) {
					instance.put(attName, params.getAggData().getAttValue(attName, time));
					long pastTime = time - params.getTimeSpane();
					instance.put(attName + "__Past", params.getAggDataPast().getAttValue(attName, pastTime));
			}
			
			if (params.getEventAttsPL() != null && params.getActivitiesToConsciderPL() != null)
				for (String attName : params.getEventAttsPL()) {
					for (String actName : params.getActivitiesToConsciderPL()) {
						String name = attName + " : " + actName;
						instance.put(name, params.getAggData().getAttValue(name, time));
						long pastTime = time - params.getTimeSpane();
						instance.put(name + "__Past", params.getAggDataPast().getAttValue(name, pastTime));
					}
			}
			
			if (params.getResorceAttsPL() != null && params.getReourcesToConsiderPL() != null)
				for (String attName : params.getResorceAttsPL()) {
					for (String resourceName : params.getReourcesToConsiderPL()) {
						String name = attName + " : " + resourceName;
						instance.put(name, params.getAggData().getAttValue(name, time));
						long pastTime = time - params.getTimeSpane();
						instance.put(name + "__Past", params.getAggDataPast().getAttValue(name, pastTime));
					}
			}
			tabularData.add(instance);
		}
		
		if (!tabularData.isEmpty()) 
			for (Map<String, Object> instance : tabularData) 
				doUpdate(instance);
		
		createTable();
		
		System.out.println("table is creates!");
	} */
	
	
	public void setSituationType(SituationType s) {
		situationType = s;
	}
	
	public void setTargetAttName(String name) {
		targetAttName = name;
	}
	
	public LinkedList<Map<String, Object>> getTabularData() {
		return tabularData;
	}
	
	public void setWindow(long w) {
		window = w;
	}
	
	public void setActivitiesToConsider(Set<String> actNames) {
		activitiesToConsider = actNames;
	}

	public void setActivitiesToConsider(Collection<String> collection) {
		activitiesToConsider = new HashSet<String>();
		
		for (String actName : collection)
			activitiesToConsider.add(actName);
	}
	
	public Map<String, Object[]> getMinMax() {
		return minMax;
	}
	
	public Map<String, Type> getAttTypes() {
		return attTypes;
	}
	
	public Map<String, Map<Integer, String>> getInverseAttValueMap() {
		return tbch.getInverseNumericalValueMap();
	}
	
	public SituationFeatureTableVisualizer getTableExample() {
		return te;
	}
	
	void setAggEventAttNames(Set<String> names) {
		aggEventAttNames = names;
	}
	
	public void writeTableTofile() {
		te.writeTableToFile();
	}

	void setAggTraceAttNames(Set<String> names) {
		aggTraceAttNames = names;
	}

	void setEventAttNames(Set<String> names) {
		eventAttNames = names;
	}

	void setTraceAttNames(Set<String> names) {
		traceAttNames = names;
	}
	
	public boolean dataHasMissingValues() {
		return hasMissingValue;
	}
	
	public void setEventGroupre(EventGrouper g) {
		eventGroup = g;
	}
	
	public Map<String, Set<String>> getLiteralValues() {
		return literalValues;
	}
	
	public JScrollPane getTablePanel() {
		return tablePanel;
	}

	
	private static Type generateDataElement(Object value) {

		if (value instanceof Boolean) {
			return Type.BOOLEAN;
		} else if (value instanceof Long || value instanceof Integer) {
			return Type.DISCRETE;
		} else if (value instanceof Double || value instanceof Float) {
			return Type.CONTINUOS;
		} else if (value instanceof Date) {
			return Type.TIMESTAMP;
		} else if (value instanceof String) {
			return Type.LITERAL;
		}
		
		return null;	
	}
	
	/**
	 * This method creates the table using the instancesOFNDC.The table is saved on a file to be used by tetrad.	
	 */
	public void createTable() {		
		try {
			tbch = new TableCreatorOneHot(classAttributeName(), attTypes, literalValues, tabularData, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tbch.intermadiateNumericalFile();
		
		te = new SituationFeatureTableVisualizer(tbch.getHeader(), tbch.getTableBody(), tbch.getInverseNumericalValueMap(), replaceNotAllowedStrings(classAttributeName()), attTypes);
		
		tablePanel = te.getTableJScrollPane();
	}
	
	public void createTrainData() {	
		
		divideToTestAndTrain(0.3);
		try {
			tbch = new TableCreatorOneHot(classAttributeName(), attTypes, literalValues, trainData, this);
		} catch (Exception e) {
			System.out.println("CreateTrainData has failed!----");
			e.printStackTrace();
		}
		tbch.intermadiateNumericalFile();
	}
	
	/**
	 * 
	 * @return The class attribute name.
	 */
	public String classAttributeName() {

		if (situationType.equals(SituationType.ES))
			return targetAttName + " : " + eventGroup.getGrouperAttname();

		return targetAttName;

	}
	
	public void setDataHasMissingValues(boolean b) {
		hasMissingValue = b;
	}
	
	public boolean getTableIsCreated() {
		if (tabularData == null || tabularData.isEmpty())
			return false;

		return true;
	} 
	
	public void setNullValueThresholdInAColumn(int t) {
		tbch.setNullValueThresholdInAColumn(t);
	}
	
	public void setRemoveNullValues(boolean b) {
		tbch.setRemoveNullValues(b);
	}
	
	public void setInterpolationMethod(String method) {
   		tbch.setInterpolationMethod(method);
   	}
	
	public void setNullValueThresholdInARow(int t) {
		tbch.setNullValueThresholdInARow(t);
	}
	
	public Object[][] getNumericalTableBody() {
		return tbch.getNumericalBody();
	}
	
	public String[] getTableHeader() {
		return tbch.getHeader();
	}
	
	public void rewriteTheFile() {
		tbch.intermadiateNumericalFile();
	}
	
	public Map<String, Double> getMean() {
		return tbch.getMean();
	}
	
	public Map<String, Double> getMedian() {
		return tbch.getMedian();
	}
	
	public Map<String, Double> getVariance() {
		return tbch.getVariance();
	}
	
	public Map<String, Double> getStdDev() {
		return tbch.getStdDev();
	}
	
	public void LimitDataTable(Set<String> attNames) {
		tbch.limitDataTable(attNames);
		
	}
	
	public Parameters getParametes() {
		return params;
	}
	
	
	public LinkedList<Map<String, Object>> getTestData() {
		return testData;
	}
	
	public LinkedList<Map<String, Object>> getTrainData() {
		return trainData;
	}
	
	/**
	 * Divide the attNames into EventAttNames and TraceAttNames.
	 * @param attNames

	public void setAttNames(Set<String> attNames) {
		traceAttNames = new HashSet<>();
		eventAttNames = new HashSet<>();
		
		for (String attName : attNames) {
			if (aggData.getAttNames().contains(attName)) {
				if (aggData.isTraceAttName(attName))
					traceAttNames.add(attName);
				else
					eventAttNames.add(attName);
			} else 
				System.out.println("--------- " + attName + " is not and aggregate attribute! ---------");
		}
	} */
	
	
	// ------------------------------------ Test Code ------------------------------
	
	public static void main(String[] arfs) {
		
		AggregateAttributes a = new AggregateAttributes();
		Parameters p = new Parameters(a.setupEventLog());	
		p.setSituationType(SituationType.TS);
		
		Set<String> traceAtts = new HashSet<>();
		traceAtts.add("Process workLoad");
		traceAtts.add("Average service time Trace");
		traceAtts.add("Process number waiting");
		p.setSelectedAggTraceAttNames(traceAtts);
		
		Set<String> eventAtts = new HashSet<>();
		eventAtts.add("Average service time Resource");
		eventAtts.add("Number of active Event");
		p.setAggEventAttNames(eventAtts);
		
		Set<String> actNames = new HashSet<String>();
		actNames.add("A");
		p.setActivitiesToConsider(actNames);
		
		p.setClassAttName("Process number waiting");
		
//		p.setWindow(43200000);   // 12 h
		
		AggregatedDataExtraction ade = new AggregatedDataExtraction(p);		
		ade.extractData();
		
		System.out.println(ade.getTabularData().toString());
	}

	public void extractDataPL() {
		// TODO Auto-generated method stub
		
	}

	public void trimTheData(Set<String> recmmendedFeatures) {
		
		LinkedList<Map<String, Object>> newTabularData = new LinkedList<>();
		
		if (!tabularData.isEmpty()) 
			for (Map<String, Object> instance : tabularData) 
				newTabularData.add(trim(instance, recmmendedFeatures));
		
		tabularData = newTabularData;
		
		Map<String, Type> types = new HashMap<>();
		for (String attName : attTypes.keySet()) 
			if (!attName.equals(replaceNotAllowedStrings(targetAttName)) && !recmmendedFeatures.contains(attName)) 
				literalValues.remove(attName);
			else
				types.put(attName, attTypes.get(attName));
		
		attTypes = types;
		
		createTable();
	}

	private Map<String, Object> trim(Map<String, Object> instance, Set<String> recmmendedFeatures) {
		Map<String, Object> newInstance = new HashMap<>();
		for (String attName : attTypes.keySet()) 
			if (attName.equals(replaceNotAllowedStrings(targetAttName)) || recmmendedFeatures.contains(attName)) 
				if (instance.containsKey(attName))
					newInstance.put(attName, instance.get(attName));
		
		return newInstance;
	}

}

