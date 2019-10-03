package org.ocmc.archiver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.jgit.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Archives files
 * 
 * @author mac002
 *
 */
public class ArchiverService implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ArchiverService.class);
	
	ArchiveIndex indexUtils = null;
	String pathRoot = "";
	File fileRoot;
	int fileRootLength = 0;
	String dirSite = "";
	File srcFileSite;
	File toFileSite;
	String dirClient = "";
	File srcFileClient;
	File toFileClient;
	String srcPathClientFile = "";
	String dirApp = "";
	File srcFileApp;
	File toFileApp;
	String dirArchive = "archive";
	File toFileArchive;
	String dirAudio = "";
	File srcFileAudio;
	File toFileAudio;
	String srcPathAudioFile = "";
	String dirMedia = "";
	File srcFileMedia;
	File toFileMedia;
	String srcPathMediaFile = "";
	File archiveFile = null;
	File archiveRootFile = null;
	File archiveIndexFile = null;
	String pathInstructionsFile = "";
	File srcFileInstructions = null;

	
	String copyAllPaths = "";
	String copyLastMonthPaths = "";
	String deleteFilesFrom = "";
	boolean messagingEnabled = false;
	boolean debugEnabled = false;
	boolean reinitEnabled = false;
	String year = "";
	String month = "";
	int intYear = 0;
	int intMonth = 0;
	int archiveDay = 1;
	List<String> copyAllList = new ArrayList<String>();
	List<String> copyLastMonthList = new ArrayList<String>();
	List<String> deleteFilesList = new ArrayList<String>();
	boolean zipAll = false;
	boolean zipAudio = false;
	boolean zipClient = false;
	boolean zipMedia = false;
	boolean zipperEnabled = false;

	
	public ArchiverService(
			String pathRoot
			, String dirSite
			, String dirClient
			, String dirApp
			, String dirAudio
			, String dirMedia
			, String pathInstructionsFile
			, String copyAllPaths
			, String copyLastMonthPaths
			, String deleteFilesFrom
			, int day
			, boolean messagingEnabled
			, boolean debugEnabled
			, boolean reinitEnabled
			, boolean zipperEnabled
			, boolean zipAll
			, boolean zipAudio
			, boolean zipClient
			, boolean zipMedia
			) {
		super();
		
		this.pathRoot = this.slash(pathRoot);
		this.dirApp = this.slash(dirApp);
		this.dirAudio = this.slash(dirAudio);
		this.dirClient = this.slash(dirClient);
		this.dirMedia = this.slash(dirMedia);
		this.dirSite = this.slash(dirSite);
		this.fileRoot = new File(this.pathRoot); // ~/html
		this.fileRootLength = this.fileRoot.getAbsolutePath().length();
		this.srcFileSite = new File(this.fileRoot.getAbsolutePath() + "/" + this.dirSite); // ~/html/dcs
		this.srcFileAudio = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirAudio);// ~/html/dcs/public/a
		this.srcFileClient = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirClient); // ~/html/dcs/public
		this.srcFileMedia = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirMedia); // ~/html/dcs/public/m
		this.srcFileApp = new File(this.srcFileClient.getAbsolutePath() + "/" + this.dirApp); // ~/html/dcs/public/dcs
		this.archiveIndexFile = new File(this.srcFileSite + "/" + this.dirArchive + "/index.html");
		this.pathInstructionsFile = pathInstructionsFile;
		this.srcFileInstructions = new File(this.pathInstructionsFile);
		if (this.srcFileInstructions.exists()) {
			indexUtils = new ArchiveIndex(org.ocmc.ioc.liturgical.utils.FileUtils.getFileContents(srcFileInstructions));
		} else {
			try {
				org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(this.pathInstructionsFile, ArchiveIndex.getInstructionsFile());
				indexUtils = new ArchiveIndex();
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		this.copyAllPaths = copyAllPaths;
		this.copyLastMonthPaths = copyLastMonthPaths;
		this.deleteFilesFrom = deleteFilesFrom;
		this.messagingEnabled = messagingEnabled;
		this.debugEnabled = debugEnabled;
		this.reinitEnabled = reinitEnabled;
		this.archiveDay = day;
		this.zipAll = zipAll;
		this.zipAudio = zipAudio;
		this.zipClient = zipClient;
		this.zipMedia = zipMedia;
		this.zipperEnabled = zipperEnabled;
	}
	
	
	@Override
	public void run() {
		logger.info("Archiver service started...");
		
		ArchiverTask archiverTask = 	new ArchiverTask(
					this.pathRoot
		    		, this.dirSite 
		    		, this.dirClient
		    		, this.dirApp
		    		, this.dirAudio
		    		, this.dirMedia
		    		, this.pathInstructionsFile
					, this.copyAllPaths
					, this.copyLastMonthPaths
					, this.deleteFilesFrom
					, this.archiveDay
					, this.messagingEnabled
					, this.debugEnabled
					, this.reinitEnabled
					);
			archiverTask.process();

			ZipperTask zipperTask = new ZipperTask(
						this.pathRoot
			    		, this.dirSite 
			    		, this.dirClient
			    		, this.dirApp
			    		, this.dirAudio
			    		, this.dirMedia
			    		, this.pathInstructionsFile
			    		, this.zipAll
			    		, this.zipAudio
			    		, this.zipClient
			    		, this.zipMedia
						, this.debugEnabled
						);
			zipperTask.process();
			logger.info("Archiver service finished...");
	}
	
	private String slash(String path) {
		if (path.endsWith("/")) {
			return path;
		} else {
			return path + "/";
		}
	}
	
}
