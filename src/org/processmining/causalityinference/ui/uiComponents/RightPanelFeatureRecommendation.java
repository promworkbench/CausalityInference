package org.processmining.causalityinference.ui.uiComponents;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.causalityinference.algorithms.LeftPanelMain;
import org.processmining.causalityinference.featureRecommedation.FeatureRecommendation;
import org.processmining.causalityinference.parameters.FeatureSelectionMethod;
import org.processmining.causalityinference.parameters.Parameters;
import org.processmining.dataTable.AggregatedDataExtraction;
import org.processmining.datadiscovery.estimators.Type;
import org.processmining.framework.plugin.PluginContext;

import csplugins.id.mapping.ui.CheckComboBox;

public class RightPanelFeatureRecommendation extends JPanel {
	
	public final static String notAllowedChars=".()&!|=<>-+*/% ";
	
	private Parameters params;
	
	private AggregatedDataExtraction tabularDataCreator;
	
	PluginContext context;
	
	LeftPanelMain leftPanel;
	
	JTextField numBins;
	
	CheckComboBox selectItems;
	
	JTextField threshold;
	
	JCheckBox isLowerUndesirable;
	
	Set<String> recmendedFeatures;
	
	JComboBox<FeatureSelectionMethod> methodFS;
	

	public RightPanelFeatureRecommendation(PluginContext context, LeftPanelMain leftPanel,
			Parameters params) {
		this.context = context;
		this.params = params;
		this.leftPanel = leftPanel;
		
		// If the table is empty
		if (tabularDataCreator == null || tabularDataCreator.getTabularData() == null) {
			JPanel panel = new JPanel();
			JLabel label = new JLabel("Please create the tabular data first!");
			panel.add(label);
			leftPanel.featureRecommendationVisualizer.addTablePanelToView(panel);
			return;
		}
	
	}
	
