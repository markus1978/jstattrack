package de.hub.jstattrack.services;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.base.Stopwatch;

import de.hub.jstattrack.IStatisticalService;
import de.hub.jstattrack.data.BatchedDataSeries;

public class BatchedPlot implements IStatisticalService {
	
	private final BatchedDataSeries series = new BatchedDataSeries(23);
	private int count = 0;
	
	
	@Override
	public void track(double value) {
		series.add(value);
		count++;
	}

	@Override
	public void report(StringBuilder out) {
		out.append("Plot:\n");
		Helper.plotChart(out, (Collection<Double>)series.asList(), 0, count/series.asList().size());
	}
}
