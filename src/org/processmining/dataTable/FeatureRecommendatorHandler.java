package org.processmining.dataTable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.datadiscovery.estimators.Type;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import csplugins.id.mapping.ui.CheckComboBox;


class FeatureRecommendatorHandler implements ActionListener {
	
	Parameters params;
	
	SituationType situationType;
	
	String targetAttName;
	
	Collection<String> badValues;
	
	double threshold;
	
	boolean lower = false;
	
	Type targetType;
	
	LinkedList<Map<String, Object>> tabularData;
	
	public FeatureRecommendatorHandler(Parameters params, String attName) {
		this.params = params;
		tabularData = getTabularData();
		targetAttName = attName;
	}

	private LinkedList<Map<String, Object>> getTabularData() {
		AggregatedDataExtraction dataExtractor = new AggregatedDataExtraction(params);
		setDataExtractor(dataExtractor);
		dataExtractor.createTable();
		tabularData = dataExtractor.getTabularData();
		return null;
	}

	public void setDataExtractor(AggregatedDataExtraction dataExtractor) {
		dataExtractor.setSituationType(params.getSituationType());
		dataExtractor.setTraceAttNames(params.getLog().get(0).getAttributes().keySet());
		dataExtractor.setEventAttNames(toSet(params.getAllEventAttNames()));
		dataExtractor.setAggTraceAttNames(toSet(params.getAggAttNames()));
		dataExtractor.setAggEventAttNames(toSet(params.getAggAttNames()));
		dataExtractor.setActivitiesToConsider(params.getAllActivityNames());
		dataExtractor.setTargetAttName(params.getClassAttName());
		if (situationType.equals(SituationType.ES))
			dataExtractor.setEventGroupre(new EventGrouper(params));
		
		if (situationType.equals(SituationType.TS))
			if (params.getSetOfAggAttNames().contains(targetAttName))
				dataExtractor.aggTraceAttNames.add(targetAttName);
			else
				dataExtractor.traceAttNames.add(targetAttName);			
		
		// TODO add class attName to the proper att Name set for choice situation
	}

	private Set<String> toSet(ArrayList<String> list) {
		Set<String> set = new HashSet<String>();
		for (String attName : list)
			set.add(attName);
		
		return set;
	}

	public void actionPerformed(ActionEvent e) {
		// if dependent attribute is not selected show a message
		if (targetAttName == null) {
			String[] options = {"OK"};
			String message = "Feature recommendation is not possible."; 
			JPanel panel = new JPanel();
			JLabel lbl2 = new JLabel("Please select the situation type and target feature first!");
			panel.add(lbl2);
			// erase the knowledge
			int selectedOption = JOptionPane.showOptionDialog(null, panel, message, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);
			return;
		}
			
		// get the parametrs
		String[] options = {"OK"};
		String message = "Set parameters"; 
		JPanel panel = new JPanel();
		JLabel lbl = new JLabel("What is the undesirable result!");
		panel.add(lbl);
		
		Object[] values = null;
		CheckComboBox valuesCheckList = null;
		NiceDoubleSlider threshold = null;
		JCheckBox isLowerBad = null;
		if (situationType.equals(SituationType.TS)) {
			targetType = getTraceAttType(targetAttName);
			values = setParametersTrace();
		} else if (situationType.equals(SituationType.ES)) {
			targetType = getEventAttType();
			values = setParametersEvent();
		} //else  // choice situation TODO
		if (targetType.equals(Type.LITERAL)) {
			valuesCheckList = new CheckComboBox(values);
			panel.add(valuesCheckList);
		} else {
			JLabel min = new JLabel("min : " + values[0].toString());
			JLabel max = new JLabel("max : " + values[1].toString());
			panel.add(min);
			panel.add(max);
			threshold = SlickerFactory.instance().createNiceDoubleSlider("Threshold", (double) 0, 
					1, 0.5, NiceSlider.Orientation.HORIZONTAL);
			panel.add(threshold);
			isLowerBad = new JCheckBox("Is lower undesirable?");
			panel.add(isLowerBad);
		}
			
		int selectedOption = JOptionPane.showOptionDialog(null, panel, message, JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);
		
		if(selectedOption == 0)
		{
		    if (targetType.equals(Type.LITERAL) || targetType.equals(Type.BOOLEAN))
		    	badValues = valuesCheckList.getSelectedItems();
		} else {
			this.threshold = threshold.getValue();
			lower = isLowerBad.isSelected();
		}
	}
	
