package org.processmining.causalityinference.ui.uiComponents;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.causalityinference.algorithms.MainView;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.causalityinference.parameters.SituationType;
import org.processmining.dataTable.AggregatedDataExtraction;

public class CreateAndSaveButton extends JPanel {
	
	Parameters params;
	
	SituationType situation;
	
	MainView mainView;
	
	IndependentAtt indepAttPanel;
	
	TimeSettingPanel timePanel;
	
	public CreateAndSaveButton(Parameters params, SituationType st, MainView panel) {
		this.params = params;
		situation = st;
		mainView = panel;
		
		setLayout(new GridLayout(2,1));
		JButton createTable = new JButton("Create Table");
		createTable.addActionListener(new CreateTableHandler());
		add(createTable);
		JButton saveTable = new JButton("Save Table (csvData.txt)");
		saveTable.addActionListener(new SaveTableHandler());
		add(saveTable);
	}
	
	public void setAware(IndependentAtt panelAtt, TimeSettingPanel panelTime) {
		indepAttPanel = panelAtt;
		timePanel = panelTime;
	}
	
	class CreateTableHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			params.setSituationType(situation);
			if (params == null)
				System.out.println("params null");
			if (indepAttPanel == null)
				System.out.println("indepAttPanel null");
			indepAttPanel.setIndepAttsInParam();
			
			params.setTimeLineSetting(timePanel.getTimeLineSetting());
			
			AggregatedDataExtraction tabularDataCreator = new AggregatedDataExtraction(params);
			tabularDataCreator.extractData();
			params.setDataTableCreator(tabularDataCreator);
			params.setAttTypes(tabularDataCreator.getAttTypes());
			params.setLiteralValuesTable(tabularDataCreator.getLiteralValues());
			params.setMinMax(tabularDataCreator.getMinMax());

//			model.setNumberOfNodes(params.getAttTypes().size());	
			mainView.getLeftPanel().getBayesianNetworkVisualizer().getGraphVisualizer().setInverseAttValueMap(tabularDataCreator.getInverseAttValueMap());
			tabularDataCreator.getTableExample().setAwareLeftPanel(mainView.getLeftPanel());
			mainView.getLeftPanel().getTableVisualizer().addTablePanelToView(tabularDataCreator.getTablePanel());
				
			params.setRemoveNullValues(false);
			mainView.getRightPanelCausalityGraph().setAwareDataCreator(tabularDataCreator);
			
			// Set the right panel for feature recommendation
			mainView.getRightPanelFeatureRecommendation().setAware(tabularDataCreator);
			mainView.getRightPanelFeatureRecommendation().updatePanel();
			
		}
		
	}
	
	class SaveTableHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			params.saveDataCSV();
		}
		
	}
	
}
