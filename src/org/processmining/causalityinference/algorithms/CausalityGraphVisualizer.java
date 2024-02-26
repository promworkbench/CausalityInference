package org.processmining.causalityinference.algorithms;

import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;


public class CausalityGraphVisualizer {
@Plugin(name = "Causal Graph Visualizer", parameterLabels = { "CausalGraph" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer

	@PluginVariant(requiredParameterLabels = { 0 })
	public JPanel visualize(PluginContext context, CausalityGraph model) {
		MainView view = new MainView(context, model);
		
		return view;
	}
}

