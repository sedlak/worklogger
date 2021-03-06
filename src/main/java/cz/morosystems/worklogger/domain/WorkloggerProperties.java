package cz.morosystems.worklogger.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class WorkloggerProperties {

	private Properties credentialsProperties;
	private Properties synchronizatorProperties;

	public WorkloggerProperties(String credentialsPropertiesFileName,
								String synchronizatorPropertiesFileName) throws IOException {
		credentialsProperties = loadProperties(credentialsPropertiesFileName);
		synchronizatorProperties = loadProperties(synchronizatorPropertiesFileName);
	}

	private Properties loadProperties(String fileName) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(fileName));
		return properties;
	}

	public Properties getCredentialsProperties(Perspective perspective) {
		return getSpecificProperties(perspective.name() + ".", credentialsProperties);
	}

	public Properties getProjectProperties(int i, Perspective perspective) {
		return getSpecificProperties(i + "." + perspective.name() + ".", synchronizatorProperties);
	}

	private Properties getSpecificProperties(String prefix, Properties properties) {
		Properties prop = new Properties();
		for (String key : properties.stringPropertyNames()) {
			if (key.startsWith(prefix)) {
				prop.setProperty(key.substring(prefix.length()), properties.getProperty(key));
			}
		}
		return prop;
	}

	public int getCountOfProjects() {
		return synchronizatorProperties.keySet().stream().mapToInt(key -> {
			String propKey = (String) key;
			return Integer.valueOf(propKey.split("\\.")[0]);
		}).max().getAsInt();
	}

}
