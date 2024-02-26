package org.processmining.causalityinference.parameters;

public enum FeatureSelectionMethod {

	RF("Random Forest"), IG("Info Gain"), Corr("Correlation base"), OM("SFVPR");
	private final String name;

	private FeatureSelectionMethod(final String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}