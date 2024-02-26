package org.processmining.causalityinference.parameters;

public enum SituationType {
	TS("Trace Situation"), ES("Event Situation"), CS("Choice Situation"), PL("Process Level");

	private final String name;

	private SituationType(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
