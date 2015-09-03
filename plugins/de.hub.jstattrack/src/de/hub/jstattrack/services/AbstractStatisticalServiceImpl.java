package de.hub.jstattrack.services;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hub.jstattrack.IStatisticalService;
import de.hub.jstattrack.Statistics;

public abstract class AbstractStatisticalServiceImpl implements IStatisticalService {
	
	public static final String tableType = "table";
	public static final String histogramType = "histogram";
	public static final String seriesType = "series";
	
	private final String name;
	private final String type;

	public AbstractStatisticalServiceImpl(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	protected String unit = "?";
	
	@Override
	public void init(TimeUnit unit) {
		this.unit = Statistics.format(unit);
	}
	
	@Override
	public void init(String unit) {
		this.unit = unit;
	}
	
	private static final DecimalFormat format = new DecimalFormat("0.0");
	
	protected void plotLine(StringBuilder out, double value, double maxValue) {
		out.append("[");
		int projected = (int)Math.round(23*value / maxValue);
		int i = 0;
		for (i = 0; i < projected; i++) {
			out.append("*");
		}
		for (;i<23;i++) {
			out.append(" ");
		}
		out.append("]");
	}
	
	protected void plotChart(StringBuilder out, Iterable<Double> source, double start, double binSize) {
		double max = Double.MIN_VALUE;
		int binCount = 0;
		for (Object binValueAsObject: source) {
			double binValue = (Double)binValueAsObject;
			max = binValue > max ? binValue : max;
			binCount++;
		}
		int index = 0;
		String formatedMaxValue = format.format(start+binSize*binCount);
		for (Object binValueAsObject: source) {
			double binValue = (Double)binValueAsObject;
			String formatedValue = format.format(start + ((index++)*binSize));
			out.append(formatedValue);
			for (int i = formatedValue.length(); i <= formatedMaxValue.length(); i++) {
				out.append(" ");
			} 
			plotLine(out, binValue, max);
			out.append(" " + format.format(binValue) + "\n");
		}
	}
	
	protected JSONArray toJSONArray(Iterable<Double> source, double start, double binSize) {
		JSONArray result = new JSONArray();
		int index = 0;
		for (Object binValueAsObject: source) {
			double binValue = (Double)binValueAsObject;
			double x = start + ((index++)*binSize);
			double y = binValue;
			JSONObject point = new JSONObject();
			point.put("x", Double.isFinite(x) ? x : 0);
			point.put("y", Double.isFinite(y) ? y : 0);
			result.put(point);
		}
		return result;
	}

	@Override
	public JSONObject reportToJSON() {
		JSONObject result = new JSONObject();
		result.put("name", name);
		result.put("type", type);
		
		result.put("unit", unit);
		result.put("data", toJSONData());
		return result;
	}

	protected abstract Object toJSONData();
}
