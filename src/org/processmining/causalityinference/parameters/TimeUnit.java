package org.processmining.causalityinference.parameters;

public enum TimeUnit {
	MS("millisecond"), Sec("second"), Min("minute"), H("hour"), D("day"), W("week");
	private final String name;

	private TimeUnit(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
