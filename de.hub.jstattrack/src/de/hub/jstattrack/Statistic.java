package de.hub.jstattrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public final class Statistic {
	
	public interface Timer {
		public void track();
	}
	
	protected final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
	protected TimeUnit timeUnit;
	protected long sumTime = -1;
	protected TimeUnit sumTimeUnit;
	protected Stopwatch watch = null;
	protected double sum = 0;
	
	protected Statistic() {
	}
	
	void addService(IStatisticalService service) {
		services.add(service);
	}
	
	public void track() {
		track(1);
	}
	
	public void track(double value) {
		if (sumTime < 0) {
			for(IStatisticalService service: services) {
				service.track(value);
			}
		} else {
			if (watch == null) {
				watch = Stopwatch.createStarted();
			}
			if (watch.elapsed(sumTimeUnit) > sumTime) {
				for(IStatisticalService service: services) {
					service.track(sum);
				}	
				watch.reset().start();
				sum = 0;
			}
			sum += value;
		}
	}
	
	public Timer timer() {
		final Stopwatch watch = Stopwatch.createStarted();
		return new Timer() {			
			@Override
			public void track() {
				Statistic.this.track(watch.elapsed(timeUnit));
			}
		};
	}
	
	public void report(StringBuilder out) {
		for(IStatisticalService service: services) {
			service.report(out);
		}
	}
}