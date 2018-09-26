package org.ocmc.archiver;

import java.io.File;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DumpCsv {

	public static void main(String[] args) {
		String in = "/volumes/ssd2/neo4j/3.4.7/bin/en_uk_gesot.csv";
		JsonParser jsonParser = new JsonParser();
		for (String line : org.ocmc.ioc.liturgical.utils.FileUtils.linesFromFile(new File(in))) {
			if (! line.startsWith("\"properties(n)")) {
				String newLine = line.replaceAll("\"\"", "\"");
				newLine = newLine.substring(1, newLine.length()-1);
				JsonObject o = jsonParser.parse(newLine).getAsJsonObject();
				System.out.println(o.toString());
			}
		}
	}

}
