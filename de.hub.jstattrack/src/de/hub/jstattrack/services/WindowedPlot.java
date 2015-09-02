package de.hub.jstattrack.services;

import java.util.Collection;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import de.hub.jstattrack.JStatTrackActivator;

public class WindowedPlot extends AbstractStatisticalServiceImpl {
	
	private final int binCount;
	private final CircularFifoBuffer binValues;
	
	private static int binCount(int windowSize) {
		return (int)(windowSize <= JStatTrackActivator.instance.batchedDataPoints ? windowSize : JStatTrackActivator.instance.batchedDataPoints);
	}
	
	public WindowedPlot() {
		this(JStatTrackActivator.instance.batchedDataPoints);
	}
	
	public WindowedPlot(int windowSize) {
		super("Windowed Plot (" + binCount(windowSize) + ")", seriesType);
		binCount = binCount(windowSize);
		binValues  = new CircularFifoBuffer(binCount);
	}

	@Override
	public void track(double value) {
		binValues.add(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void report(StringBuilder out) {
		out.append("Plot:\n");
		plotChart(out, (Collection<Double>)binValues, 0, 1);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object toJSONData() {
		return toJSONArray((Collection<Double>)binValues, 0, 1);
	}
}
