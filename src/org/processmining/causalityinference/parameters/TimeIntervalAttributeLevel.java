package org.processmining.causalityinference.parameters;

public enum TimeIntervalAttributeLevel {
	PL("process Level"), TL("trace Level"), EL("event Level"), RL("resource Level");
	
	private final String name;

	private TimeIntervalAttributeLevel(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