	private Object[] setParametersEvent() {
		String classAttName = params.getClassAttName();
		// aggregated atts
		if (params.getAggAttNames().contains(targetAttName)) {
			targetType = params.getAggAttType(targetAttName);
			Map<Long, Long> data = params.getAggData().get(0).get(classAttName);
			Object[] minMax = new Object[2];
			
			// initiate the min max
			for (Long l : data.keySet()) {
				if (data.get(l) != null) {
					minMax[0] = data.get(l);
					minMax[1] = data.get(l);
					break;
				}
			}
			// compute min max		
			for (Long l : data.keySet()) {
				if ((long) minMax[0] > data.get(l))
					minMax[0] = data.get(l);
				if ((long) minMax[1] < data.get(l))
					minMax[1] = data.get(l);
			}
			return minMax;
		}
		//normal att literal
		XLog log = params.getLog();
		EventGrouper eventGroup = new EventGrouper(params);
		NormalAttributes normalAtts = new NormalAttributes(params);
		//normal att literal
		Set<String> setOfValues = new HashSet<String>();
		if (targetType.equals(Type.BOOLEAN) || targetType.equals(Type.LITERAL)) {
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace, event, targetAttName);
						if (v != null) 
							setOfValues.add(v.toString());
					}
				}
			}
			
			Object[] literalValues = new Object[setOfValues.size()];
			int i = 0;
			for (String val : setOfValues) {
				literalValues[i] = val;
				i++;
			}
			return literalValues;
		}
		//normal att discrete
		if (targetType.equals(Type.DISCRETE)) {
			Long[] minMax = new Long[2];
			// initiate
			for (XTrace trace : log) {
				boolean flag = false;
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace, event, targetAttName);
						if (v != null) {
							minMax[0] = Long.valueOf(v.toString());
							minMax[1] = Long.valueOf(v.toString());
							flag = true;
							break;}
					}
					if (flag)
						break;
				}
				if (flag)
					break;
			}
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace, event, targetAttName);
						if (minMax[0] > Long.valueOf(v.toString()))
							minMax[0] = Long.valueOf(v.toString());
						if (minMax[1] < Long.valueOf(v.toString()))
							minMax[1] = Long.valueOf(v.toString());
					}
				}
			}
			return minMax;
		}
		//normal att continuous
		if (targetType.equals(Type.CONTINUOS)) {
			Double[] minMax = new Double[2];
			boolean flag = false;
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace,event, targetAttName);
						if (v != null) {
							minMax[0] = Double.valueOf(v.toString());
							minMax[1] = Double.valueOf(v.toString());
							flag = true;
							break;}
					}
					if (flag)
						break;
				}
				if (flag)
					break;
			}
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace, event, targetAttName);
						if (minMax[0] > Double.valueOf(v.toString()))
							minMax[0] = Double.valueOf(v.toString());
						if (minMax[1] < Double.valueOf(v.toString()))
							minMax[1] = Double.valueOf(v.toString());
					}
				}
			}
			return minMax;
		}
		// timestamp
		if (targetType.equals(Type.TIMESTAMP)) {
			Date[] minMax = new Date[2];
			boolean flag = false;
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Object v = normalAtts.getValue(trace, event, targetAttName);
						if (v != null) {
							minMax[0] = (Date) v;
							minMax[1] = (Date) v;
							flag = true;
							break;
						}
						if (flag)
							break;
					}
					if (flag)
						break;
				}
				if (flag)
					break;
			}
			for (XTrace trace : log) {
				for (XEvent event : trace) {
					if (eventGroup.isEventAnClassEvent(event)) {
						Date v = (Date) normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
						if (minMax[0].after(v))
							minMax[0] = v;
						if (minMax[1].before(v))
							minMax[1] = v;
					}
				}
			}
			return minMax;
		}
		return null;		
	}
	
	/**
	 * 
	 * @return the type of the target att name when it is an event att name
	 */
	private Type getEventAttType() {
		// aggregated atts
		if (params.getAggAttNames().contains(targetAttName)) 
			return Type.DISCRETE;
		
		// normal atts
		EventGrouper eventGroup = new EventGrouper(params);
		XLog log = params.getLog();
		for (XTrace trace : log)
			for (XEvent event : trace)
				if (eventGroup.isEventAnClassEvent(event)) {
					NormalAttributes normalAtts = new NormalAttributes(params);
					Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
					if (v != null)
						return generateDataElement(v);
				}
		return null;
	}

	/**
	 * If the att type is categorical, it returns an array of its values o.w. 
	 * arr[0] is min value and arr[1] is the max value.
	 * @return array of the att values if categorical o.w. array[0] = min value and
	 * array[1] is max value for that attribute;
	 */
	private Object[] setParametersTrace() {
		// aggregated atts
		if (params.getAggAttNames().contains(targetAttName)) {
			targetType = params.getAggAttType(targetAttName);
			Map<Long, Long> data = params.getAggData().get(0).get(params.getClassAttName());
			Object[] minMax = new Object[2];
			
			// initiate the min max
			for (Long l : data.keySet()) {
				if (data.get(l) != null) {
					minMax[0] = data.get(l);
					minMax[1] = data.get(l);
					break;
				}
			}
			// compute min max
			for (Long l : data.keySet()) {
				if ((long) minMax[0] > data.get(l))
					minMax[0] = data.get(l);
				if ((long) minMax[1] < data.get(l))
					minMax[1] = data.get(l);
			}
			return minMax;
		}
		//normal att literal
		XLog log = params.getLog();
		NormalAttributes normalAtts = new NormalAttributes(params);
		targetType = getTraceAttType(targetAttName);
		//normal att literal
		Set<String> setOfValues = new HashSet<String>();
		if (targetType.equals(Type.BOOLEAN) || targetType.equals(Type.LITERAL)) {
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (v != null) 
					setOfValues.add(v.toString());
			}
			
			Object[] literalValues = new Object[setOfValues.size()];
			int i = 0;
			for (String val : setOfValues) {
				literalValues[i] = val;
				i++;
			}
			return literalValues;
		}
		//normal att discrete
		if (targetType.equals(Type.DISCRETE)) {
			Long[] minMax = new Long[2];
			// initiate
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (v != null) {
					minMax[0] = Long.valueOf(v.toString());
					minMax[1] = Long.valueOf(v.toString());
					break;}
			}
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (minMax[0] > Long.valueOf(v.toString()))
					minMax[0] = Long.valueOf(v.toString());
				if (minMax[1] < Long.valueOf(v.toString()))
					minMax[1] = Long.valueOf(v.toString());
			}
			return minMax;
		}
		//normal att continuous
		if (targetType.equals(Type.CONTINUOS)) {
			Double[] minMax = new Double[2];
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (v != null) {
					minMax[0] = Double.valueOf(v.toString());
					minMax[1] = Double.valueOf(v.toString());
					break;}
			}
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (minMax[0] > Double.valueOf(v.toString()))
					minMax[0] = Double.valueOf(v.toString());
				if (minMax[1] < Double.valueOf(v.toString()))
					minMax[1] = Double.valueOf(v.toString());
			}
			return minMax;
		}
		// timestamp
		if (targetType.equals(Type.TIMESTAMP)) {
			Date[] minMax = new Date[2];
			for (XTrace trace : log) {
				Object v = normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (v != null) {
					minMax[0] = (Date) v;
					minMax[1] = (Date) v;
					break;}
			}
			for (XTrace trace : log) {
				Date v = (Date) normalAtts.getValue(trace, trace.get(trace.size() - 1), targetAttName);
				if (minMax[0].after(v))
					minMax[0] = v;
				if (minMax[1].before(v))
					minMax[1] = v;
			}
			return minMax;
		}
		return null;
	}

	public Type getTraceAttType(String attName) {
		if (attName.equals("Sub Model Attribute") || attName.equals("Trace Duration") ||
				attName.equals("number modelMove") || attName.equals("number logMove"))
			return Type.DISCRETE;
		
		if (attName.equals("Trace Delay") || attName.equals("deviation"))
			return Type.BOOLEAN;
		
		if (attName.startsWith("concept:") || attName.startsWith("lifecycle:"))
			return Type.LITERAL;
		
		if (attName.startsWith("time:"))
			return Type.TIMESTAMP;
		
		NormalAttributes normalAtts = new NormalAttributes(params);
		Object value = normalAtts.getValue(params.getLog().get(0), params.getLog().get(0).get(0), attName);

		return generateDataElement(value);
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
	
}