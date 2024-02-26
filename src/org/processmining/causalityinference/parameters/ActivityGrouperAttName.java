package org.processmining.causalityinference.parameters;

public enum ActivityGrouperAttName {
	AN("activity name"), D("duration"), TS("timestamp"), R("resource");
	
	private final String name;

	private ActivityGrouperAttName(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
