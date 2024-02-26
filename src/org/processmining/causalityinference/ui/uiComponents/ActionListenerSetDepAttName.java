package org.processmining.causalityinference.ui.uiComponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.TimeIntervalAttributeLevel;

public class ActionListenerSetDepAttName implements ActionListener {
	DependentAtt dependentAtt; 
	Parameters params;
	
	public ActionListenerSetDepAttName(DependentAtt dependentAtt, Parameters params) {
		this.dependentAtt = dependentAtt;
		this.params = params;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (dependentAtt.getAttCB().getSelectedItem() != null && dependentAtt.getAttCB().getSelectedItem() != null) {
			String firstPart = (String) dependentAtt.getAttCB().getSelectedItem();
			String secondPart = (String) dependentAtt.getAttCB2().getSelectedItem();
			
			if (dependentAtt.getLevel().equals(TimeIntervalAttributeLevel.PL) || dependentAtt.getLevel().equals(TimeIntervalAttributeLevel.TL)) 
				params.setClassAttName(firstPart);
			
			if (dependentAtt.getLevel().equals(TimeIntervalAttributeLevel.EL) || dependentAtt.getLevel().equals(TimeIntervalAttributeLevel.RL)) 
				params.setClassAttName(firstPart + " : " + secondPart);
			
		}
		
	}

}
