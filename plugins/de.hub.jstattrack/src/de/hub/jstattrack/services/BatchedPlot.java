package de.hub.jstattrack.services;

import java.util.Collection;
import java.util.List;

import de.hub.jstattrack.JStatTrackActivator;
import de.hub.jstattrack.data.BatchedDataSeries;

public class BatchedPlot extends AbstractStatisticalServiceImpl {
	
	private final BatchedDataSeries series = new BatchedDataSeries(JStatTrackActivator.instance.batchedDataPoints);
	private int count = 0;
	
	public BatchedPlot() {
		super("Batched Plot", seriesType);	
	}

	@Override
	public void track(double value) {
		series.add(value);
		count++;
	}

	@Override
	public void report(StringBuilder out) {
		out.append("Plot:\n");
		List<Double> asList = series.asList();
		int size = asList.size();
		plotChart(out, (Collection<Double>)asList, 0, size == 0 ? 0 : count/size);
	}

	@Override
	protected Object toJSONData() {
		List<Double> asList = series.asList();
		int size = asList.size();
		return toJSONArray(asList, 0, size == 0 ? 0 : count/size);
	}
}
