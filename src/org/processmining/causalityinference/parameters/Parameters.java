package org.processmining.causalityinference.parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.processmining.dataTable.AggregateAttributes;
import org.processmining.dataTable.AggregatedDataExtraction;
import org.processmining.dataTable.ORplaces;
import org.processmining.dataTable.Augmentation.DurationOfActivity;
import org.processmining.dataTable.Augmentation.sub_model_duration;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class Parameters {
	private XLog log;
	private Petrinet model;
	private PNRepResult res;
	private PluginContext context;
	private AggregatedDataExtraction tabularDataCreator;
	private LinkedList<Object[]> timeLineParams = new LinkedList<>();
	
	public final static String notAllowedChars=".()&!|=<>-+*/% ";
	
	/**
	 * The time window for the aggregated attributes.
	 */
	private long window = 100000;
	
	/**
	 * The map including all the aggregated values for all the attribute
	 * names and all the time windows. (used for the current time attributes)
	 */
	private LinkedList<AggregateAttributes> aggData;
	
	/**
	 * The type of the situation. 
	 * 	- Trace situation
	 * 	- Event situation
	 * 	- Choice situation
	 */
	private SituationType situationType = SituationType.TS;
	
	/**
	 * The set of aggregated trace attribute names. In case of trace situation, includes
	 * target attribute name also.
	 */	
	private Set<String> selectedAggTraceAttNames; 

	/**
	 * The set of aggregated event attribute names. In case of event or choice situation, 
	 * also may include target attribute name.
	*/ 
	private Set<String> selectedAggEventAttNames; 
	
	/**
	 * The set of trace attribute names (normal attributes). In case of trace situation, includes
	 * target attribute name also.
	 */	
	private Set<String> selectedNoramlTraceAttNames; 

	/**
	 * The set of event attribute names (normal attributes). In case of event or choice situation, 
	 * also may include target attribute name.
	*/ 
	private Set<String> selectedNormalEventAttNames; 
	
	/**
	 * The set of activity names of the events that their attribute values have
	 * to be extracted.
	 */
	private Set<String> activitiesToConsider;
	
	/**
	 * If trace delay is selected as class or independent attribute, what the 
	 * threshold is.
	 */
	private long traceDelayThreshold;
	
	/**
	 * If the sub-model-duration attribute is selected, includes the transitions in the selected 
	 * sub-model.
	 */
	private Set<Transition> sub_model;
	
	/**
	 * if independent attributes includes SubModel attribute.
	 */
	private boolean indepSubmodel = false;
	
	/**
	 * if independent attributes includes choice attribute.
	 */
	private boolean indepChoice = false;
	
	/**
	 * If some of the choices has been selected as class or independent attributes.
	 */
	private Set<Place> selectedORplaces = new HashSet<Place>();
	
	/**
	 * if it is needed to remove the null values? //TODO remove when upgrading to the higher version of Tetrad
	 */
	private boolean removeNullValues = false;
	
	/**
	 * <attName, values> for the categorical features in the data table.
	 */
	private Map<String, Set<String>> literalValuesTable;
	
	// -------------- process level properties ----------------------------
	
	/**
	 * The time unit that is used for the duration calculation and 
	 * also time-window and time-difference.
	 * possible values:
	 * Millisecond, second, minute, hour, day, week
	 */
	private TimeUnit timeUnit = TimeUnit.D;
	// ------------- class situation feature properties ------------------------
	
	/**
	 * The activity name of the event that contains the target
	 * attribute (in case of event situation).
	 */
	private String targetAttName;
	
	// ------------- Event situation , event grouping info -------------
	
	/**
	 *  Identify which attribute is used for grouping the events.
	 */
	private ActivityGrouperAttName grouperAttName; 
	
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
	
	//------------------------ Process Level -----------------------------

	Set<String> traceAttsPL;

	Set<String> eventAttsPL;
	
	Set<String> resourceAttsPL;
	
	Set<String> activitiesToConsciderPL;
	
	Set<String> reourcesToConsiderPL;
	// ---------------------------- event log info ---------------------------

	private Set<String> traceAttributeNames;
	private long minTraceDuration = 0;
	private long maxTraceDuration = 0;
	private ArrayList<String> allEventAttNames=new ArrayList<String>();
	private ArrayList<String> allTraceAttNames=new ArrayList<String>();
	private final ArrayList<String> allActNames = new ArrayList<String>();
	private Map<String, Set<String>> literalValuesLog;
	private long minActDuration;
	private long maxActDuration;
	// --------------------properties of tabular data -----------------
	
	/**
	 * Include the min and max value of the discrete, continues, and timestamp 
	 * attributes in the tabular data.
	 */
	private Map<String, Object[]> minMax = new HashMap<String, Object[]>();
	
	/**
	 * Indicate the type of each attribute.
	 */
	private Map<String, Type> attTypes = new HashMap<String, Type>();
	
	/**
	 * A map from boolean and categorical attribute names to a set including all of their values;
	 */
	private Map<String, Set<String>> literalValuesNDC = new HashMap<String, Set<String>>();
	
	// ---------------------- Feature recommendation ---------------------
	
	/**
	 * The feature selection method, random forest, information gain, correlation
	 */
	private FeatureSelectionMethod methodFS = FeatureSelectionMethod.RF;
	
	/**
	 * Number of bins when the attribute in numerical.
	 */
	private int numBinsFR;
	
	/**
	 * In case of nominal target feature, it is the set of desirable
	 * values.
	 */
	private Set<String> undesirableValuesFR;
	
	/**
	 * In case of numerical target feature, it is the threshold for
	 * desirable values.
	 */
	private double thresholdFR;
	
	/**
	 * In case of numerical target feature, shows if lower or 
	 * higher than threshold is desirable.
	 */
	private boolean isLowerUnDesirableFR;
	
	/**
	 * The information gain threshold when using feature recommendation 
	 * just based of information gain (OM method)
	 */
	private double infoGainThreshold;
	// --------------------- Limit graph -----------------------
	
	int depth=1;

	// --------------------- Feature recommendation functions ----------------
	
	public void setInfoGainThreshold(Double d) {
		infoGainThreshold = d;
	}
	
	public double getInfoGainThreshold() {
		return infoGainThreshold;
	}
	
	public void setFeatureSelectionMethod(FeatureSelectionMethod featureSelectionMethod) {
		methodFS = featureSelectionMethod;
	}
	
	public FeatureSelectionMethod getFeatureSelectionMethod() {
		return methodFS;
	}
	
	public int getNumBinsFR() {
		return numBinsFR;
	}
	
	public void setNumBinsFR(int num) {
		numBinsFR = num;
	}
	
	public Set<String> getUndesirableValuesFR() {
		return undesirableValuesFR;
	}
	
	public void setUndesirableValuesFR(Set<String> values) {
		undesirableValuesFR = values;
	}
	
	public double getThresholdFR() {
		return thresholdFR;
	}
	
	public void setThresholdFR(double tr) {
		thresholdFR = tr;
	}
	
	public boolean getIsLowerUnDesirableFR() {
		return isLowerUnDesirableFR;
	}
	
	public void setIsLowerUnDesirableFR(boolean b) {
		isLowerUnDesirableFR = b;
	}
	
	public void setIndepSubmodel(boolean b) {
		indepSubmodel = b;
	}
	
	public boolean getIndepSubmodel() {
		return indepSubmodel;
	}
	
	public void setIndepChoice(boolean b) {
		indepChoice = b;
	}
	
	public boolean getIndepChoice() {
		return indepChoice;
	}
	
	//----------------------- Finding the best DAG ------------------------------------
	
	// TODO add the needed parameters like threshold
	
	public LinkedList<Map<String, Object>> getTestData() {
		return tabularDataCreator.getTestData();
	}
	
	public LinkedList<Map<String, Object>> getTrainData() {
		return tabularDataCreator.getTrainData();
	}
	//-----------------------These info are needed for the experimental results-------------------------------
	
	int numCol; // number of columns in the data used for inferring the causal structure
	int numRow; // number of rows in the data used for inferring the causal structure
	
	public void setNumCol(int n) {
		numCol = n;
	}
	
	public int getNumCol() {
		return numCol;
	}
	public void setNumRow(int n) {
		numRow = n;
	}
	
	public int getNumRow() {
		return numRow;
	}
	// -----------------------------------------------------------------------
	
	public Parameters(XLog log, Petrinet model, PNRepResult res) {
		this.log = log;
		this.model = model;
		this.res = res;
	}
	
	public Parameters(XLog log) {
		this.log = log;
	}
	
	public Parameters() {
		
	}
	
	public void setTraceAttNames(Object[] selectedTraceAttributes) {
		selectedAggTraceAttNames = new HashSet<String>();
		if (selectedTraceAttributes != null)
			for (int i = 0; i < selectedTraceAttributes.length; i++) 
				selectedAggTraceAttNames.add((String) selectedTraceAttributes[i]);
	}

	public void setEventAttNames(Object[] selectedEventAttributes) {
		selectedAggEventAttNames = new HashSet<String>();
		
		if (selectedEventAttributes != null)
			for (int i = 0; i < selectedEventAttributes.length; i++) 
				selectedAggEventAttNames.add((String) selectedEventAttributes[i]);
	}
	
	public void setTraceDelayThreshold(double d) {
		XTrace trace = log.get(0);
		long min = wholeTraceDuration(trace);
		long max = wholeTraceDuration(trace);
		long avg = 0;
		for (XTrace t : log) {
			long temp = wholeTraceDuration(t);
			avg = avg + temp;
			if (temp < min) {
				min = temp;
			}
			if (temp > max) {
				max = temp;
			}
		}
		System.out.println(" min : " + min);
		System.out.println(" max : " + max);
		System.out.println(" avg : " + avg/log.size());
		this.traceDelayThreshold = Math.round(((max - min) * 0.01));
		System.out.println(" Delay Threshold : " + this.traceDelayThreshold);
	}
	
	
	public void setSelectetSub_model (Set<Transition> selectedTransitions) {
		this.sub_model = new HashSet<Transition>();
		this.sub_model.addAll(selectedTransitions);
		addSubModelDurations();
	}
	
	private void addSubModelDurations() {
		if (sub_model != null) {
			sub_model_duration smd = new sub_model_duration(sub_model, log, model, res);
			Map<Integer, Long> smdValues = smd.sub_modelDurations();
			for (Integer traceIdx : smdValues.keySet()) {
				XTrace trace = log.get(traceIdx);
				XAttributeMap amap = trace.getAttributes();
				XAttributeContinuousImpl nvalue = new XAttributeContinuousImpl("Sub model duration", smdValues.get(traceIdx));
				if (amap.containsKey("Sub model duration")) {
					amap.remove("Sub model duration");
				}
				amap.put("Sub model duration", nvalue);
			}
		}
	}

	public void setSelectedORplaces (Set<Place> selectedORPlaces) {
		this.selectedORplaces = new HashSet<Place>();
		this.selectedORplaces.addAll(selectedORPlaces);
		
	}
	
	public Set<Place> getSelectedORplace () {
		return selectedORplaces;
	}
	
	public long getTraceDelayThreshold() {
		return traceDelayThreshold;
	}

	public XLog getLog() {
		return log;
	}
	
//	public void setWindow(long w) {
//		window = w;
//		setAggData(false);
//	}
	
//	public void setWindow(String value) {
//		window = getWindowSize(value);
//		setAggData(false);
//	}
	
	public long getWindow() {
		return window;
	}
	
	public ArrayList<String> getAllTraceAttNames() {
		ArrayList<String> list = new ArrayList<String>();
		for (String attname : traceAttributeNames)
			list.add(attname);
		
		return list;
	}
	
	public void setSituationType(SituationType st) {
		situationType = st;
	}
	
	public String classAttributeName() {
		return replaceNotAllowedStrings(classAttributeNameNoReplacement());
	}
	
	public String classAttributeNameNoReplacement() {
		if (situationType.equals(SituationType.ES))
			return targetAttName + " : " + grouperAttName;
		else
			return targetAttName;
		}
	
	/**
	 * removes the not allowed char for the consistency
	 */
   	public String replaceNotAllowedStrings(String str) {
   		if (str == null)
   			return null;
   		
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}
	
	public SituationType getSituationType() {
		return situationType;
	}

	public Set<String> getSelectedNormalTraceAttNames() {
		
		if (indepSubmodel == true) 
			if (selectedNoramlTraceAttNames == null || selectedNoramlTraceAttNames.isEmpty()) {
				selectedNoramlTraceAttNames = new HashSet<>();
				selectedNoramlTraceAttNames.add("Sub Model Attribute");
			}

		if (selectedNoramlTraceAttNames == null || selectedNoramlTraceAttNames.isEmpty())
			return null;
		
		return selectedNoramlTraceAttNames;
	}
	public void setSelectedNormalTraceAttNames(Set<String> set) {
		selectedNoramlTraceAttNames = set;
	}
	
	public void setSelectedNormalTraceAttNames(Collection<String> names) {
		if (names == null)
			selectedNoramlTraceAttNames = null;
		else {
			selectedNoramlTraceAttNames = new HashSet<>();
			for (String name : names)
				selectedNoramlTraceAttNames.add(name);
		}
	}
	
	public Set<String> getSelectedAggTraceAttNames() {
		return selectedAggTraceAttNames;
	}
	
	public void setSelectedAggTraceAttNames(Set<String> set) {
		selectedAggTraceAttNames = set;
	}
	
	public void setSelectedAggTraceAttNames(Collection<String> names) {
		if (names == null)
			selectedAggTraceAttNames = null;
		else {
			selectedAggTraceAttNames = new HashSet<>();
			for (String name : names)
				selectedAggTraceAttNames.add(name);
		}
	}
	
	public void setSelectedNormalEventAttNames(Set<String> set) {
		selectedNormalEventAttNames = set;
	}
	
	public Set<String> getEventAttNames() {
		return selectedNormalEventAttNames;
	}
	
	public void setSelectedNormalEventAttNames(Collection<String> names) {
		if (names == null)
			selectedNormalEventAttNames = null;
		else {
			selectedNormalEventAttNames = new HashSet<>();
			for (String name : names)
				selectedNormalEventAttNames.add(name);
		}
	}
	
	public Set<String> getSelectedAggEventAttNames() {
		return selectedAggEventAttNames;
	}
	
	public void setAggEventAttNames(Set<String> set) {
		selectedAggEventAttNames = set;
	}
	
	public void setSelectedAggEventAttNames(Collection<String> names) {
		if (names == null)
			selectedAggEventAttNames = null;
		else {
			selectedAggEventAttNames = new HashSet<>();
			for (String name : names)
				selectedAggEventAttNames.add(name);
		}
	}
	
	public void setActivitiesToConsider(Set<String> set) {
		activitiesToConsider = set;
	}
	
	public void setActivitiesToConsider(Collection<String> names) {
		if (names == null)
			activitiesToConsider = null;
		else {
			activitiesToConsider = new HashSet<>();
			for (String name : names)
				activitiesToConsider.add(name);
		}
	}
	
	public Set<String> getActivitiesToConsider() {
		return activitiesToConsider;
	}
	
	public void setClassAttName(String name) {
		targetAttName = name;
	}

	public String getClassAttName() {
		return targetAttName;
	}
	
	public String getClassAttNameReplaced() {
		return replaceNotAllowedStrings(targetAttName);
	}
	
	public void setGrouperAttName (ActivityGrouperAttName attName) {
		grouperAttName = attName;
	}
	
	public ActivityGrouperAttName getGrouperAttName() {
		return grouperAttName;
	}
	
	public void setGrouperAttValues(Set<String> set) {
		values = set;
	}
	public Set<String> getGrouperAttValues() {
		// in choice situation we need to fine events that contain the target choice attribute
		if (situationType.equals(SituationType.CS)) {
			Set<String> v = new HashSet<>();
			v.add(targetAttName);
			return v;
		}
			
		return values;
	}
	
	public void setMinThreshold(String threshold) {
		minThreshold = threshold;
	}
	
	public String getMinThreshold() {
		return minThreshold;
	}
	
	public void setMaxThreshold(String threshold) {
		maxThreshold = threshold;
	}
	
	public String getMaxThreshold() {
		return maxThreshold;
	}
	
	public ArrayList<String> getAllActivityNames() {
		return allActNames;
	}
	
	public ArrayList<String> getAggAttNames() {
		ArrayList<String> aggAttNames = new ArrayList<String>();
		aggAttNames.add("Average service time Trace");
		aggAttNames.add("Average waiting time Trace");
		aggAttNames.add("Process workLoad");
		aggAttNames.add("Process number waiting");
		aggAttNames.add("Number of active Event");
		aggAttNames.add("Number of waiting Event");
		aggAttNames.add("Resource workLoad");
		
		return aggAttNames;
	}
	
	public ArrayList<String> getTraceAggAttNames() {
		ArrayList<String> aggAttNames = new ArrayList<String>();
		aggAttNames.add("Average service time Trace");
		aggAttNames.add("Average waiting time Trace");
		aggAttNames.add("Process workLoad");
		aggAttNames.add("Process number waiting");
		
		return aggAttNames;
	}
	
	public String[] getProcessLevelAggAttNames() {
		String[] attNames = {"Process workLoad", "Process number waiting"};
		return attNames;
	}
	
	public String[] getTraceLevelAggAttNames() {
		String[] attNames = {"Average service time Trace", "Average waiting time Trace"};
		return attNames;
	}
	
	public String[] getEventLevelAggAttNames() {
		String[] attNames = {"Number of active Event", "Number of waiting Event" };
		return attNames;
	}

	public String[] getResourceLevelAggAttNames() {
		String[] attNames = {"Resource workLoad"};
		return attNames;
	}
	
	public Object[] getAggAttNamesArray() {
		ArrayList<String> list = getAggAttNames();
		Object[] array = new Object[list.size()];
		int i = 0;
		for (String attName : list) {
			array[i] = attName;
			i++;
		}
		
		return array;
	}
	
	public Type getAggAttType(String attName) {
	/**	ArrayList<String> aggAttNames = new ArrayList<String>();
		aggAttNames.add("Average service time Trace");
		aggAttNames.add("Average waiting time Trace");
		aggAttNames.add("Average service time Event");
		aggAttNames.add("Average waiting time Event");
		
		if (aggAttNames.contains(attName))
			return "Long";
		else
			return "Integer"; */
		return Type.DISCRETE;
	}
	
	public Set<String> getSetOfAggAttNames() {
		Set<String> aggAttNames = new HashSet<String>();
		aggAttNames.add("Average service time Trace");
		aggAttNames.add("Average waiting time Trace");
		aggAttNames.add("Process workLoad");
		aggAttNames.add("Process number waiting");
		aggAttNames.add("Number of active Event");
		aggAttNames.add("Number of waiting Event");
		aggAttNames.add("Resource workLoad");
		
		return aggAttNames;
	}
	
	
//	public void setAggData(Boolean past) {
//			aggData = new AggregateAttributes(this, past, getTiemUnitMillisec());
//			aggData.setValues();
//	}
	
	public LinkedList<AggregateAttributes> getAggData() {
		return aggData;
	}
	
	public long getTiemUnitMillisec(TimeUnit timeUnit) {
		if (timeUnit.equals(TimeUnit.MS))
			return  1;
		else if (timeUnit.equals(TimeUnit.Sec))
			return 1 * 1000;
		else if (timeUnit.equals(TimeUnit.Min))
			return 1 * 1000 * 60;
		else if (timeUnit.equals(TimeUnit.H))
			return 1 * 1000 * 60 * 60;
		else if (timeUnit.equals(TimeUnit.W))
			return 1 * 1000 * 60 * 60 * 24 * 7;
		else 
			return 1 * 1000 * 60 * 60 * 24;	
	}
	
	public void init()
	{	
		// Adding the first trace attributes to the traceAttributeNames.
		// Here we assume that all the traces has the same set of attributes.
		gatherTraceAttributeNames();
		
		if (res != null)   // adding the choice information and the duration of each action as attributes to the events
			enrichTheLog(); //TODO removed just for the covid example. return it later!
		
		minTraceDuration = wholeTraceDuration(log.get(0));
		//<--
		HashSet<String> tempAttributeSet = new HashSet<String>();
		HashSet<String> tempActivitySet = new HashSet<String>();
		
		Set<String> traceAttNames = new HashSet<>();
		for (XTrace trace : log)
		{	
			// collect trace attribute names
			for(String attr : trace.getAttributes().keySet())
				if (!attr.startsWith("concept:"))
					traceAttNames.add(attr);
			
			for(XEvent event : trace)
			{
				tempActivitySet.add(XConceptExtension.instance().extractName(event));
				for(String attr : event.getAttributes().keySet()) // gathers all the attributes in the event except those that are mentioned
					if (!attr.startsWith("concept:") && !attr.startsWith("time:") && !attr.startsWith("resource:") 
							&& !attr.startsWith("org:") && !attr.startsWith("role:") 
							&& !attr.startsWith("Choice_") && !attr.startsWith("lifecycle") 
							&& !attr.startsWith("situation"))
						tempAttributeSet.add(attr);	
			}
			long duration = wholeTraceDuration(trace);
			if (duration < minTraceDuration) 
				minTraceDuration = duration;
			if (duration > maxTraceDuration) 
				maxTraceDuration = duration;
		}
		
		for (String attName : traceAttNames)
			allTraceAttNames.add(attName);
		allEventAttNames.addAll(tempAttributeSet); // collect all the event attribute names in the log except those
		// that start with "concept:", "time:", "resource:" and "org:"
		Collections.sort(allEventAttNames);
		Collections.sort(allTraceAttNames);
		allActNames.addAll(tempActivitySet); // collect all the event names in the log
		Collections.sort(this.allActNames);
		
		literalValuesLog = getLiteralValuesMap(log);
		
		XTrace trace = log.get(0);
		for (XEvent event : trace) {
			XAttributeMap map = event.getAttributes();
			for (String att : map.keySet())
				System.out.println(att);
		}
		System.out.println(" end of init();");		
	}
	
	/**
	 * Gathering all the trace attribute names.
	 * 
	 * traceAttributeNames = first trace attribute names + drieven attribute names
	 */
	public void gatherTraceAttributeNames() {
		traceAttributeNames = new HashSet<String>();
		
		XTrace firstTrace = log.get(0);
		for(String attr : firstTrace.getAttributes().keySet()) // gathers all the attributes in the event except those that are mentioned
		{
			if (!attr.startsWith("concept:") && !attr.startsWith("lifecycle:") && !attr.startsWith("time:"))
			{
				traceAttributeNames.add(attr);
			}					
		}
		
//		traceAttributeNames.add("Sub Model Attribute");
		traceAttributeNames.add("Trace Duration");
		traceAttributeNames.add("Trace Delay");
		if (res != null) {
//			traceAttributeNames.add("Choice Attribute");
			traceAttributeNames.add("deviation");
			traceAttributeNames.add("number modelMove");
			traceAttributeNames.add("number logMove");
		}
	}
	
	/**
	 * Adding driven attributes to traces in the log and events, including
	 * choice attribute, deviation, numLogMove, numModelMove, eventDuration
	 */
	public void enrichTheLog() {
		ORplaces orp = new ORplaces( model);
		DurationOfActivity ad = new DurationOfActivity( model);
		for (SyncReplayResult singleVariantReplay : res) {
			Set<Integer> allTraceIdxOfThisVariant = singleVariantReplay.getTraceIndex();
			for (Integer traceIdx : allTraceIdxOfThisVariant) {
				orp.enrichTraceWithORChoices(log.get(traceIdx), singleVariantReplay, traceIdx);
				ad.setActivityDuration(log.get(traceIdx), singleVariantReplay);
			}
		}
		
		minActDuration = ad.getMinAllDurations();
		maxActDuration = ad.getMaxAllDurations();
	}
	
	public long wholeTraceDuration(XTrace trace) {
  		XEvent firstEvent = trace.get(0);
		XEvent lastEvent = trace.get(trace.size()-1);
		Date timestampE1=XTimeExtension.instance().extractTimestamp(firstEvent);
		Date timestampE2=XTimeExtension.instance().extractTimestamp(lastEvent);
		return timestampE2.getTime()-timestampE1.getTime();
  	}
	
	/**
	 * it creates a map of the form <String, Set<Strings>>
	 * the key is the name of literal attributes in the log
	 * the value is the set of all possible values for the key attribute in the log
	 * @param log
	 * @return a map of attNames, set of its values for the literal attributes
	 */
	
	// 
	// the key is the name of literal attributes in the log
	// the value is the set of all possible values for the key attribute in the log
	private static Map<String, Set<String>> getLiteralValuesMap(XLog log) {
		
		Map<String, Set<String>> retValue=new HashMap<String, Set<String>>();
		
		for(XTrace trace : log) {
			
			
			for(XEvent event : trace) {
				
				for(XAttribute attributeEntry : event.getAttributes().values()) {
					
					if (attributeEntry instanceof XAttributeLiteral) {
						
						String value = ((XAttributeLiteral)attributeEntry).getValue();
						String varName=attributeEntry.getKey();
						Set<String> literalValues = retValue.get(varName);

						if (literalValues == null) {
							literalValues = new HashSet<String>();
							retValue.put(varName, literalValues);
						}
						
						literalValues.add(value);
					}
				}
			}
		}
		return retValue;
	}

	public ArrayList<String> getAllEventAttNames() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("Activity Duration");
		list.add("Elapsed Time");
		list.add("Remaining Time");
		list.add("Timestamp");
		list.add("Executor_Group");
		list.add("Resource");
		list.add("Next Activity");// TODO add this att
		list.add("Previous Activity");// TODO add this att
		list.add("Number of execution"); // TODO add this att
		
		for (String attName : allEventAttNames)
			if (!attName.equals("activityduratin"))
				list.add(attName);
		
		return list;
	}
	
	/**
	 * @return an array of aggregated and normal trace attNames
	 */
	public String[] getAllAggAndNormalTraceAttNames() {
		String[] attNames = new String[getAggAttNames().size() + traceAttributeNames.size()];
		int i = 0;
		for (String attName : getAggAttNames()) {
			attNames[i] = attName;
			i++;
		}
		
		for (String attName : traceAttributeNames) {
			attNames[i] = attName;
			i++;
		}
		return attNames;
	}
	
	/**
	 * @return an array of aggregated and normal event attNames
	 */
	public String[] getAllAggAndNormalEventAttNames() {
		ArrayList<String> list = getAllEventAttNames();
		String[] attNames = new String[getAggAttNames().size() + list.size()];
		int i = 0;
		for (String attName : getAggAttNames()) {
			attNames[i] = attName;
			i++;
		}
		
		for (String attName : list) {
			attNames[i] = attName;
			i++;
		}
		return attNames;
	}

	public Set<String> getSelectedNormalEventAttNames() {
		if (indepChoice == true && selectedORplaces != null && selectedORplaces.size() > 0) 
			if (selectedNormalEventAttNames == null || selectedNormalEventAttNames.isEmpty()) {
				selectedNormalEventAttNames = new HashSet<>();
				for (Place p : selectedORplaces) 
					selectedNormalEventAttNames.add(p.getLabel());
			}
		
		if (selectedNormalEventAttNames == null || selectedNormalEventAttNames.isEmpty())
			return null;
		
		return selectedNormalEventAttNames;
	}

	public void setAttTypes(Map<String, Type> attTypes) {
		this.attTypes = attTypes;
	}
	
	public Map<String, Type> getAttTypes() {
		return attTypes;
	}
	
	public Object[] getMinMax(String attName) {
		return minMax.get(attName);
	}

	public Map<String, Object[]> getMinMax() {
		return minMax;
	}
	
	public void setMinMax(Map<String, Object[]> map) {
		minMax = map;
	}
	
	public void setLiteralValuesTable(Map<String, Set<String>> map) {
		literalValuesTable = map;
	}
	
	public Map<String, Set<String>> getLiteralValuesTable() {
		return literalValuesTable;
	}

	public void setRemoveNullValues(boolean b) {
		removeNullValues = b;
	}

	public LinkedList<String> getAttributeNames() {
		
		LinkedList<String> list = new LinkedList<>();
		for (String attName : attTypes.keySet()) 
			list.add(attName);
		
		return list;
	}

	public PetrinetGraph getModel() {
		return model;
	}
	
	/**
	 * 
	 * @return A map of att name and a set of literal values for that att.
	 * This map is for the log not for the tabular data.
	 */
	public Map<String, Set<String>> getAllLiteralValues() {
		return literalValuesLog;
	}
	public long getMinAllActDuration() {
		return minActDuration;
	}
	
	public long getMaxAllActDuration() {
		return maxActDuration;
	}

	public void setGrouperAttValues(Collection<String> selectedValues) {
		values = new HashSet<String>();
		
		for (String attName : selectedValues)
			values.add(attName);
		
	}

