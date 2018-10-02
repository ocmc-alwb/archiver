package org.ocmc.archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jgit.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archives files
 * 
 * Modified from www.mkyong.com/java/how-to-compress-files-in-zip-format
 * @author mac002
 *
 */
public class Zipper implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Zipper.class);
	ArchiveIndex indexUtils = new ArchiveIndex();

	private static final String redirectHtml = "<html><head><title>AGES DCS Archive Redirect</title><meta http-equiv='refresh' content='0; URL=public/dcs/dcs.html'><meta name='keywords' content='automatic redirection'></head><body>Redirecting to the archive index.</body></html>";
	private static final String indexHtml = "<html><head><title>AGES DCS Archives</title></head><body></body></html>";
	String pathRoot = "";
	File fileRoot;
	int fileRootLength = 0;
	String dirSite = "";
	File srcFileSite;
	String dirClient = "";
	File srcFileClient;
	String srcPathClientFile = "";
	String dirApp = "";
	File srcFileApp;
	String dirArchive = "archive";
	File toFileArchive;
	String dirAudio = "";
	File srcFileAudio;
	String srcPathAudioFile = "";
	String dirMedia = "";
	File srcFileMedia;
	String srcPathMediaFile = "";
	File archiveFile = null;
	File toFileArchiveSite = null;
	File redirectFile = null;
	String redirectFileName = "index.html";
	String pathInstructionsFile = "";
	File srcFileInstructions = null;
	boolean debugEnabled = false;
	boolean zipAll = false;
	boolean zipAudio = false;
	boolean zipClient = false;
	boolean zipMedia = false;
	
	File archiveIndexFile = null;
	
    List<String> fileList = new ArrayList<String>();
    List<String> fileListAudio = new ArrayList<String>();
    List<String> fileListClient = new ArrayList<String>();
    List<String> fileListMedia = new ArrayList<String>();
    
    List<String> zipHtmlTableRows = new ArrayList<String>();
	
	public Zipper(
			String pathRoot
			, String dirSite
			, String dirClient
			, String dirApp
			, String dirAudio
			, String dirMedia
			, String pathInstructionsFile
			, boolean zipAll
			, boolean zipAudio
			, boolean zipClient
			, boolean zipMedia
			, boolean debugEnabled
			) {
		super();
		this.debugEnabled = debugEnabled;
		this.pathRoot = this.slash(pathRoot);
		this.dirApp = this.slash(dirApp);
		this.dirAudio = this.slash(dirAudio);
		this.dirClient = this.slash(dirClient);
		this.dirMedia = this.slash(dirMedia);
		this.dirSite = this.slash(dirSite);
		this.pathInstructionsFile = pathInstructionsFile;
		this.zipAll = zipAll;
		this.zipAudio = zipAudio;
		this.zipClient = zipClient;
		this.zipMedia = zipMedia;
		this.fileRoot = new File(this.pathRoot); // ~/html
		this.fileRootLength = this.fileRoot.getAbsolutePath().length();
		this.srcFileSite = new File(this.fileRoot.getAbsolutePath() + "/" + this.dirSite); // ~/html/dcs
		this.srcFileAudio = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirAudio);// ~/html/dcs/public/a
		this.srcFileClient = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirClient); // ~/html/dcs/public
		this.srcFileMedia = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirMedia); // ~/html/dcs/public/m
		this.srcFileApp = new File(this.srcFileClient.getAbsolutePath() + "/" + this.dirApp); // ~/html/dcs/public/dcs
		this.toFileArchive = new File(this.srcFileSite.getAbsolutePath() + "/" + this.dirArchive); // ~/html/dcs/archive
		this.fileRootLength = this.srcFileSite.getAbsolutePath().length();
		this.archiveIndexFile = new File(this.srcFileSite + "/" + this.dirArchive + "/index.html");
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
		this.reportFiles();
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
	
	private void updateArchiveIndex() {
		try {
			Document doc = Jsoup.parse(this.archiveIndexFile, "UTF-8", "http://example.com/");
			Element zipTable = doc.select("table#zipTable").first();
			Element zipSpan = doc.select("span#zipSpan").first();
			zipSpan.text("Zips last updated " + Instant.now().toString() + " (GMT)");
			zipTable.children().remove();
			for (String row : this.zipHtmlTableRows) {
				zipTable.append(row);
			}
			org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(
					this.archiveIndexFile.getAbsolutePath()
					, doc.toString()
					);
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}

	private void reportFiles() {
		if (this.debugEnabled) {
			logger.info("Root = " + this.fileRoot.getAbsolutePath());
			logger.info("Site = " + this.srcFileSite.getAbsolutePath());
			logger.info("Audio = " + this.srcFileAudio.getAbsolutePath());
			logger.info("Client = " + this.srcFileClient.getAbsolutePath());
			logger.info("Media = " + this.srcFileMedia.getAbsolutePath());
		}
	}
	private String slash(String path) {
		if (path.endsWith("/")) {
			return path;
		} else {
			return path + "/";
		}
	}
	
	@Override
	public void run() {
		Instant start = Instant.now();
		File logFile = new File(this.toFileArchive.getAbsolutePath() + "/" + "zipper.log");
		if (! this.toFileArchive.exists()) {
			try {
				FileUtils.mkdirs(this.toFileArchive);
			} catch (IOException e) {
				ErrorUtils.report(logger, e);
			}
		}
		logger.info("Starting zip");
		this.initializeArchiveIndex();

		StringBuffer logFileLine = new StringBuffer();
			StringBuffer result = new StringBuffer();
			try {
				if (this.zipAudio || this.zipAll) {
					this.fileListAudio = this.generateFileList(this.srcFileAudio, this.fileListAudio);
				}
				if (this.zipClient || this.zipAll) {
					this.fileListClient = this.generateFileList(this.srcFileClient, this.fileListClient);
					this.redirectFile = new File(this.toFileArchive.getAbsolutePath() + "/temp/" + this.redirectFileName);
					org.ocmc.ioc.liturgical.utils.FileUtils.writeFile(redirectFile.getAbsolutePath(), redirectHtml);
					this.fileListClient.add(this.redirectFileName);
				}
				
				if (this.zipMedia || this.zipAll) {
					this.fileListMedia = this.generateFileList(this.srcFileMedia, this.fileListMedia);
				}
				
				if (this.zipAll) {
					this.fileList.addAll(this.fileListAudio);
					this.fileList.addAll(this.fileListClient);
					this.fileList.addAll(this.fileListMedia);
				}
				
				File zip = null;
				
				if (this.zipClient) {
					zip = new File(this.toFileArchive.getAbsolutePath()  + "/" + Constants.zipDcs);
					if (zip.exists()) {
						try {
							zip.delete();
						} catch (Exception e) {
							ErrorUtils.report(logger, e);
						}
					}
					this.zipIt(zip.getAbsolutePath(), this.fileListClient);
					String size = org.apache.commons.io.FileUtils.byteCountToDisplaySize(org.apache.commons.io.FileUtils.sizeOf(zip));
					this.zipHtmlTableRows.add(
							this.indexUtils.getZipRowHtml(
									ArchiveIndex.ZIP_TYPES.CLIENT
									, zip.getName()
									, size
									)
							);
				}
				if (this.zipMedia) {
					zip = new File(this.toFileArchive.getAbsolutePath()  + "/" + Constants.zipMedia);
					if (zip.exists()) {
						try {
							zip.delete();
						} catch (Exception e) {
							ErrorUtils.report(logger, e);
						}
					}
					this.zipIt(zip.getAbsolutePath(), this.fileListMedia);
					String size = org.apache.commons.io.FileUtils.byteCountToDisplaySize(org.apache.commons.io.FileUtils.sizeOf(zip));
					this.zipHtmlTableRows.add(
							this.indexUtils.getZipRowHtml(
									ArchiveIndex.ZIP_TYPES.MEDIA
									, zip.getName()
									, size
									)
							);
				}
				if (this.zipAudio) {
					zip = new File(this.toFileArchive.getAbsolutePath()  + "/" + Constants.zipAudio);
					if (zip.exists()) {
						try {
							zip.delete();
						} catch (Exception e) {
							ErrorUtils.report(logger, e);
						}
					}
					this.zipIt(zip.getAbsolutePath(), this.fileListAudio);
					String size = org.apache.commons.io.FileUtils.byteCountToDisplaySize(org.apache.commons.io.FileUtils.sizeOf(zip));
					this.zipHtmlTableRows.add(
							this.indexUtils.getZipRowHtml(
									ArchiveIndex.ZIP_TYPES.AUDIO
									, zip.getName()
									, size
									)
							);
				}
				if (this.zipAll) {
					zip = new File(this.toFileArchive.getAbsolutePath()  + "/" + Constants.zipAll);
					if (zip.exists()) {
						try {
							zip.delete();
						} catch (Exception e) {
							ErrorUtils.report(logger, e);
						}
					}
					this.zipIt(zip.getAbsolutePath(), this.fileList);
					String size = org.apache.commons.io.FileUtils.byteCountToDisplaySize(org.apache.commons.io.FileUtils.sizeOf(zip));
					this.zipHtmlTableRows.add(
							this.indexUtils.getZipRowHtml(
									ArchiveIndex.ZIP_TYPES.ALL
									, zip.getName()
									, size
									)
							);
				}
				try {
					if (this.redirectFile != null) {
						this.redirectFile.delete();
						this.redirectFile.getParentFile().delete();
					}
				} catch (Exception e) {
				}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		if (this.debugEnabled) {
			logger.info(result.toString());
		}
		this.updateArchiveIndex();
		logFileLine.append("\nZipper ran ");
		logFileLine.append(start.toString());
		logFileLine.append(" to ");
		logFileLine.append(Instant.now().toString());
		String elapsedMessage = this.getElapsedMessage(start);
		logFileLine.append(" " + elapsedMessage);
		logger.info("Finished zipping...");
		try {
			org.apache.commons.io.FileUtils.write(logFile, logFileLine.toString(), true);
		} catch (IOException e) {
			ErrorUtils.report(logger, e);
		}
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
	
	    /**
	     * Zip it
	     * @param zipFile output ZIP file location
	     * @param fileList list of files to add
	     */
	    public void zipIt(String zipFile, List<String> fileList){

	     byte[] buffer = new byte[1024];
	    	
	     try{
	    	 
	    	 String sourceDir = this.srcFileSite.getAbsolutePath();
	    		
	    	FileOutputStream fos = new FileOutputStream(zipFile);
	    	ZipOutputStream zos = new ZipOutputStream(fos);

	    	if (this.debugEnabled) {
		    	logger.info("Output to Zip : " + zipFile);
	    	}
	    		
	    	for(String file : fileList){
	    		if (file.equals(this.redirectFileName)) { // this is the redirect html file
	    			sourceDir = this.redirectFile.getParent();
	    		}
	    		if (this.debugEnabled) {
		    		logger.info("File Added : " + file);
	    		}
	    		ZipEntry ze= new ZipEntry(file);
	        	zos.putNextEntry(ze);
	               
	        	FileInputStream in = new FileInputStream(sourceDir + "/" + file);
	       	   
	        	int len;
	        	while ((len = in.read(buffer)) > 0) {
	        		zos.write(buffer, 0, len);
	        	}
	               
	        	in.close();
	    		if (file.equals(this.redirectFileName)) { // this is the redirect html file
	    			sourceDir = this.srcFileSite.getAbsolutePath(); // put the sourceDir back to what it should be
	    		}

	    	}
	    		
	    	zos.closeEntry();
	    	zos.close();
	
	    }catch(IOException ex){
	       ex.printStackTrace();   
	    }
	   }
	    
	    /**
	     * Traverse a directory and get all files,
	     * and add the file into fileList  
	     * @param node file or directory
	     * @param fileList the file list we are generating
	     */
	    public List<String> generateFileList(File node, List<String> fileList){

	    	//add file only
		if(node.isFile()){
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}
			
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				generateFileList(new File(node, filename), fileList);
			}
		}
		return fileList;
	    }

	    /**
	     * Format the file path for zip
	     * @param file file path
	     * @return Formatted file path
	     */
	    private String generateZipEntry(String file){
	    	String result = file.substring(this.fileRootLength+1, file.length());
	    	return result;
	    }

}
