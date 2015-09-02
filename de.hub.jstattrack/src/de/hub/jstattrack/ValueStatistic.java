package de.hub.jstattrack;

public class ValueStatistic extends AbstractStatistic {

	private final String unit;
	
	public ValueStatistic(String unit) {
		super();
		this.unit = unit;
	}

	public ValueStatistic() {
		super();
		unit = "#";
	}

	public void track(double value) {
		trackWithServices(value);
	}

	@Override
	protected void initService(IStatisticalService service) {
		service.init(unit);
	}
	
	
}
