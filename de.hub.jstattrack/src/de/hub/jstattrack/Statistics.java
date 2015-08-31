package de.hub.jstattrack;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public final class Statistics {

	private static final Multimap<StatisticsId, IWithStatistics> sources = ArrayListMultimap.create();
	private static final Map<StatisticsId, Statistic> statistics = new HashMap<StatisticsId, Statistic>();
	private static final List<StatisticsId> statisticIds = new ArrayList<StatisticsId>();
	
	private static WebServer webserver = null;
	
	private static class WebServer extends NanoHTTPD {
		private WebServer() {
			super(JStatTrackActivator.instance.webServerPort);
			try {
				start();
			} catch (IOException e) {
				JStatTrackActivator.instance.warning("Could not start jStatTrack webserver at " + JStatTrackActivator.instance.webServerPort, e);
			}
			JStatTrackActivator.instance.info("Started jStatTrack webserver at " + JStatTrackActivator.instance.webServerPort);
		}

		@Override
		public Response serve(IHTTPSession session) {
			StringBuilder out = new StringBuilder();
			report(out);
			return new Response(Status.OK, MIME_PLAINTEXT, out.toString());
		}
	}

	
	public static void init() {		
		if (JStatTrackActivator.instance.withWebServer) {
			if (webserver == null) {
				webserver = new WebServer();
			}
		}
	}
	
	public interface StatisticFactory {
		public Statistic createStatistic();
	}
	
	public static Statistic register(Class<?> clazz, String name, StatisticFactory statisticFactory) {
		StatisticsId id = new StatisticsId(clazz, name);
		if (!statisticIds.contains(id)) {
			statisticIds.add(id);
		}
		Statistic statistic = statistics.get(id);
		if (statistic == null) {
			statistic = statisticFactory.createStatistic();
			statistics.put(id, statistic);
		}
		return statistic;
	}
	
	public static Statistic register(IWithStatistics source, String name, StatisticFactory statisticFactory) {
		
		StatisticsId id = new StatisticsId(source.getClass(), name);
		if (!sources.get(id).contains(source)) {
			sources.put(id, source);
		}
		return register(source.getClass(), name, statisticFactory);
	}
	
	public static Statistic get(Class<?> clazz, String name) {
		StatisticsId key = new StatisticsId(clazz, name);
		Statistic statistic = statistics.get(key);
		return statistic;
	}
	
	public static void trackRegisteredSourcesWithStatistic() {
		for (StatisticsId id: statisticIds) {
			for (IWithStatistics source: sources.get(id)) {
				source.trackStatistics();
			}
		}
	}
	
	public static void report(StringBuilder stringBuilder) {
		for (StatisticsId id: statisticIds) {
			stringBuilder.append("-" + id.name + ":" + id.clazz.getSimpleName() + "\n");
			statistics.get(id).report(stringBuilder);
			stringBuilder.append("\n");
		}
	}

	public static void printReport(PrintStream out) {
		StringBuilder stringBuilder = new StringBuilder();
		report(stringBuilder);
		out.println(stringBuilder.toString());
	}
	
	private static class StatisticsId {
		private final Class<?> clazz;
		private final String name;
		public StatisticsId(Class<?> clazz, String name) {
			super();
			this.clazz = clazz;
			this.name = name;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatisticsId other = (StatisticsId) obj;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}


	public static String reportToString() {
		StringBuilder out = new StringBuilder();
		report(out);
		return out.toString();
	}
}
