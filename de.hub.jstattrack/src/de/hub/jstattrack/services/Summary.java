package de.hub.jstattrack.services;

import java.text.DecimalFormat;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

public class Summary extends AbstractStatisticalServiceImpl {
	private static final DecimalFormat f = new DecimalFormat("#.#");
	private final SummaryStatistics summary = new SummaryStatistics();
	
	public Summary() {
		super("Summary", tableType);
	}

	@Override
	public void track(double value) {
		summary.addValue(value);
	}

	@Override
	public void report(StringBuilder out) {
		StatisticalSummary summary = this.summary.getSummary();
		out.append("Summary: ");
		out.append("mean=" + f.format(summary.getMean()) + ",min=" + f.format(summary.getMin()) + ",max=" + f.format(summary.getMax()));
		out.append(",n=" + f.format(summary.getN()) + ",sum=" + f.format(summary.getSum()) + ",sd=" + f.format(summary.getStandardDeviation()));
		out.append("\n");
	}
	
	private JSONObject toJSONObject(String key, Object value) {
		JSONObject result = new JSONObject();
		result.put("key", key);
		if (value instanceof Double) {
			result.put("value", Double.isFinite((Double)value) ? value : 0);
		} else {
			result.put("value", value);
		}
		return result;
	}

	@Override
	protected Object toJSONData() {		
		JSONArray data = new JSONArray();
		data.put(toJSONObject("mean", summary.getMean()));
		data.put(toJSONObject("min", summary.getMin()));
		data.put(toJSONObject("max", summary.getMax()));
		data.put(toJSONObject("N", summary.getN()));
		data.put(toJSONObject("deviation", summary.getStandardDeviation()));
		
		return data;
	}
}
