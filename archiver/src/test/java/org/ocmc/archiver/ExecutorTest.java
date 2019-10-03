package org.ocmc.archiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {
	private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


	public static void main(String[] args) {
			executorService.scheduleAtFixedRate(
					new ZipperTest()
					,0
					, 10
					, TimeUnit.SECONDS
						);
	}

}
