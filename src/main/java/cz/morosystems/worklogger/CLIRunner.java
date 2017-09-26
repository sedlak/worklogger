package cz.morosystems.worklogger;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pavol Sedlak
 */
public class CLIRunner {

	private static final Logger logger = LoggerFactory.getLogger(CLIRunner.class);
	Options opts;
	CommandLine cmdLine;

	private void initializeOptions(){

		opts = new Options();
		opts.addOption("sync", "Synchronize.");
		opts.addOption("logwork", "Run work logging.");

	}

	public void runWithParams(String[] args){
		initializeOptions();
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		try {
			// parse the command line arguments
			cmdLine = parser.parse( opts, args );

			if(cmdLine.hasOption("logwork")){
				UserWorkManager um = new UserWorkManager("/connection.properties");
				um.generateWorkLogs();
			}
			else if (cmdLine.hasOption("sync")) {
				WorkLogSynchronizator synchronizator = new WorkLogSynchronizator();
				synchronizator.syncAllDefinedProjects();

			}
			else{
				formatter.printHelp( "Work Logger", opts );
			}

		}catch (IOException e) {
			logger.error("Oooops, problem!", e);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

}
