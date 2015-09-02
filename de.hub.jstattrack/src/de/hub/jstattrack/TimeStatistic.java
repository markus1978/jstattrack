package de.hub.jstattrack;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class TimeStatistic extends AbstractStatistic {

	public interface Timer {
		public void track();
	}
	
	private final TimeUnit unit;
	
	public TimeStatistic(TimeUnit unit) {
		super();
		this.unit = unit;
	}
	
	@Override
	protected void initService(IStatisticalService service) {
		service.init(unit);
	}
	
	public Timer timer() {
		final Stopwatch watch = Stopwatch.createStarted();
		return new Timer() {			
			@Override
			public void track() {
				trackWithServices(watch.elapsed(unit));
			}
		};
	}
}
