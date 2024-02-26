package org.processmining.causalityinference.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.causalityinference.algorithms.CausalityGraph;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;


public class CausalInferencePluginLog {
	
	@Plugin(name = "Root-cause Analysis Using Structural Equation Model (event log)", parameterLabels =   
		{ "XESLog", "Model", "Conformance Checking Result" }, returnLabels = { "unmodified log" }, 
		returnTypes = { CausalityGraph.class }, userAccessible = true, help = "Please refer to \"Causality Inference plugin Manual.pdf\" "
				+ "in https://github.com/mahnaz-qafari/Manual for a detailed manual." )
	
	@UITopiaVariant(affiliation = "PADS RWTH Aachen", author = "Mahnaz Qafari", email = "m.s.qafari@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0})
	public static CausalityGraph apply(PluginContext context, XLog log) {
		CausalityGraph cg = new CausalityGraph(log); 
		return cg;
	}
}
