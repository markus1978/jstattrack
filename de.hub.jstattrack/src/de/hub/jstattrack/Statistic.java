package de.hub.jstattrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import de.hub.jstattrack.Statistics.StatisticFactory;

public final class Statistic {
	
	public interface Timer {
		public void track();
	}
	
	public static class StatisticBuilder implements StatisticFactory {		
		private final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
		private long sumTime = -1;
		private TimeUnit sumTimeUnit = TimeUnit.MILLISECONDS;
		private TimeUnit timeUnit = TimeUnit.MICROSECONDS;
		
		private void add(IStatisticalService service) {
			services.add(service);
		}
		
		public StatisticBuilder sumTime(long time, TimeUnit timeUnit) {
			sumTime = time;
			sumTimeUnit = timeUnit;
			return this;
		}
		
		public StatisticBuilder sumTime(long timeInMillies) {
			return sumTime(timeInMillies, TimeUnit.MILLISECONDS);
		}
		
		public StatisticBuilder withTimeUnit(TimeUnit timeUnit) {
			this.timeUnit  = timeUnit;
			return this;
		}
		
		public StatisticBuilder withService(IStatisticalService service) {
			add(service);
			return this;
		}
		
		public Statistic register(Class<?> clazz, String name) {
			return Statistics.register(clazz, name, this);
		}
		
		public Statistic register(IWithStatistics source, String name) {
			return Statistics.register(source, name, this);
		}

		@Override
		public Statistic createStatistic() {
			Statistic statistic = new Statistic();
			statistic.sumTime = sumTime;
			statistic.sumTimeUnit = sumTimeUnit;
			statistic.timeUnit = timeUnit;
			for (IStatisticalService service : services) {
				statistic.addService(service);
			}
			return statistic;
		} 
	}
	
	private final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
	private TimeUnit timeUnit;
	private long sumTime = -1;
	public TimeUnit sumTimeUnit;
	private Stopwatch watch = null;
	private double sum = 0;
	
	private Statistic() {
	}
	
	private void addService(IStatisticalService service) {
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