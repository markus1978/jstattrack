package de.hub.jstattrack;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public final class Statistics {

	private static final Multimap<UUID, IWithStatistics> sources = ArrayListMultimap.create();
	private static final Map<UUID, AbstractStatistic> statistics = new HashMap<UUID, AbstractStatistic>();
	private static final List<UUID> statisticIds = new ArrayList<UUID>();
	
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
			if (session.getUri().endsWith("json")) {
				Response response = new Response(Status.OK, "application/json", reportToJSON().toString(1));
				response.addHeader("Access-Control-Allow-Origin", "http://localhost:8000"); // TODO
				return response;
			} else {
				StringBuilder out = new StringBuilder();
				report(out);
				return new Response(Status.OK, MIME_PLAINTEXT, out.toString());
			}
		}
	}

	
	public static void init() {		
		if (System.getProperty("JStatTrackPort") != null) {
			JStatTrackActivator.instance.webServerPort = Integer.parseInt(System.getProperty("JStatTrackPort"));
		}
		if (JStatTrackActivator.instance.withWebServer) {
			if (webserver == null) {
				webserver = new WebServer();
			}
		}
	}
	
	public interface StatisticFactory {
		public AbstractStatistic createStatistic();
	}
	
	public static AbstractStatistic register(UUID uuid, AbstractStatistic newStatistic) {
		if (!statisticIds.contains(uuid)) {
			statisticIds.add(uuid);
			Collections.sort(statisticIds, new Comparator<UUID>() {
				@Override
				public int compare(UUID o1, UUID o2) {
					int classCompare = o1.clazz.getCanonicalName().compareTo(o2.clazz.getCanonicalName());
					if (classCompare == 0) {
						return o1.name.compareTo(o2.name);
					} else {
						return classCompare;
					}
				}
			});
		}
		AbstractStatistic statistic = statistics.get(uuid);
		if (statistic == null) {
			statistic = newStatistic;
			statistics.put(uuid, statistic);
		}
		return statistic;
	}
	
	public static AbstractStatistic get(Class<?> clazz, String name) {
		UUID key = new UUID(clazz, name);
		AbstractStatistic statistic = statistics.get(key);
		return statistic;
	}
	
	public static void trackRegisteredSourcesWithStatistic() {
		for (UUID id: statisticIds) {
			for (IWithStatistics source: sources.get(id)) {
				source.trackStatistics();
			}
		}
	}
	
	public static void report(StringBuilder stringBuilder) {
		for (UUID id: statisticIds) {
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
	
	public static String reportToString() {
		StringBuilder out = new StringBuilder();
		report(out);
		return out.toString();
	}
	
	public static JSONArray reportToJSON() {
		JSONArray result = new JSONArray();
		
		for (UUID id: statisticIds) {
			JSONObject statistic = new JSONObject();
			statistic.put("class", id.getClassName());
			statistic.put("name", id.getStatName());
			statistic.put("services", statistics.get(id).reportToJSON());
			
			result.put(statistic);
		}
		return result;
	}
	
	public static JSONArray getStatServiceDataFromJSONReport(UUID id, String serviceName, JSONArray jsonData) {
		for (int i = 0; i < jsonData.length(); i++) {
			JSONObject jsonObject = jsonData.getJSONObject(i);
			if (id.getClassName().equals(jsonObject.getString("class")) && id.getStatName().equals(jsonObject.getString("name"))) {
				JSONArray services = jsonObject.getJSONArray("services");
				for (int serviceIndex = 0; serviceIndex < services.length(); serviceIndex++) {
					JSONObject serviceObject = services.getJSONObject(serviceIndex);
					if (serviceName.equals(serviceObject.getString("name"))) {
						return serviceObject.getJSONArray("data");
					}
				}				
			}
		}
		return null;
	}
	
	public static String format(TimeUnit unit) {
		String unitStr = null;
		switch(unit) {
		case DAYS:
			unitStr = "D";
			break;
		case HOURS:
			unitStr = "H";
			break;
		case MICROSECONDS:			
			unitStr = String.valueOf(Character.toChars(0x00B5)) + "s";
			break;
		case MILLISECONDS:
			unitStr = "ms";
			break;
		case MINUTES:
			unitStr = "M";
			break;
		case NANOSECONDS:
			unitStr = "ns";
			break;
		case SECONDS:
			unitStr = "s";
			break;
		default:
			unitStr = "unknown";
			break;
		}
		return unitStr;
	}
	
	public static UUID UUID(Class<?> theClass, String name) {
		return new UUID(theClass, name);
	}
	
	public static class UUID {
		private final Class<?> clazz;
		private final String name;
		public UUID(Class<?> theClass, String name) {
			super();
			this.clazz = theClass;
			this.name = name;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
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
			UUID other = (UUID) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (clazz == null) {
				if (other.clazz != null)
					return false;
			} else if (!clazz.equals(other.clazz))
				return false;
			return true;
		}
		
		public String getClassName() {
			return clazz.getCanonicalName();
		}
		
		public String getStatName() {
			return name;
		}
		@Override
		public String toString() {
			return getClassName() + "." + getStatName();
		}
		
	}
}
