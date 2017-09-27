package cz.morosystems.worklogger;

import cz.morosystems.worklogger.daylogger.UserWorkManager;
import cz.morosystems.worklogger.synchronizator.WorkLogSynchronizator;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pavol Sedlak
 */
public class CLIRunner {

	private static final Logger logger = LoggerFactory.getLogger(CLIRunner.class);

	private Options opts;//  = new Options().addOption("sync", "Synchronize.").addOption("logwork", "Run work logging.");
	private CommandLineParser parser;// = new DefaultParser();
	private HelpFormatter formatter;// = new HelpFormatter();

	public void runWithParams(String[] args) {

		try {
			// parse the command line arguments
			CommandLine cmdLine = parser.parse(opts, args);

			if (cmdLine.hasOption("logwork")) {
				UserWorkManager userWorkManager = new UserWorkManager("/connection.properties");
				userWorkManager.generateWorkLogs();
			} else if (cmdLine.hasOption("sync")) {
				WorkLogSynchronizator synchronizator = new WorkLogSynchronizator();
				synchronizator.syncAllDefinedProjects();

			} else {
				formatter.printHelp("Work Logger", opts);
			}

		} catch (Exception e) {
			logger.error("Oooops, problem!", e);
			// nothing more to do about it
			// exit with error code 1
			System.exit(1);
		}
	}
}
