package de.hub.jstattrack;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class CountStatistic extends AbstractStatistic {
	
	private final long timeDuration;
	private final TimeUnit timeUnit;
	private final Stopwatch watch;
	
	private long sumForCurrentDuration = 0;
	
	
	public CountStatistic(long timeDuration, TimeUnit timeUnit) {
		super();
		this.timeDuration = timeDuration;
		this.timeUnit = timeUnit;
		this.watch = Stopwatch.createUnstarted();
	}

	void addService(IStatisticalService service) {
		services.add(service);
		initService(service);
	}
	
	protected void initService(IStatisticalService service) {
		service.init("#" + ((timeDuration == 1) ? "" : ""+timeDuration) + Statistics.format(timeUnit));
	}
	
	public void track() {
		if (!watch.isRunning()) {
			watch.start();
		}
		sumForCurrentDuration++;
		long time = watch.elapsed(timeUnit);
		if (time >= timeDuration) {
			trackWithServices(sumForCurrentDuration);
			sumForCurrentDuration = 0;
			watch.reset().start();
		}
	}
}
