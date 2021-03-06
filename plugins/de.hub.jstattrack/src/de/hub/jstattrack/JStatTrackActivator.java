package de.hub.jstattrack;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class JStatTrackActivator extends Plugin {
	public static final String commit = "$git$";
	
	public static JStatTrackActivator instance = null;
	
	public boolean withWebServer = false;
	public int webServerPort = 8080;
	public boolean logInStandAlone = false;
	public int batchedDataPoints = 23;
	
	private boolean isStandAlone = false;	

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);		
		instance = this;
		init();		
	}

	private void init() {
		Statistics.init();
	}
	
	public void enableWebServer() {
		withWebServer = true;
		Statistics.init();
	}
	
	public static void standalone() {
		instance = new JStatTrackActivator();
		instance.init();		
		
		try {
			instance.getLog();
		} catch (Exception e) {
			instance.isStandAlone = true;
		}
	}
	
	private void log(int level, String msg, Exception e) {
		if (!isStandAlone) {
			try {
				getLog().log(new Status(level, getBundle().getSymbolicName(), Status.OK, msg, e));
			} catch (Exception ex) {
				isStandAlone = true;
			}	
		}
		if (isStandAlone) {
			if (logInStandAlone) {
				System.out.println("LOG(" + level + "): " + (msg != null ? msg : "(null)") + (e != null ? ": " + e.getMessage() : ""));
			}
		}		
	}
	
	public void debug(String msg) {
		log(Status.OK, msg, null);
	}

	public void info(String msg) {
		log(Status.INFO, msg, null);
	}

	public void warning(String msg) {
		log(Status.WARNING, msg, null);		
	}
	
	public void warning(String msg, Exception e) {
		log(Status.WARNING, msg, e);
	}
	
	public void error(String msg) {
		log(Status.ERROR, msg, null);
	}
	
	public void error(String msg, Exception e) {
		log(Status.ERROR, msg, e);
	}	
}
