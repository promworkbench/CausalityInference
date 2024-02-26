package org.processmining.causalityinference.help.datasetGenerator;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;

public class ITCompWizard extends ProMPropertiesPanel
		implements ProMWizardStep<GenerateITCompLog> {

	private static final String TITLE = "Select the noise intervals (integers) : ";

	private ProMTextField complexityLB;
	private ProMTextField complexityUB;
	private ProMTextField priorityLB;
	private ProMTextField priorityUB;
	private ProMTextField teamSizeLB;
	private ProMTextField teamSizeUB;
	private ProMTextField PBdurationLB;
	private ProMTextField PBdurationUB;
	private ProMTextField IFdurationLB;
	private ProMTextField IFdurationUB;

	
	public ITCompWizard() {
		super(TITLE);
		
		complexityLB = this.addTextField("Complexity noise lower Bound : ");
		complexityUB = this.addTextField("Complexity noise upper Bound : ");
		priorityLB = this.addTextField("priority noise lower Bound : ");
		priorityUB = this.addTextField("priority noise upper Bound : ");
		teamSizeLB = this.addTextField("Team size noise lower Bound : ");
		teamSizeUB = this.addTextField("Team size noise upper Bound : ");
		PBdurationLB = this.addTextField("Product backlog duration noise lower Bound : ");
		PBdurationUB = this.addTextField("Product backlog duration noise upper Bound : ");
		IFdurationLB = this.addTextField("Implementation phase duration noise lower Bound : ");
		IFdurationUB = this.addTextField("Implementation phase duration noise upper Bound : ");
	}
	
	public GenerateITCompLog apply(GenerateITCompLog model, JComponent component) {
		// TODO Auto-generated method stub
		if (canApply(model, component)) {
			ITCompWizard step = (ITCompWizard) component;
			model.setComplexityLB(step.getComplexityLB());
			model.setComplexityUB(step.getComplexityUB());
			model.setPriorityLB(step.getPriorityLB());
			model.setPriorityUB(step.getPriorityUB());
			model.setTeamSizeLB(step.getTeamSizeLB());
			model.setTeamSizeUB(step.getTeamSizeUB());
			model.setPBdurationLB(step.getPBdurationLB());
			model.setIFdurationUB(step.getPBdurationUB());
			model.setIFdurationLB(step.getIFdurationLB());
			model.setIFdurationUB(step.getIFdurationUB());
		}
		return model;
	}
	


	private int getComplexityLB() {
		return Integer.valueOf(this.complexityLB.getText());
	}
	
	private int getComplexityUB() {
		return Integer.valueOf(this.complexityUB.getText());
	}
	
	private int getPriorityLB() {
		return Integer.valueOf(this.priorityLB.getText());
	}
	
	private int getPriorityUB() {
		return Integer.valueOf(this.priorityUB.getText());
	}
	
	private int getTeamSizeLB() {
		return Integer.valueOf(this.teamSizeLB.getText());
	}
	
	private int getTeamSizeUB() {
		return Integer.valueOf(this.teamSizeUB.getText());
	}

	private int getPBdurationLB() {
		return Integer.valueOf(this.PBdurationLB.getText());
	}
	
	private int getPBdurationUB() {
		return Integer.valueOf(this.PBdurationUB.getText());
	}
	
	private int getIFdurationLB() {
		return Integer.valueOf(this.IFdurationLB.getText());
	}
	
	private int getIFdurationUB() {
		return Integer.valueOf(this.IFdurationUB.getText());
	}

	public String getTitle() {
		return this.TITLE;
	}

	public boolean canApply(GenerateITCompLog model, JComponent component) {
		return component instanceof ITCompWizard;
	}

	public JComponent getComponent(GenerateITCompLog model) {
		return this;
	}
}