package cz.morosystems.worklogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@SpringBootApplication
public class WorkloggerApplication {

	private static final Logger logger = LoggerFactory.getLogger(WorkloggerApplication.class);

	public static void main(String[] args) {
//		Zatial nepotrebujeme SPRING, tak to zatial mozme spravit po svojom
//		ConfigurableApplicationContext context = SpringApplication.run(WorkloggerApplication.class, args);

		CLIRunner cli = new CLIRunner();
		cli.runWithParams(args);

	}
}