/**	public void setTimeSpan(long value) {
		timeSpan = value;
	}
	
	public void setTimeSpan(String value) {
		timeSpan = getWindowSize(value);
	}
	
	public long getTimeSpane() {
		return timeSpan;
	}
	
	public void setWindowPast(long value) {
		windowPast = value;
		aggDataPast = new AggregateAttributes(this, true, getTiemUnitMillisec());
		aggDataPast.setValues();
	} 
	
	public void setWindowPast(String value) {
		windowPast = getWindowSize(value);
		aggDataPast = new AggregateAttributes(this, true, getTiemUnitMillisec());
		aggDataPast.setValues();
	} */
	
//	public AggregateAttributes getAggDataPast() {
//		return aggDataPast;
//	}
	
//	public long getWindowPast() {
//		return windowPast;
//	}

//	public void setIsPastIncluded(boolean b) {
//		isPastIncluded = b;	
//	}
	
//	public boolean isPastIncluded() {
//		return isPastIncluded;	
//	}	
	
	
	
	public long getWindowSize(String value, TimeUnit timeUnit) {
		Long unit = 1l;
		if (timeUnit.equals(TimeUnit.Sec))
			unit = 1000l;
		else if (timeUnit.equals(TimeUnit.Min))
			unit = (long) (1000 * 60);
		else if (timeUnit.equals(TimeUnit.H))
			unit = (long) (1000 * 60 * 60);
		else if (timeUnit.equals(TimeUnit.D))
			unit = (long) (1000 * 60 * 60 * 24);
		else if (timeUnit.equals(TimeUnit.W))
			unit = (long) (1000 * 60 * 60 * 24 * 7);
		
		return Math.round(Double.valueOf(value)*unit);
	} 
	
	public Petrinet getPetrinet() {
		return model;
	}

	public void setContext(PluginContext context) {
		this.context = context;
	}
	
	public PluginContext getContext() {
		return context;
	}

	public void setTimeUnit(TimeUnit tu) {
		timeUnit = tu;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setDataTableCreator(AggregatedDataExtraction tabularDataCreator) {
		this.tabularDataCreator = tabularDataCreator;
	}
	
	public AggregatedDataExtraction getDataTableCreator() {
		return tabularDataCreator;
	}

	public void setTimeLineSetting(LinkedList<Object[]> tlp) {
		timeLineParams = tlp;
		
		aggData = new LinkedList<AggregateAttributes>();
		for (Object[] timeLine : timeLineParams) 
			aggData.add(new AggregateAttributes(this, getTiemUnitMillisec((TimeUnit) timeLine[0]),
					getWindowSize(timeLine[1].toString(), (TimeUnit) timeLine[0]),
					getWindowSize(timeLine[2].toString(), (TimeUnit) timeLine[0])));
	}
	
	public LinkedList<Object[]> getTimeLines() {
		return timeLineParams;
	}

	public void trimTheData(Set<String> recmmendedFeatures) {
			
		Map<String, Type> types = new HashMap<>();
		for (String attName : attTypes.keySet()) {
			if (!attName.equals(replaceNotAllowedStrings(targetAttName)) && !recmmendedFeatures.contains(attName)) {
				minMax.remove(attName);
				literalValuesNDC.remove(attName);
				tabularDataCreator.trimTheData(recmmendedFeatures);
			} else
				types.put(attName, attTypes.get(attName));
		}
		
		attTypes = types;
	}
	
	// --------------Process level independent attribute setting----------------
/**	public Set<String> getProcessLevelAggAtts() {
		return processLevelAggAtts;
	}
	
	public void setProcessLevelAggAtts(Set<String> names) {
		if (names == null)
			processLevelAggAtts = null;
		else {
			processLevelAggAtts = new HashSet<>();
			for (String name : names)
				processLevelAggAtts.add(name);
		}
	} */
	
	
	public void setTraceAttsPL(Collection<String> names) {
		if (names == null)
			traceAttsPL = null;
		else {
			traceAttsPL = new HashSet<>();
			for (String name : names)
				traceAttsPL.add(name);
		}
	}
	
	public Set<String> getTraceAttsPL() {
		return traceAttsPL;
	}
	
	public void setEventAttsPL(Collection<String> names) {
		if (names == null)
			eventAttsPL = null;
		else {
			eventAttsPL = new HashSet<>();
			for (String name : names)
				eventAttsPL.add(name);
		}
	}
	
	public Set<String> getEventAttsPL() {
		return eventAttsPL;
	}
	
	public void setResourceAttsPL(Collection<String> names) {
		if (names == null)
			resourceAttsPL = null;
		else {
			resourceAttsPL = new HashSet<>();
			for (String name : names)
				resourceAttsPL.add(name);
		}
	}
	
	public Set<String> getResorceAttsPL() {
		return resourceAttsPL;
	}
	
	public void setActivitiesToConsciderPL(Collection<String> names) {
		if (names == null)
			activitiesToConsciderPL = null;
		else {
			activitiesToConsciderPL = new HashSet<>();
			for (String name : names)
				activitiesToConsciderPL.add(name);
		}
	}
	
	public Set<String> getActivitiesToConsciderPL() {
		return activitiesToConsciderPL;
	}
	
	public void setReourcesToConsiderPL(Collection<String> names) {
		if (names == null)
			reourcesToConsiderPL = null;
		else {
			reourcesToConsiderPL = new HashSet<>();
			for (String name : names)
				reourcesToConsiderPL.add(name);
		}
	}
	
	public Set<String> getReourcesToConsiderPL() {
		return reourcesToConsiderPL;
	}
	
	public void setDepth(int d) {
		depth = d;
	}
	
	public int getDepth() {
		return depth;
	}

	public void saveDataCSV() {
		this.tabularDataCreator.writeTableTofile();
	}
}
