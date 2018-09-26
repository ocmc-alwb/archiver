package org.ocmc.archiver;

public class ArchiveIndex {
	StringBuffer sb = new StringBuffer();
	static enum ZIP_TYPES {ALL, AUDIO, CLIENT, MEDIA};
	static String yearRowPre = "<tr class='index-day-tr'><td class='index-day-td'><a class='index-day-link' href='../../archive";
	static String yearRowPost = "/dcs/dcs.html'>";
	static String yearRowSuffix = "</a></td></tr>";
	static String zipRowPre = "<tr class='index-day-tr'><td class='index-day-td'><a class='index-day-link' href='../../archive/";
	static String zipRowPost = "'>";
	
	public ArchiveIndex() {
		this.setContent(getInstructionsFile());
	}

	public ArchiveIndex(String instructions) {
		this.setContent(instructions);
	}
	
	private void setContent(String instructions) {
		sb.append("<!DOCTYPE html>");
		sb.append("\n<html>");
		sb.append("\n<head>");
		sb.append("\n<base href='../public/dcs/'>");
		sb.append("\n<!-- Meta Declarations -->");
		sb.append("\n<meta charset='utf-8'/>");
		sb.append("\n<meta name='viewport' content='width=device-width'/>");
		sb.append("\n<!-- Stylesheets -->");
		sb.append("\n<link href='css/font-awesome/css/font-awesome.css' rel='stylesheet'/>");
		sb.append("\n<link rel='stylesheet' type='text/css' href='js/lib/bootstrap/css/bootstrap.css'/>");
		sb.append("\n<link type='text/css' rel='stylesheet' href='css/jquery.dropdown.css' />");
		sb.append("\n<link rel='stylesheet' type='text/css' href='css/alwb.css'/>");
		sb.append("\n<style>#instructionsTable > tbody {float:left;width: 50%;}td.zip-how-to-td-left {width:20%; padding: 20px;text-align:left;}td.zip-how-to-td-right {width: 80%; padding: 20px; text-align:left;}</style>");
		sb.append("\n");
		sb.append("\n<!-- Scripts -->");
		sb.append("\n<script data-main='js/app' src='js/lib/require.js'></script>");
		sb.append("\n");
		sb.append("\n<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->");
		sb.append("\n<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->");
		sb.append("\n<!--[if lt IE 9]>");
		sb.append("\n<script src='https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js'></script>");
		sb.append("\n<script src='https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js'></script>");
		sb.append("\n<script src='https://code.jquery.com/jquery-3.3.1.min.js'   integrity='sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=' crossorigin='anonymous'></script><script> $(function(){ $('#includedContent').load('../../archive/instructions.html'); }); </script>   ");
		sb.append("\n<![endif]-->");
		sb.append("\n</head>");
		sb.append("\n<body>");
		sb.append("\n<div class='agesMenu'>");
		sb.append("\n<!-- Menu Bar Icons-->");
		sb.append("\n<a href='#' data-jqm-dropdown='#jqm-dropdown-pages' title='Go to...'><i class='fa fa-bars ages-menu-link'></i></a>");
		sb.append("\n<!-- Dropdown Menu Contents -->");
		sb.append("\n<div id='jqm-dropdown-pages' class='jqm-dropdown jqm-dropdown-tip'>");
		sb.append("\n<ul class='jqm-dropdown-menu jqm-dropdown-relative'>");
		sb.append("\n<li><a href='servicesindex.html'><i class='fa fa-calendar'>&nbsp;Daily Services by Date</i></a></li>");
		sb.append("\n<li><a href='booksindex.html'><i class='fa fa-arrows'>&nbsp;Sacraments and Services</i></a></li>");
		sb.append("\n<li><a href='customindex.html'><i class='fa fa-list-alt'>&nbsp;Additional Texts Music</i></a></li>");
		sb.append("\n<li class='jqm-dropdown-divider'></li>");
		sb.append("\n<li><a href='about.html'><i class='fa fa-info-circle'>&nbsp;About</i></a></li>");
		sb.append("\n<li><a href='contact.html'><i class='fa fa-envelope'>&nbsp;Contact</i></a></li>");
		sb.append("\n<li><a href='donate.html'><i class='fa fa-money'>&nbsp;Donate</i></a></li>");
		sb.append("\n<li class='jqm-dropdown-divider'></li>");
		sb.append("\n<li><a href='javascript:(showInfo())'><i class='fa fa-laptop'>&nbsp;Browser Information</i></a></li>");
		sb.append("\n<li class='jqm-dropdown-divider'></li>");
		sb.append("\n<li><a href='help.html'><i class='fa fa-question-circle'>&nbsp;Help</i></a></li>");
		sb.append("\n</ul>");
		sb.append("\n</div>");
		sb.append("\n</div>");
		sb.append("\n<div class='index-content'>");
		sb.append("\n<h1 class='index-title'>AGES Digital Chant Stand<br>Archive Index</h1>");
		// year table
		sb.append("\n<p class='index-service-day'>Online Access by Year</p>");
		sb.append("\n<table id='yearTable' class='services-index-table'>");
		sb.append("	<tr class='index-day-tr'><td class='index-day-td'><span class='index-day'>Creating Archive files.  Please check back in a few minutes.</span></td></tr>");
		sb.append("\n</table>");
		sb.append("\n<p class='source'><span id='yearSpan'</span></p><br/><br/>");
		// zip table
		sb.append("\n<p class='index-service-day'>Zips of the Current Version of DCS</p>");
		sb.append("\n<table id='zipTable' class='services-index-table'>");
		sb.append("	<tr class='index-day-tr'><td class='index-day-td'><span class='index-day'>Creating Zip files.  Please check back in a few minutes.</span></td></tr>");
		sb.append("\n</table>");
		sb.append("\n<p class='source'><span id='zipSpan'</span></p><br/><br/>");
		// instructions 
		sb.append("<div id='includedContent'></div>");
//		sb.append(instructions);
		sb.append("\n</body>");
		sb.append("\n</html>");
	}
	
