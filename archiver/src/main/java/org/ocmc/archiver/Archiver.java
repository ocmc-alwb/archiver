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
public class Archiver implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Archiver.class);
	
	ArchiveIndex indexUtils = new ArchiveIndex();
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
	
	
	public Archiver(
			String pathRoot
			, String dirSite
			, String dirClient
			, String dirApp
			, String dirAudio
			, String dirMedia
			, String copyAllPaths
			, String copyLastMonthPaths
			, String deleteFilesFrom
			, int day
			, boolean messagingEnabled
			, boolean debugEnabled
			, boolean reinitEnabled
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
		this.copyAllPaths = copyAllPaths;
		this.copyLastMonthPaths = copyLastMonthPaths;
		this.deleteFilesFrom = deleteFilesFrom;
		this.messagingEnabled = messagingEnabled;
		this.debugEnabled = debugEnabled;
		this.reinitEnabled = reinitEnabled;
		this.archiveDay = day;
		this.initializeLists();
	}
	
	private void initializeArchiveIndex() {
		try {
			if (! this.archiveIndexFile.exists()) {
				this.archiveIndexFile.getParentFile().mkdirs();
				org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(
						this.archiveIndexFile.getAbsolutePath()
						, this.indexUtils.getEmptyTable()
						);
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	private List<String> getArchiveFolderPaths() {
		List<String> result = new ArrayList<String>();
		for (File dir : this.srcFileSite.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
			if (dir.getName().startsWith(this.dirArchive) && dir.getName().length() > this.dirArchive.length()) {
				result.add(dir.getAbsolutePath());
			}
		}
		return result;
	}
	
	private void updateArchiveIndex() {
		try {
			Document doc = Jsoup.parse(this.archiveIndexFile, "UTF-8", "http://example.com/");
			Element yearTable = doc.select("table#yearTable").first();
			Element yearSpan = doc.select("span#yearSpan").first();
			yearSpan.text("Year Archive last updated " + Instant.now().toString() + " (GMT)");
			yearTable.children().remove();
			for (String archivePath : this.getArchiveFolderPaths()) {
				String year = archivePath.substring(archivePath.length()-4);
				String archiveDir = "/" + this.dirArchive + year;
				yearTable.append(this.indexUtils.getYearRowHtml(year));
			}
			org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(
					this.archiveIndexFile.getAbsolutePath()
					, doc.toString()
					);
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	private void initializeLists() {
		for (String path : this.copyAllPaths.split(";")) {
			this.copyAllList.add(this.slash(path));
		}
		for (String path : this.copyLastMonthPaths.split(";")) {
			this.copyLastMonthList.add(this.slash(path));
		}
		for (String path : this.deleteFilesFrom.split(";")) {
			this.deleteFilesList.add(this.slash(path));
		}
	}
	private String slash(String path) {
		if (path.endsWith("/")) {
			return path;
		} else {
			return path + "/";
		}
	}
	
	private void copy(File fromDir, File toDir, boolean useParentAsDestination) {
		try {
			if (! toDir.exists()) {
				FileUtils.mkdirs(toDir);
			} else {
				org.apache.commons.io.FileUtils.cleanDirectory(toDir);
			}
			File to = null;
			if (useParentAsDestination) {
				to = toDir.getParentFile();
			} else {
				to = toDir;
			}
			org.apache.commons.io.FileUtils.copyDirectoryToDirectory(
					fromDir
					, to
					);
		} catch (IOException e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	@Override
	public void run() {
		this.archiveRootFile = this.srcFileSite;
		File logFile = new File(this.archiveRootFile.getAbsolutePath() + "/archive/archiver.log");
		Date today = new Date(); 
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		this.intMonth = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		this.year = Integer.toString(year);
		this.month = String.format("%02d", this.intMonth);
		

		if (this.archiveDay == -1 || this.archiveDay == dayOfMonth) {
			Instant start = Instant.now();
			this.sendMessage("Archiving files");
			this.initializeArchiveIndex();
			String archiveYearPath = this.archiveRootFile.getAbsolutePath() + "/archive" + this.year + "/";
			this.archiveFile = new File(archiveYearPath);
			this.toFileClient = new File(archiveYearPath);
			this.toFileApp = new File(this.toFileClient.getAbsolutePath() + "/" + this.dirApp);

			StringBuffer logFileLine = new StringBuffer();
			
			if (! this.archiveFile.exists()) {
				this.reinitEnabled = true;
			}
			if (this.reinitEnabled) {
				this.copy(
						this.srcFileApp
						, this.toFileClient
						, false
						);
			} else {
				// clear out the files in the root and copy from source
				for (String path : this.deleteFilesList) {
					Collection<File> files = org.apache.commons.io.FileUtils.listFiles(this.toFileApp
							, new WildcardFileFilter("*", IOCase.INSENSITIVE)
							, new NotFileFilter(DirectoryFileFilter.DIRECTORY));
					for (File f : files) {
						if (! f.getName().startsWith("servicesindex.html")) {
							if (this.debugEnabled) {
								logger.info("Deleting " + f.getAbsolutePath());
							}
							f.delete();
						}
					}
					files = org.apache.commons.io.FileUtils.listFiles(this.srcFileApp
							, new WildcardFileFilter("*", IOCase.INSENSITIVE)
							,new NotFileFilter(DirectoryFileFilter.DIRECTORY));
					for (File f : files) {
						if (! f.getName().startsWith("servicesindex.html")) { // preserve it so we have prior month's information
							if (this.debugEnabled) {
								logger.info("Copying " + f.getAbsolutePath());
							}
							try {
								File to = new File(this.toFileApp.getAbsolutePath() + "/" + f.getName());
								org.apache.commons.io.FileUtils.copyFile(f, to);
							} catch (IOException e) {
								ErrorUtils.report(logger, e);
							}
						}
					}
				}
				// clean out directories and copy all for dirs in copyAllList
				for (String path : this.copyAllList) {
					File from = new File(this.srcFileApp +"/" + path);
					File to = new File(this.toFileApp + "/" + path);
					try {
						org.apache.commons.io.FileUtils.cleanDirectory(to);
						if (this.debugEnabled) {
							logger.info("Cleaned " + to.getAbsolutePath());
						}
						org.apache.commons.io.FileUtils.copyDirectoryToDirectory(from, to.getParentFile());
						if (this.debugEnabled) {
							logger.info("Copied " + to.getParentFile().getAbsolutePath());
						}
					} catch (IOException e) {
						ErrorUtils.report(logger, e);
					}
				}
				String yearMonth = this.year + "/" + this.month;
				// selectively copy services for specified year/month
				for (String path : this.copyLastMonthList) {
					File from = new File(this.srcFileApp + "/" + path);
					File to = new File(this.toFileApp + "/" + path);
					Collection<File> files = org.apache.commons.io.FileUtils.listFilesAndDirs(
							from
							, new NotFileFilter(TrueFileFilter.INSTANCE)
							, DirectoryFileFilter.DIRECTORY
							);
					for (File f : files) {
						if (f.getAbsolutePath().endsWith(yearMonth)) {
							try {
								to = new File(to.getAbsolutePath() + "/" + yearMonth);
								if (to.exists()) {
									org.apache.commons.io.FileUtils.cleanDirectory(to);
								}
								org.apache.commons.io.FileUtils.copyDirectoryToDirectory(f, to.getParentFile());
								break;
							} catch (IOException e) {
								ErrorUtils.report(logger, e);
							}
						}
					}
				}
				// copy the day indexes that match this year and month
				File copyFromFilePath = new File(this.srcFileApp + "/indexes");
				File copyToFilePath = new File(this.toFileApp + "/indexes");
				String yearMonthName = this.year + this.month;
				List<String> hrefs = new ArrayList<String>();
				Collection<File> files = org.apache.commons.io.FileUtils.listFiles(copyFromFilePath, 
				        new WildcardFileFilter("*", IOCase.INSENSITIVE),
				        new NotFileFilter(DirectoryFileFilter.DIRECTORY));
				for (File f : files) {
					if (f.getName().startsWith(yearMonthName)) {
						try {
							File to = new File(copyToFilePath + "/" + f.getName());
							if (this.debugEnabled) {
								logger.info("Copying " + f.getAbsolutePath());
								logger.info("to " + to.getAbsolutePath());
							}
							org.apache.commons.io.FileUtils.copyFile(f, to);
							hrefs.add(to.getName());
						} catch (IOException e) {
							ErrorUtils.report(logger, e);
						}
					}
				}
				this.updateIndex(hrefs);
			}
			logFileLine.append("\nArchiver ran ");
			logFileLine.append(start.toString());
			logFileLine.append(" to ");
			logFileLine.append(Instant.now().toString());
			String elapsedMessage = this.getElapsedMessage(start);
			logFileLine.append(" " + elapsedMessage);
			this.sendMessage("." + elapsedMessage);
			try {
				org.apache.commons.io.FileUtils.write(logFile, logFileLine.toString(), true);
			} catch (IOException e) {
				ErrorUtils.report(logger, e);
			}
		} else {
			try {
				org.apache.commons.io.FileUtils.write(logFile, Instant.now().toString() + " today is " + dayOfMonth + " so did not archive.  Day of Month must be " + this.archiveDay, true);
			} catch (IOException e) {
				ErrorUtils.report(logger, e);
			}
		}
		this.updateArchiveIndex();
	}
	
	private void updateIndex(List<String> hrefs) {
		File sourceServicesIndex  = new File(this.srcFileApp + "/servicesindex.html");
		File archiveServicesIndex  = new File(this.toFileApp + "/servicesindex.html");
		try {
			Document sourceDoc = Jsoup.parse(sourceServicesIndex, "UTF-8", "http://example.com/");
			Document archiveDoc = Jsoup.parse(archiveServicesIndex, "UTF-8", "http://example.com/");
			Elements sourceDays = sourceDoc.select("a.index-day-link");
			String yearMonth = "indexes/" + this.year + this.month;
			// create a list of elements to add to the archive servicesindex
			Map<String,Element> toAdd = new TreeMap<String,Element>();
			for (Element e : sourceDays) {
				String href = e.attr("href");
				if (href.startsWith(yearMonth)) {
					toAdd.put(href,e.parent().parent());
				}
			}
			/**
			 * Scenarios
			 * 1. No entries for month in archive servicesindex.html.  If so, insert them.
			 * 2. There are entries for the month.  If so, replace them.
			 */
			String targetMonthYear = this.getMonthYear(this.intMonth);
			Elements target = archiveDoc.select("tr.index-month-tr:contains(" + targetMonthYear + ")");
			   Element nextElement = null;
			if (target.size() == 0) { // does not exist.  Get the last entry as the point from which we will start appending
				nextElement = archiveDoc.select("tr.index-day-tr").last();
				Element temp = sourceDoc.select("tr.index-month-tr").first();
				temp.select("span.index-month").first().text(targetMonthYear);
				nextElement.after(temp);
				nextElement = temp;
			} else { // exists.  Need to replace the day rows
			   Element monthElement = target.first();
			   nextElement = monthElement; // we will use this as the starting point to append from
			   boolean notDone = true;
			   while(notDone) {
				   Element nextHref = monthElement.nextElementSibling();
				   if (nextHref == null) {
					   notDone = false;
				   } else {
					   String ref = nextHref.select("a").attr("href");
					   if (ref.contains(yearMonth)) {
						   nextHref.remove();
					   } else {
						   notDone = false;
					   }
				   }
			   }
			}
			// append values.  The nextElement is either the last tr in the index or the 
		   for (Element e : toAdd.values()) {
			   nextElement.after(e);
			   nextElement = e;
		   }
			org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(archiveServicesIndex.getAbsolutePath(), archiveDoc.toString());
		} catch (IOException e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	private String getMonthYear(int month) {
		String result = "";
		switch (month) {
		case (1): {
			result = "January " + this.year;
			break;
		}
		case (2): {
			result = "February " + this.year;
			break;
		}
		case (3): {
			result = "March " + this.year;
			break;
		}
		case (4): {
			result = "April " + this.year;
			break;
		}
		case (5): {
			result = "May " + this.year;
			break;
		}
		case (6): {
			result = "June " + this.year;
			break;
		}
		case (7): {
			result = "July " + this.year;
			break;
		}
		case (8): {
			result = "August " + this.year;
			break;
		}
		case (9): {
			result = "September " + this.year;
			break;
		}
		case (10): {
			result = "October " + this.year;
			break;
		}
		case (11): {
			result = "November " + this.year;
			break;
		}
		case (12): {
			result = "December " + this.year;
			break;
		}
		}
		return result;
	}
	private String getElapsedMessage(Instant start) {
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toHours();
		String elapsedMsg = "" ;
		if (timeElapsed < 1) {
			timeElapsed = Duration.between(start, finish).toMinutes();
			if (timeElapsed < 1) {
				timeElapsed = Duration.between(start, finish).getSeconds();
				elapsedMsg = "Elapsed.seconds=" + timeElapsed;
			} else {
				elapsedMsg = "Elapsed.minutes=" + timeElapsed;
			}
		} else {
			elapsedMsg = "Elapsed.hours=" + timeElapsed;
		}
		return elapsedMsg;
	}
	private void sendMessage(String m) {
		String msg = Instant.now().toString() + " " + m;
		logger.info(msg);
		ArchiverApp.sendMessage(m);
	}

}
