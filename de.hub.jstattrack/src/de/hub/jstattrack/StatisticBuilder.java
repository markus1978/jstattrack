package de.hub.jstattrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hub.jstattrack.Statistics.StatisticFactory;
import de.hub.jstattrack.services.Summary;

public class StatisticBuilder implements StatisticFactory {		
	
	private final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
	private long sumTime = -1;
	private TimeUnit sumTimeUnit = TimeUnit.MILLISECONDS;
	private TimeUnit timeUnit = TimeUnit.MICROSECONDS;
	
	public static StatisticBuilder create() {
		return new StatisticBuilder();
	}
	
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
	
	public StatisticBuilder withService(Class<? extends IStatisticalService> service) {
		try {
			add(service.newInstance());
		} catch (Exception e) {
			throw new RuntimeException();
		}
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

	public static StatisticBuilder createWithSummary() {
		return create().withService(Summary.class);
	} 
}