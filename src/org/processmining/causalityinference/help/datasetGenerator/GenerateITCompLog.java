package org.processmining.causalityinference.help.datasetGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.processmining.xeslite.external.XFactoryExternalStore.MapDBDiskImpl;

public class GenerateITCompLog {
	
	private int complexityLB = 1;
	private int complexityUB = 10;
	private int priorityLB = 1;
	private int priorityUB = 3;
	private int teamSizeLB = -1;
	private int teamSizeUB = 29;
	private int PBdurationLB = -2;
	private int PBdurationUB = 58;
	private int IFdurationLB = 10;
	private int IFdurationUB = 110;
	int xTimes = 20;
	
	private LinkedList<Map<String, Integer>> data = new LinkedList<>();
	private LinkedList<Map<String, Integer>> noises = new LinkedList<>();
	private String[] header = {"Implementation phase duration person day", "Complexity", "Priority", "Duration person day", "Team size"};
	

	XLog creatTheLog() {
		
		teamSizeUB = 3 * xTimes - 1;
		PBdurationUB = 6 * xTimes - 2;
		IFdurationUB = 10 * xTimes + 10;
		
		// create new log
		MapDBDiskImpl factory = new MapDBDiskImpl();
		XLog newLog = factory.createLog();
		//one day
		long oneDay = 86400000;
		// set the beginning date of the history
		Date date = null;
		String string = "January 2, 2010";
		DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
		try {
			date = format.parse(string);
		} catch (ParseException e) {
			System.out.println("problem setting the first date of history!");
			e.printStackTrace();
		}
		
		for (int j = 0; j < 1000; j++) {
			Map<String, Integer> values = getValues();
			XTrace newTrace = factory.createTrace();
			XAttributeMap tmap = newTrace.getAttributes();
			String name = "Implementation phase duration person day";
			XAttributeDiscreteImpl att = new XAttributeDiscreteImpl(name, values.get(name));
			tmap.put(name, att);
			XAttributeDiscreteImpl cAtt = new XAttributeDiscreteImpl("Complexity", values.get("Complexity"));
			tmap.put("Complexity", cAtt);
			
			// we want each implementation phase to take 200 h except the last one
			int lnt = values.get(name)/200;
			boolean flag = false; // to avoid very short implementation phase duration. at least 2 days 
			if (values.get(name)%200 > 1) {
				lnt = lnt + 1;
				flag = true;
			}
			
			String[] eventNames = new String[4 + 3 * lnt];
			eventNames[0] = "Business case development";
			eventNames[1] = "Feasilitlity stusy";
			eventNames[2] = "Product backlog";
			eventNames[3] = "Team charter";
			for (int i = 0; i < lnt; i++) {
				eventNames[3 + (i * 3) + 1] = "Development";
				eventNames[3 + (i * 3) + 2] = "Test";
				eventNames[3 + (i * 3) + 3] = "Release";
						
			}
			
			for (int i = 0; i < eventNames.length; i++) {
				XEvent event = factory.createEvent();
				XConceptExtension.instance().assignName(event, eventNames[i]);
				if (eventNames[i].equals("Business case development")) {
					XAttributeMap emap = event.getAttributes();
					String aName = "Priority";
					XAttributeDiscreteImpl at = new XAttributeDiscreteImpl(aName, values.get(aName));
					emap.put(aName, at);
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					time = time + oneDay;
					date = new Date(time);
				} else if (eventNames[i].equals("Feasilitlity stusy")) {
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					time = time + oneDay * 10;
					date = new Date(time);
				}  else if (eventNames[i].equals("Product backlog")) {
					XAttributeMap emap = event.getAttributes();
					String aName = "Duration person day";
					XAttributeDiscreteImpl at = new XAttributeDiscreteImpl(aName, values.get(aName));
					emap.put(aName, at);
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					time = time + oneDay * values.get("Duration person day");
					date = new Date(time);
				} else if (eventNames[i].equals("Team charter")) {
					XAttributeMap emap = event.getAttributes();
					String aName = "Team size";
					XAttributeDiscreteImpl at = new XAttributeDiscreteImpl(aName, values.get(aName));
					emap.put(aName, at);
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					time = time + oneDay;
					date = new Date(time);
				} else if (eventNames[i].equals("Development")) {
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					if (flag && i > eventNames.length - 4) {
						int n = values.get("Implementation phase duration person day") % 200;
						if (n % 2 == 0)
							time = time + oneDay * (n / 2);
						else
							time = time + oneDay * (n / 2) + 1;
						date = new Date(time);
					} else {
						time = time + oneDay * 100;
						date = new Date(time);
					}
				}  else if (eventNames[i].equals("Test")) {
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					if (flag && i > eventNames.length - 4) {
						int n = values.get("Implementation phase duration person day") % 200;
						time = time + oneDay * (n / 2);
						date = new Date(time);
					} else {
						time = time + oneDay * 100;
						date = new Date(time);
					}
				} else if (eventNames[i].equals("Release")) {
					XTimeExtension.instance().assignTimestamp(event, date);
					long time = date.getTime();
					time = time + oneDay;
					date = new Date(time);
				}
				
				newTrace.add(event);
			}
			
			newLog.add(newTrace);
		}
		
		writeToFile(true);
		writeToFile(false);
		return newLog;
	}
	

