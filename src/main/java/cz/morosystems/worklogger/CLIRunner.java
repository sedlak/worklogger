package cz.morosystems.worklogger;

import cz.morosystems.worklogger.domain.WorkloggerProperties;
import cz.morosystems.worklogger.service.daylogger.DayloggerService;
import cz.morosystems.worklogger.service.synchronizator.SynchronizatorService;
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

  private Options opts;
  private CommandLineParser parser;
  private HelpFormatter formatter;

  public void runWithParams(String[] args) {

    try {
      // parse the command line arguments
      initialize();
      CommandLine cmdLine = parser.parse(opts, args);
      WorkloggerProperties properties = new WorkloggerProperties("wls.properties");
      if (cmdLine.hasOption("logwork")) {
        DayloggerService dayloggerService = new DayloggerService(properties);
        dayloggerService.generateWorkLogs();
      }
      if (cmdLine.hasOption("sync")) {
        SynchronizatorService synchronizator = new SynchronizatorService(properties);
        synchronizator.syncAllDefinedProjects();

      }
      /*if (!cmdLine.hasOption("sync") && cmdLine.hasOption("logwork")) {
        formatter.printHelp("Work Logger", opts);
        System.exit(1);
      }
      */

    } catch (Exception e) {
      logger.error("Oooops, problem!", e);
      System.exit(1);
    }
  }

  private void initialize() {
    opts = new Options().
        addOption("sync", "Synchronize.").
        addOption("logwork", "Run work logging.");
    parser = new DefaultParser();
    formatter = new HelpFormatter();

  }
}
