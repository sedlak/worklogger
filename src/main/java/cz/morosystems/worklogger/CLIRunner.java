package cz.morosystems.worklogger;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pavol Sedlak
 */
public class CLIRunner {

	private static final Logger logger = LoggerFactory.getLogger(CLIRunner.class);

	public void runWithParams(String[] args){
		try {

			WorkLogSynchronizator synchronizator = new WorkLogSynchronizator();
			synchronizator.syncAllDefinedProjects();

		}catch (IOException e) {
			logger.error("Oooops, problem!", e);
		}
	}

}