	private Map<String, Integer> getValues() {
		Map<String, Integer> values = new HashMap<>();
		Map<String, Integer> noise = new HashMap<>();
		Random ran = new Random();
		  
		
		// initiate : Complexity
		int cn = getRandomNoise(complexityUB, complexityLB);
		int c = cn;
		values.put("Complexity", c);
		noise.put("Complexity", cn);
		// initiate : priority
		int pn = getRandomNoise(priorityUB, priorityLB);
		int p = pn;
		values.put("Priority", p);
		noise.put("Priority", pn);
		// initiate : number of employee
		int tsn = getRandomNoise(teamSizeUB, teamSizeLB);
		int ts = 5 * c + 3 * p + tsn;
		values.put("Team size", ts);
		noise.put("Team size", tsn);
		// initiate : duration
		int pbdn = getRandomNoise(PBdurationUB, PBdurationLB);
		int pbd = 10 * c + pbdn;
		values.put("Duration person day", pbd);
		noise.put("Duration person day", pbdn);
		// maintain : duration
		int ifdn = getRandomNoise(IFdurationUB, IFdurationLB);
		int ifd = 50 * c + 5 * ts + ifdn;
		values.put("Implementation phase duration person day", ifd);
		noise.put("Implementation phase duration person day", ifdn);
		
		data.add(values);
		noises.add(noise);
		return values;
	}
	
	public int getRandomNoise(int ub, int lb) {
		Random ran = new Random();
		int noise = ran.nextInt(ub + 3) - lb + 2;
		while (noise > ub || noise  < lb)
			noise = ran.nextInt(ub + 3) - lb + 2;
		return noise;
	}

	public void setComplexityLB(int complexityLB) {
		this.complexityLB = complexityLB;
	}

	public void setComplexityUB(int complexityUB) {
		this.complexityUB = complexityUB;
	}

	public void setPriorityLB(int priorityLB) {
		this.PBdurationLB = priorityLB;
	}

	public void setPriorityUB(int priorityUB) {
		this.priorityUB = priorityUB;
	}

	public void setTeamSizeLB(int teamSizeLB) {
		this.teamSizeLB = teamSizeLB;
	}

	public void setTeamSizeUB(int teamSizeUB) {
		this.teamSizeUB = teamSizeUB;
	}

	public void setPBdurationLB(int pBdurationLB) {
		this.PBdurationLB = pBdurationLB;
	}

	public void setPBdutationUB(int pBdurationUB) {
		this.PBdurationUB = pBdurationUB;
	}

	public void setIFdurationLB(int iFdurationLB) {
		this.IFdurationLB = iFdurationLB;
	}

	public void setIFdurationUB(int iFdurationUB) {
		this.IFdurationUB = iFdurationUB;
	}
	
	private void writeToFile(boolean flag) {
		String fileName = "data.csv";
		
		if (flag)
			fileName = "noise.csv";
		
		try{
	        Writer output = null;
	        File file = new File(fileName);
	        output = new BufferedWriter(new FileWriter(file));
	        
	        output.write(headerString());
	        output.write(System.getProperty( "line.separator" ));
	        
	        for(int rowNum = 0; rowNum < noises.size(); rowNum++){
	        	String r = row(data.get(rowNum));
	        	if (flag)
	        		r = row(noises.get(rowNum));
	           output.write(r);
	          output.write(System.getProperty( "line.separator" ));
	        }

	        output.close();
	        System.out.println("File has been written");

	    }catch(Exception e){
	        System.out.println("Could not create file");
	    }
	}
	
	private String row(Map<String, Integer> instanceNoise) {
		String r = new String();
		for (int col = 0; col < header.length; col++)
			r = r + instanceNoise.get(header[col]).toString() + ",";
		
		return r.substring(0, r.length()-1);
	}

	private String headerString() {
		String h = new String();
		for (int i = 0; i < header.length; i++)
			h = h + header[i] + ",";
		
		return h.substring(0, h.length()-1);
	}
}
