package cz.morosystems.worklogger;

//@SpringBootApplication
public class WorkloggerApplication {

	public static void main(String[] args) {
		//		Zatial nepotrebujeme SPRING, tak to zatial mozme spravit po svojom
		//		ConfigurableApplicationContext context = SpringApplication.run(WorkloggerApplication.class, args);

		CLIRunner cli = new CLIRunner();
		cli.runWithParams(args);

	}
}
