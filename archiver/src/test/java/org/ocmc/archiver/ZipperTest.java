package org.ocmc.archiver;

import java.time.Instant;

public class ZipperTest implements Runnable {
	String start = "";
	
	public ZipperTest() {
		this.start = Instant.now().toString();
	}
	@Override
	public void run() {
		System.out.println(this.start);
	}
}
