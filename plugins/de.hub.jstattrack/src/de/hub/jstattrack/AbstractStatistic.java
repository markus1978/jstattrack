package de.hub.jstattrack;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import de.hub.jstattrack.Statistics.UUID;

public abstract class AbstractStatistic {

	protected final List<IStatisticalService> services = new ArrayList<IStatisticalService>();
	private boolean first = true;
	private UUID id = null;
	
	synchronized void addService(IStatisticalService service) {
		services.add(service);
		initService(service);
	}
	
	public UUID getId() {
		return id;
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
	public <E extends AbstractStatistic> E register(UUID uuid) {
		return (E)Statistics.register(uuid, this);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends AbstractStatistic> E register(Class<?> theClass, String name) {
		id = new UUID(theClass, name);
		return (E)Statistics.register(id, this);
	}
}