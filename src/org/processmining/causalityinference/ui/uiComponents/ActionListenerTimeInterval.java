package org.processmining.causalityinference.ui.uiComponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;

import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.TimeIntervalAttributeLevel;

public class ActionListenerTimeInterval implements ActionListener{
	
	private DependentAtt depAttPanel;
	private Parameters params;

	public ActionListenerTimeInterval(DependentAtt depAttP, Parameters params) {
		this.depAttPanel = depAttP;
		this.params = params;
	}

	public void actionPerformed(ActionEvent e) {
		TimeIntervalAttributeLevel level = depAttPanel.getLevel();
	
		if (level.equals(TimeIntervalAttributeLevel.PL)) {
			depAttPanel.getAttCB().setModel( new DefaultComboBoxModel<String>(params.getProcessLevelAggAttNames()));
			depAttPanel.getAttCB().setEnabled(true);
			depAttPanel.getAttCB().setSelectedIndex(0);
			depAttPanel.getAttCB2().setEnabled(false);
		} else if (level.equals(TimeIntervalAttributeLevel.TL)) {
			
			depAttPanel.getAttCB().setModel( new DefaultComboBoxModel<String>(params.getTraceLevelAggAttNames()));
			depAttPanel.getAttCB().setEnabled(true);
			depAttPanel.getAttCB().setSelectedIndex(0);
			depAttPanel.getAttCB2().setEnabled(false);
		} else if (level.equals(TimeIntervalAttributeLevel.EL)) {
			
			depAttPanel.getAttCB().setModel( new DefaultComboBoxModel<String>(params.getEventLevelAggAttNames()));
			depAttPanel.getAttCB().setEnabled(true);
			depAttPanel.getAttCB().setSelectedIndex(0);
			String[] actNames = new String[params.getAllActivityNames().size()];
			int i = 0;
			for (String actName : params.getAllActivityNames()) {
				actNames[i] = actName;
				i++;
			}
			depAttPanel.getAttCB2().setModel( new DefaultComboBoxModel<String>(actNames));
			depAttPanel.getAttCB2().setEnabled(true);
			depAttPanel.getAttCB2().setSelectedIndex(0);
		} else if (level.equals(TimeIntervalAttributeLevel.RL)) {
			
			String resourceAttName = null;
			if (params.getAllLiteralValues().containsKey("org:resource")) 
				resourceAttName = "org:resource";
			if (params.getAllLiteralValues().containsKey("resource")) 
				resourceAttName = "resource";
			
			depAttPanel.getAttCB().setModel( new DefaultComboBoxModel<String>(params.getResourceLevelAggAttNames()));
			depAttPanel.getAttCB().setEnabled(true);
			depAttPanel.getAttCB().setSelectedIndex(0);
			String[] resNames = new String[params.getAllLiteralValues().get(resourceAttName).size()];
			int i = 0;
			Set<String> resources = params.getAllLiteralValues().get(resourceAttName);
			for (String resName : resources) {
				resNames[i] = resName;
				i++;
			}
			depAttPanel.getAttCB2().setModel( new DefaultComboBoxModel<String>(resNames));
			depAttPanel.getAttCB2().setEnabled(true);
			depAttPanel.getAttCB2().setSelectedIndex(0);
		}
		
	}

}
