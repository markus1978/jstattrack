package de.hub.jstattrack;

import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

public interface IStatisticalService {
	public void init(TimeUnit unit);
	public void track(double value);
	public void report(StringBuilder out);
	public JSONObject reportToJSON();
}
