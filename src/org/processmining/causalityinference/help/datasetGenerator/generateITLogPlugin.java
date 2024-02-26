package org.processmining.causalityinference.help.datasetGenerator;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.causalityinference.help.GenerateEventLogDifferentNoiseFunctions;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class generateITLogPlugin {

@Plugin(

		name = "Generate event log for IT company with selected noise intervals", parameterLabels = {}, returnLabels = {
				"IT company log" }, returnTypes = {
						XLog.class }, userAccessible = true, help = GenerateEventLogDifferentNoiseFunctions.TEXT)
@UITopiaVariant(affiliation = "University of PADS RWTH Aachen", author = "Mahnaz", email = "m.s.qafari@pads.rwth-aachen.de")
	public static XLog apply(UIPluginContext context) {
		ITCompWizard step = new ITCompWizard();
		List<ProMWizardStep<GenerateITCompLog>> steplist = new ArrayList<ProMWizardStep<GenerateITCompLog>>();
		steplist.add(step);
		ListWizard<GenerateITCompLog> listWizard = new ListWizard<GenerateITCompLog>(steplist);
		GenerateITCompLog bl = ProMWizardDisplay.show(context, listWizard,
				new GenerateITCompLog());
	
		return bl.creatTheLog();
	
		// GenerateITCompLog g = new GenerateITCompLog();
		// return g.creatTheLog();
	}
}