package org.ocmc.archiver;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.ocmc.ioc.liturgical.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a thread to periodically archive files
 * @author mac002
 *
 */
public class ArchiverApp {
	private static final Logger logger = LoggerFactory.getLogger(ArchiverApp.class);

	private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private static String messagingToken = "";
	private static boolean messagingEnabled = false;
	
	/**
	 * If the property is null, the method returns
	 * back the value of var, otherwise it checks
	 * prop to see if it starts with "true".  If so
	 * it returns true, else false.  
	 * 
	 * The var is passed in so that if the config file lacks 
	 * the specified property, the default value gets used.
	 * @param var the variable
	 * @param prop the property
	 * @return true if so
	 */
	public static boolean toBoolean(boolean var, String prop) {
		if (prop == null) {
			return var;
		} else {
			return prop.startsWith("true");
		}
	}

	/**
	 * @param args - unused
	 */
	public static void main( String[] args ) {
    	try {

    		/**
    		 * Read in the environmental variables.  
    		 * Folders to copy
    		 * Archive folder path
    		 * 
    		 */
    		String pathRoot = System.getenv("PATH_ROOT");
    		String dirSite = System.getenv("DIR_SITE");
    		String dirClient = System.getenv("DIR_CLIENT");
    		String dirApp = System.getenv("DIR_APP");
    		String dirAudio = System.getenv("DIR_AUDIO");
    		String dirMedia = System.getenv("DIR_MEDIA");
       		String copyAll =      System.getenv("COPY_ALL");
       		String copyLastMonth =      System.getenv("COPY_LAST_MONTH");
       		String deleteFilesFrom =      System.getenv("DELETE_FILES_FROM");
       	       		
    		int initialDelay       = 1;
    		int period               = 4;
    		try {
        		initialDelay = Integer.parseInt(System.getenv("INITIAL_DELAY"));
    		} catch (Exception e) {
    			initialDelay = 1;
    		}
    		try {
        		period = Integer.parseInt(System.getenv("PERIOD"));
    		} catch (Exception e) {
    			period = 4;
    		}

    		String unit = System.getenv("TIME_UNIT");
    		TimeUnit timeUnit = TimeUnit.HOURS;
    		if (unit != null) {
        		switch (unit.toLowerCase()) {
        		case ("seconds"): {
        			timeUnit = TimeUnit.SECONDS;
        			break;
        		}
        		case ("minutes"): {
        			timeUnit = TimeUnit.MINUTES;
        			break;
        		}
        		default: {
        			timeUnit = TimeUnit.HOURS;
        		}
        		}
    		}

    		int archiveDay  = 1;
    		try {
        		archiveDay = Integer.parseInt(System.getenv("ARCHIVE_DAY"));
    		} catch (Exception e) {
    			archiveDay = 1; // the first of the month
    		}

    		boolean zipAll = true;
    		boolean zipMedia = true;
    		boolean zipAudio = true;
    		boolean zipClient = true;
    		boolean archiverEnabled = true;
    		boolean zipperEnabled = true;
    		
    		String propArchiverEnabled = System.getenv("ARCHIVER_ENABLED");
    		if (propArchiverEnabled == null) {
    			archiverEnabled = true;
    		} else {
    			archiverEnabled = propArchiverEnabled.toLowerCase().equals("true");
    		}

    		String propZipperEnabled = System.getenv("ZIPPER_ENABLED");
    		if (propZipperEnabled == null) {
    			zipperEnabled = true;
    		} else {
       			zipperEnabled = propZipperEnabled.toLowerCase().equals("true");
    		}

    		String propZipAll = System.getenv("ZIP_ALL");
    		if (propZipAll != null && propZipAll.toLowerCase().equals("true")) {
    			zipAll = true;
    		} else {
    			zipAll = false;
    		}

    		String propZipAudio = System.getenv("ZIP_AUDIO");
    		if (propZipAudio != null && propZipAudio.toLowerCase().equals("true")) {
    			zipAudio = true;
    		} else {
    			zipAudio = false;
    		}
    		
    		String propZipClient = System.getenv("ZIP_CLIENT");
    		if (propZipClient != null && propZipClient.toLowerCase().equals("true")) {
    			zipClient = true;
    		} else {
        		zipClient = false;
    		}

    		String propZipMedia = System.getenv("ZIP_MEDIA");
    		if (propZipMedia != null && propZipMedia.toLowerCase().equals("true")) {
    			zipMedia = true;
    		} else {
    			zipMedia = false;
    		}

    		boolean debugEnabled = false;
    		boolean reinitEnabled = false;

    		String propDebugEnabled = System.getenv("DEBUG_ENABLED");
    		if (propDebugEnabled != null && propDebugEnabled.toLowerCase().equals("true")) {
    			debugEnabled = true;
    		}

    		String propReinitEnabled = System.getenv("REINIT_ENABLED");
    		if (propReinitEnabled != null && propReinitEnabled.toLowerCase().equals("true")) {
    			reinitEnabled = true;
    		}

    		String propMessagingEnabled = System.getenv("MSG_ENABLED");
    		if (propMessagingEnabled == null) {
    			messagingEnabled = false;
    		} else if (propMessagingEnabled.toLowerCase().equals("true")) {
    			messagingEnabled = true;
    		} else {
    			messagingEnabled = false;
    		}

    		String strMessagingToken = System.getenv("MSG_TOKEN");
    		if (strMessagingToken == null) {
    			messagingEnabled = false;
    		} else {
    			messagingToken = strMessagingToken;
    		}

    		try {
           		if (pathRoot == null 
           				|| dirSite == null 
           				|| dirClient == null 
           				|| dirApp == null 
           				|| dirAudio == null 
           				|| dirMedia == null
           				) {
          			logger.error("PATH_ROOT, DIR_SITE, DIR_CLIENT, DIR_APP, DIR_AUDIO, and DIR_MEDIA  are required. Stopping the app.");
           		} else {
         			 logger.info("Archiver version: " + Constants.VERSION);
	       			logger.info("logger info enabled = " + logger.isInfoEnabled());
	       			logger.info("logger warn enabled = " + logger.isWarnEnabled());
	       			logger.info("logger trace enabled = " + logger.isTraceEnabled());
	       			logger.info("logger debug enabled = " + logger.isDebugEnabled());
	       			logger.debug("If you see this, logger.debug is working");
	       			ArchiverApp.class.getClassLoader();
	       			String location = getLocation();
	       			logger.info("Jar is executing from: " + location);
	       			logger.info("PATH_ROOT = " + pathRoot);
	       			logger.info("DIR_SITE = " + dirSite);
	       			logger.info("DIR_CLIENT = " + dirClient);
	       			logger.info("DIR_APP = " + dirApp);
	       			logger.info("DIR_Audio = " + dirAudio);
	       			logger.info("DIR_Media = " + dirMedia);
	       		
	       			logger.info("COPY_ALL = " + copyAll);
	       			logger.info("COPY_LAST_MONTH = " + copyLastMonth);
	       			logger.info("ARCHIVE_DAY = " + archiveDay);
	       			logger.info("INITIAL_DELAY = " + initialDelay);
	       			logger.info("PERIOD = " + period);
	       			logger.info("TIME_UNIT = " + timeUnit);
	       			logger.info("MSG_ENABLED = " + messagingEnabled);
	       			logger.info("DEBUG_ENABLED = " + debugEnabled);
	       			logger.info("REINIT_ENABLED = " + reinitEnabled);
	       			
	       			logger.info("ZIP_ALL = " + zipAll);
	       			logger.info("ZIP_AUDIO = " + zipAudio);
	       			logger.info("ZIP_CLIENT = " + zipClient);
	       			logger.info("ZIP_MEDIA = " + zipMedia);
	       			
	       			if (archiverEnabled) {
	   					executorService.scheduleAtFixedRate(
	   							new Archiver(
       									pathRoot
       						    		, dirSite 
       						    		, dirClient
       						    		, dirApp
       						    		, dirAudio
       						    		, dirMedia
	   									, copyAll
	   									, copyLastMonth
	   									, deleteFilesFrom
	   									, archiveDay
	   									, messagingEnabled
	   									, debugEnabled
	   									, reinitEnabled
	   									)
	   							, initialDelay
	   							, period
	   							, timeUnit
	   							);
	       			}
	       			if (zipperEnabled) {
	      	    		try {
	       					executorService.scheduleAtFixedRate(
	       							new Zipper(
	       									pathRoot
	       						    		, dirSite 
	       						    		, dirClient
	       						    		, dirApp
	       						    		, dirAudio
	       						    		, dirMedia
	       						    		, zipAll
	       						    		, zipAudio
	       						    		, zipClient
	       						    		, zipMedia
	       									, debugEnabled
	       									)
	       							, initialDelay
	       							, period
	       							, timeUnit
	       							);
	       	    		} catch (Exception e) {
	       	    			ErrorUtils.report(logger, e);
	       	    		}
	       			}
            		}
	    		} catch (Exception e) {
	    			ErrorUtils.report(logger, e);
	    		}
    	} catch (ArrayIndexOutOfBoundsException arrayError) {
    		logger.error("You failed to pass in one or more of: username, password, github token, slack token");
    	} catch (Exception e) {
    		ErrorUtils.report(logger, e);
    	}
    }
	
	  public static String getLocation() {
		  try {
			return new File(ArchiverApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
		} catch (URISyntaxException e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	  }
	  
	  public static String sendMessage(String message) {
		  if (messagingEnabled) {
			  String response = "";
			  MessageUtils.sendMessage(messagingToken, message);
			  return response;
		  } else {
			  return "Messaging not enabled";
		  }
	  }

}
