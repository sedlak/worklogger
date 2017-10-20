package cz.morosystems.worklogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.morosystems.worklogger.domain.WorkloggerProperties;
import cz.morosystems.worklogger.service.daylogger.DayloggerService;
import cz.morosystems.worklogger.service.synchronizator.SynchronizatorService;

/**
 * @author Pavol Sedlak
 */
public class CLIRunner {

	public static final String CLI_OPTION_SYNC = "sync";
	public static final String CLI_OPTION_DAYLOG = "daylog";
	private static final Logger logger = LoggerFactory.getLogger(CLIRunner.class);
	private Options opts;
	private CommandLineParser parser;
	private HelpFormatter formatter;

	public void runWithParams(String[] args) {

		try {
			// parse the command line arguments
			initialize();
			CommandLine cmdLine = parser.parse(opts, args);
			WorkloggerProperties properties = new WorkloggerProperties(
				"worklogger-jira-credentials.properties", "worklogger-jira-synchronizator.properties");

			if (cmdLine.hasOption(CLI_OPTION_DAYLOG)) {
				DayloggerService dayloggerService = new DayloggerService(properties);
				dayloggerService.generateWorkLogs();
			}

			if (cmdLine.hasOption(CLI_OPTION_SYNC)) {
				SynchronizatorService synchronizator = new SynchronizatorService(properties);
				synchronizator.syncAllDefinedProjects();

			}

			if (!cmdLine.hasOption(CLI_OPTION_SYNC)
				&& !cmdLine.hasOption(CLI_OPTION_DAYLOG)) {
				formatter.printHelp("worklogger", opts);
				System.exit(1);
			}

		} catch (Exception e) {
			logger.error("Oooops, problem!", e);
			System.exit(1);
		}
	}

	private void initialize() {
		opts = new Options().
			addOption(CLI_OPTION_DAYLOG, "Create worklogs based on todays JIRA activity").
			addOption(CLI_OPTION_SYNC, "Synchronize worklogs between JIRA instances");
		parser = new DefaultParser();
		formatter = new HelpFormatter();
	}
}
