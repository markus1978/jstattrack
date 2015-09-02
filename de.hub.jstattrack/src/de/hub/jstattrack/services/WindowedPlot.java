package de.hub.jstattrack.services;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.google.common.base.Stopwatch;

import de.hub.jstattrack.JStatTrackActivator;

public class WindowedPlot extends AbstractStatisticalServiceImpl {
	
	private final long binDuration;
	private final SummaryStatistics bin = new SummaryStatistics();
	private final Stopwatch watch = Stopwatch.createUnstarted();
	
	private final CircularFifoBuffer binValues = new CircularFifoBuffer(JStatTrackActivator.instance.batchedDataPoints);
	
	public WindowedPlot(long windowSizeInMillies) {
		super("Windowed Plot", seriesType);
		binDuration = windowSizeInMillies / JStatTrackActivator.instance.batchedDataPoints;
	}

	@Override
	public void track(double value) {
		if (!watch.isRunning()) {
			watch.start();
		}
		if (watch.elapsed(TimeUnit.MILLISECONDS) > binDuration) {
			double binValue = bin.getMean();
			bin.clear();
			binValues.add(binValue);
			watch.reset().start();
		}
		
		bin.addValue(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void report(StringBuilder out) {
		out.append("Plot:\n");
		plotChart(out, (Collection<Double>)binValues, 0, binDuration);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object toJSONData() {
		return toJSONArray((Collection<Double>)binValues, 0, binDuration);
	}
}
