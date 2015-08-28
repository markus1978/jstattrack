package de.hub.jstattrack;

public interface IStatisticalService {
	public void track(double value);
	public void report(StringBuilder out);
}
