package de.hub.jstattrack;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public abstract class AbstractStatistic {

	protected final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
	private boolean first = true;
	
	synchronized void addService(IStatisticalService service) {
		services.add(service);
		initService(service);
	}
	
	protected void initService(IStatisticalService service) {
		
	}
	
	protected synchronized void trackWithServices(double value) {
		if (first) {
			first = false;
			return;
		}
		for(IStatisticalService service: services) {
			service.track(value);
		}
	}
	

	public synchronized void report(StringBuilder out) {
		for(IStatisticalService service: services) {
			service.report(out);
		}
	}

	public synchronized JSONArray reportToJSON() {
		JSONArray result = new JSONArray(); 
		for(IStatisticalService service: services) {
			result.put(service.reportToJSON());
		}			
		return result;
 	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractStatistic> E with(IStatisticalService service) {
		addService(service);
		return (E)this;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractStatistic> E with(Class<? extends IStatisticalService> service) {
		try {
			addService(service.newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (E)this;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractStatistic> E register(Class<?> clazz, String name) {
		return (E)Statistics.register(clazz, name, this);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractStatistic> E register(IWithStatistics source, String name) {
		return (E)Statistics.register(source, name, this);
	}
}