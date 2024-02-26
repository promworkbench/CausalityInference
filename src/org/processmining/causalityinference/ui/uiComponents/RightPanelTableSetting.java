package org.processmining.causalityinference.ui.uiComponents;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.processmining.causalityinference.algorithms.MainView;
import org.processmining.causalityinference.dialogs.RelativeLayout;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.framework.plugin.PluginContext;

public class RightPanelTableSetting extends JPanel {
	
	PluginContext context;
	
	private Parameters params;	
	
	SituationType situation;
	
	JPanel mainPanel;

	public RightPanelTableSetting(PluginContext context, Parameters params, SituationType situation, MainView mainView) {
		this.context = context;
		this.params = params;
		this.situation = situation;
		params.setSituationType(situation);
		
		//layout
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill( true );
		setLayout(rl);
		
		JPanel panel = new JPanel();
		panel.setLayout(new RelativeLayout(RelativeLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());
		
		// set dependent attribute setting
		DependentAtt depAttPanel = new DependentAtt(params);
		add(depAttPanel.setDepAttPanel(), new Float(10));
		
		// set independent attribute setting
		IndependentAtt indepAttPanel = new IndependentAtt(params);
		if (situation.equals(SituationType.PL))
			indepAttPanel.setIndepAttPanelprecesslevel();
		else
			indepAttPanel.setIndepAttPanel();    
		
		panel.add(indepAttPanel, new Float(60));
		
		// save button
		JPanel p2 = new JPanel();
		p2.setLayout(new RelativeLayout(RelativeLayout.Y_AXIS));
		
		// time setting
		TimeSettingPanel timePanel = new TimeSettingPanel();
		p2.add(timePanel.getTimeLinePanel(), new Float(70));
		
		// Save button
		CreateAndSaveButton saveButton = new CreateAndSaveButton(params, situation, mainView);
		saveButton.setAware(indepAttPanel, timePanel);
		p2.add(saveButton, new Float(30));
		
		panel.add(p2, new Float(40));
		
		add(panel, new Float(90));
	}

}
