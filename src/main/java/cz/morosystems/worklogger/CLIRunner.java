package cz.morosystems.worklogger;

import cz.morosystems.worklogger.daylogger.DayloggerService;
import cz.morosystems.worklogger.synchronizator.SynchronizatorService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
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
			initialize();
			CommandLine cmdLine = parser.parse(opts, args);

			if (cmdLine.hasOption("logwork")) {
				DayloggerService dayloggerService = new DayloggerService("/connection.properties");
				dayloggerService.generateWorkLogs();
			} else if (cmdLine.hasOption("sync")) {
				SynchronizatorService synchronizator = new SynchronizatorService();
				synchronizator.syncAllDefinedProjects();

			} else {
				formatter.printHelp("Work Logger", opts);
			}

		} catch (Exception e) {
			logger.error("Oooops, problem!", e);
			System.exit(1);
		}
	}

	private void initialize(){
		opts = new Options().
					addOption("sync", "Synchronize.").
					addOption("logwork", "Run work logging.");
		parser = new DefaultParser();
		formatter = new HelpFormatter();

	}
}