	public JPanel reDoFeatureRecommendation() {
		JPanel panel = new JPanel();
		// If the table is not empty
		panel.setLayout(new GridLayout(12,1));
		
		//// Add the tab content
		panel.add(new JLabel("Feature selection method:"));
		
		methodFS = new JComboBox<>();
        methodFS.setPreferredSize(new Dimension(150, 30));
        methodFS.setEnabled(true);
        methodFS.addItem(FeatureSelectionMethod.OM);
        methodFS.addItem(FeatureSelectionMethod.RF);
        methodFS.addItem(FeatureSelectionMethod.IG);
        methodFS.addItem(FeatureSelectionMethod.Corr);
        methodFS.setSelectedItem(FeatureSelectionMethod.RF);
        panel.add(methodFS);
		
		// ----> number of bins if we have numerical features
		boolean hasNumerical = false;
		for (String attName : tabularDataCreator.getAttTypes().keySet())
			if (!tabularDataCreator.getAttTypes().get(attName).equals(Type.BOOLEAN) && 
					!tabularDataCreator.getAttTypes().get(attName).equals(Type.LITERAL))
				hasNumerical = true;
		
		if (hasNumerical) {
			JLabel l = new JLabel("Number of bins for discretization");
			panel.add(l);
			numBins = new JTextField("Number of bins : ");
			numBins.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					numBins.setText("");
				}

				public void focusLost(FocusEvent e) {
					
				}
			});
			panel.add(numBins);
		}
		
		// ----> select the good values
		String classAttName = replaceNotAllowedStrings(tabularDataCreator.classAttributeName());
		Type classAttType = tabularDataCreator.getAttTypes().get(classAttName);
		//-----------------> numerical
		if (classAttType.equals(Type.BOOLEAN) || classAttType.equals(Type.LITERAL)) {
			boolean isClassAttNominal = false;
			Set<String> values = new HashSet<>();
			if (classAttType.equals(Type.BOOLEAN)) {
				values.add("true");
				values.add("false");
			}
			if (classAttType.equals(Type.LITERAL)) {
				for(String item : tabularDataCreator.getLiteralValues().get(classAttName))
					values.add(item);
			}
			
			JLabel l = new JLabel("Please select the undesirable values");
			panel.add(l);
			
			selectItems = new CheckComboBox(values);
			Dimension dim = selectItems.getPreferredSize();
	    	dim.width *= 2;
	    	selectItems.setPreferredSize(dim);
			panel.add(selectItems);
		}
		
		//-----------------> timestamp
		if (classAttType.equals(Type.TIMESTAMP)) {
			Set<String> days = new HashSet<String>();
			days.add("Sunday");
			days.add("Monday");
			days.add("Tuesday");
			days.add("Wednesday");
			days.add("Thursday");
			days.add("Friday");
			days.add("Saturday");
			
			selectItems = new CheckComboBox(days);
			panel.add(selectItems);
			
	//		JTextField min = new JTextField("00:00");
	//      max = new JTextField("00:00");
	//		add(min);
	//		add(max);
			threshold = new JTextField();
			panel.add(threshold);
			
			isLowerUndesirable = new JCheckBox("Lower values are undesirable!");
			panel.add(isLowerUndesirable);
		}
		
		if (classAttType.equals(Type.DISCRETE) || classAttType.equals(Type.CONTINUOS)) {
			JLabel lmin = new JLabel("Min : " + tabularDataCreator.getMinMax().get(classAttName)[0]);
			panel.add(lmin);
			
			JLabel lmax = new JLabel("Max : " + tabularDataCreator.getMinMax().get(classAttName)[1]);
			panel.add(lmax);
			
			threshold = new JTextField();
			panel.add(threshold);
			
			isLowerUndesirable = new JCheckBox("Lower values are undesirable!");
			panel.add(isLowerUndesirable);
		}
		
		
		JButton recommendButton = new JButton(" Recommend features ");
		panel.add(recommendButton);
		Handler handler = new Handler();
		recommendButton.addActionListener(handler); 
		
		JButton setFeaturesButton = new JButton(" Create table with recommended features ");
		panel.add(setFeaturesButton);
		CrateTableWithSelectedFeaturesHandler tableHandler = new CrateTableWithSelectedFeaturesHandler(leftPanel);
		setFeaturesButton.addActionListener(tableHandler); 
		
		return panel;			
	}
	
	public class CrateTableWithSelectedFeaturesHandler implements ActionListener {
		LeftPanelMain leftPanel;
		public CrateTableWithSelectedFeaturesHandler(LeftPanelMain lp) {
			leftPanel = lp;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			long timeS = System.currentTimeMillis();
			params.trimTheData(recmendedFeatures);
			long timeE = System.currentTimeMillis();
			System.out.println("-----------------------------");
			System.out.println("trim data time : " + (timeE - timeS));
			System.out.println("-----------------------------" );
			leftPanel.getTableVisualizer().addTablePanelToView(tabularDataCreator.getTablePanel());
		}

	}
	
	public void updatePanel() {

		leftPanel.rightPanelFeatureRecommendation.removeAll();
		leftPanel.rightPanelFeatureRecommendation.add(reDoFeatureRecommendation());
		leftPanel.rightPanelFeatureRecommendation.updateUI();
		leftPanel.rightPanelFeatureRecommendation.setEnabled(true);
		this.updateUI();
	}
	
	private class Handler implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			// set the parameters
			
			params.setFeatureSelectionMethod((FeatureSelectionMethod) methodFS.getSelectedItem());
			
			if (((FeatureSelectionMethod) methodFS.getSelectedItem()).equals(FeatureSelectionMethod.OM) || 
					((FeatureSelectionMethod) methodFS.getSelectedItem()).equals(FeatureSelectionMethod.IG))
				setThreshold();
			
			if (numBins != null)
				params.setNumBinsFR(Integer.valueOf(numBins.getText()));
			
			String classAttName = replaceNotAllowedStrings(tabularDataCreator.classAttributeName());
			Type classAttType = tabularDataCreator.getAttTypes().get(classAttName);
			
			if (classAttType.equals(Type.BOOLEAN) || classAttType.equals(Type.LITERAL)
					|| classAttType.equals(Type.TIMESTAMP)) {
				Set<String> values = new HashSet<String>();
				
				for (Object attName : selectItems.getSelectedItems())
					values.add(attName.toString());
				
				params.setUndesirableValuesFR(values);
			}
			
			if (classAttType.equals(Type.DISCRETE) || classAttType.equals(Type.CONTINUOS)
					|| classAttType.equals(Type.TIMESTAMP)) {
				params.setThresholdFR(Double.valueOf(threshold.getText()));
				params.setIsLowerUnDesirableFR(isLowerUndesirable.isSelected());
			}
			
			// feature recommendation
			long timeS = System.currentTimeMillis();
			FeatureRecommendation fr = new FeatureRecommendation(tabularDataCreator, params);
			fr.findFeatures();
			fr.generateList();
			long timeE = System.currentTimeMillis();
			System.out.println("-----------------------------");
			System.out.println("feature recommendation time : " + (timeE - timeS));
			System.out.println("num selected features : "+ fr.getSelectedAtts().size());
			System.out.println("-----------------------------" );
			recmendedFeatures = fr.getSelectedAtts();
			
			leftPanel.featureRecommendationVisualizer.addTablePanelToView(fr.getTableForView());
		}

		private void setThreshold() {
			JPanel panel = new JPanel();
			String[] options = {"OK"};
			JTextField txt = new JTextField(10);
			panel.add(txt);
			int selectedOption = JOptionPane.showOptionDialog(null, panel, "Information gain threshold", JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options , options[0]);

			if(selectedOption == 0)
			{
			    String text = txt.getText();
			    params.setInfoGainThreshold(Double.valueOf(text));
			}
			
		}	
	}

	public void setAware(AggregatedDataExtraction tabularDataCreator) {
		this.tabularDataCreator = tabularDataCreator;
	}
	
	/**
	 * removes the not allowed char for the consistency
	 */
   	public String replaceNotAllowedStrings(String str) {
  		
   		char[] array=str.toCharArray();
		for(int i=0;i<array.length;i++)
		{
			if (notAllowedChars.indexOf(array[i])!=-1)
				array[i]='_';
		}
		return (new String(array));
   	}

}
