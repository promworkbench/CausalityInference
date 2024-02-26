package org.processmining.causalityinference.ui.uiComponents;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class TimeWindow{
	
	JTextField window;
	String title;
	
	public TimeWindow(String str) {
		title = str;
		window = new JTextField(title);
		window.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				window.setText("");
			}

			public void focusLost(FocusEvent e) {
				
			}
		});
	}
	
	public double getValue() {
		return Double.valueOf(window.getText());
	}
	
	public String getComment() {
		return title;
	}
}