	public String getYearRowHtml(String year) {
		StringBuffer result = new StringBuffer();
		result.append(yearRowPre);
		result.append(year);
		result.append(yearRowPost);
		result.append(year);
		result.append(yearRowSuffix);
		return result.toString();
	}
	
	public String getZipRowHtml(
			ArchiveIndex.ZIP_TYPES type
			, String href
			, String size
			) {
		StringBuffer result = new StringBuffer();
		result.append(zipRowPre);
		result.append(href);
		result.append(zipRowPost);
		switch(type) {
		case ALL:
			result.append("DCS - All");
			break;
		case AUDIO:
			result.append("Audio");
			break;
		case CLIENT:
			result.append("DCS");
			break;
		case MEDIA:
			result.append("Media");
			break;
		default:
			result.append("Unknown zip file type");
			break;
		}
		result.append("</a>");
		result.append(" (");
		result.append(size);
		result.append(")");
		result.append("</td></tr>");
		return result.toString();
	}

	public String getEmptyTable() {
		return sb.toString();
	}

	public static String getYearRowPre() {
		return yearRowPre;
	}

	public static void setYearRowPre(String yearRowPre) {
		ArchiveIndex.yearRowPre = yearRowPre;
	}

	public static String getYearRowPost() {
		return yearRowPost;
	}

	public static void setYearRowPost(String yearRowPost) {
		ArchiveIndex.yearRowPost = yearRowPost;
	}

	public static String getYearRowSuffix() {
		return yearRowSuffix;
	}

	public static void setYearRowSuffix(String yearRowSuffix) {
		ArchiveIndex.yearRowSuffix = yearRowSuffix;
	}

	public static String getZipRowPre() {
		return zipRowPre;
	}

	public static void setZipRowPre(String zipRowPre) {
		ArchiveIndex.zipRowPre = zipRowPre;
	}

	public static String getZipRowPost() {
		return zipRowPost;
	}

	public static void setZipRowPost(String zipRowPost) {
		ArchiveIndex.zipRowPost = zipRowPost;
	}
	
	public static String getInstructionsFile() {
		StringBuffer sbi = new StringBuffer();
		sbi.append("\n<p class='index-service-day'>Instructions for Using the Zip Files</p>");
		sbi.append("\n<div class='zip-instructions'>");
		sbi.append("\n\t<div class='zip-dcs'>");
		sbi.append("\n\t\t<div class='zip-instructions-title'>DCS</div>");
		sbi.append("\n\t\t<div class='zip-instructions-line-1'>Use this link to get the current DCS only, without scores and audio.</div>");
		sbi.append("\n\t\t\t<ol class='zip-instructions-list'>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Download the zip and extract it.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>When you extract it, a new directory will be created.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Delete the <em>ages-dcs.zip</em> file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>In the new directory, open the file called <em>index.html</em> in your web browser.</li>");
		sbi.append("\n\t\t\t</ol>");
		sbi.append("\n\t</div>");
		sbi.append("\n\t<div class='zip-media'>");
		sbi.append("\n\t\t<div class='zip-instructions-title'>Media</div>");
		sbi.append("\n\t\t<div class='zip-instructions-line-1'>Use this link to add all scores to your DCS.</div>");
		sbi.append("\n\t\t\t<ol class='zip-instructions-list'>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Download the Media zip file into the directory that was created when you unzipped the DCS file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Unzip the <em>ages-media.zip</em> file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Delete the <em>ages-media.zip</em> file.</li>");
		sbi.append("\n\t\t\t</ol>");
		sbi.append("\n\t</div>");
		sbi.append("\n\t<div class='zip-audio'>");
		sbi.append("\n\t\t<div class='zip-instructions-title'>Audio</div>");
		sbi.append("\n\t\t<div class='zip-instructions-line-1'>Use this link to add all audio to your DCS.</div>");
		sbi.append("\n\t\t\t<ol class='zip-instructions-list'>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Download the Audio zip file into the directory that was created when you unzipped the DCS file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Unzip the <em>ages-audio.zip</em> file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Delete the <em>ages-audio.zip</em> file.</li>");
		sbi.append("\n\t\t\t</ol>");
		sbi.append("\n\t</div>");
		sbi.append("\n\t<div class='zip-all'>");
		sbi.append("\n\t\t<div class='zip-instructions-title'>All</div>");
		sbi.append("\n\t\t<div class='zip-instructions-line-1'>Use this link to download the DCS, media, and audio as a single zip.</div>");
		sbi.append("\n\t\t\t<ol class='zip-instructions-list'>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Download the DCS - All zip.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Unzip the <em>ages-dcs-all.zip</em> file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>When you extract it, a new directory will be created.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>Delete the <em>ages-dcs-all.zip</em> file.</li>");
		sbi.append("\n\t\t\t\t<li class='zip-instructions-li'>In the new directory, open the file called <em>index.html</em> in your web browser.</li>");
		sbi.append("\n\t\t\t</ol>");
		sbi.append("\n\t</div>");
		sbi.append("\n</div>\n");
		return sbi.toString();
	}


}
