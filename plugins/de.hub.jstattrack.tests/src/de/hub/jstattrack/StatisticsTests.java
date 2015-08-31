package de.hub.jstattrack;

import org.junit.Test;

import de.hub.jstattrack.services.BatchedPlot;

public class StatisticsTests {
	
	@Test
	public void testBatchedTimeSeries() {
		Statistic stat = StatisticBuilder.create().withService(BatchedPlot.class).register(BatchedPlot.class, "test");
		
		for (int i = 0; i < 1000; i++) {
			stat.track(i);
		}
		
		Statistics.printReport(System.out);
	}
}