package cz.morosystems.worklogger.domain;

import cz.morosystems.worklogger.domain.Perspective;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class WorkloggerProperties {

  private Properties properties;

  public WorkloggerProperties(String propertiesFileName) throws IOException {
    properties = loadProperties(propertiesFileName);
  }

  private Properties loadProperties(String fileName) throws IOException {
    properties = new Properties();
    properties.load(new FileInputStream(fileName));
    return properties;
  }

  public Properties getSpecificProperties(int sequenceNumber, Perspective perspective) {
    Properties prop = new Properties();
    Enumeration<String> enumeration = (Enumeration<String>) properties.propertyNames();
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(sequenceNumber + "." + perspective)) {
        prop.setProperty(key.split("\\.")[2], properties.getProperty(key));
      }
    }
    return prop;
  }

  public int getCountOfProjects() {
    return properties.keySet().stream().mapToInt(key -> {
      String propKey = (String) key;
      return Integer.valueOf(propKey.split("\\.")[0]);
    }).max().getAsInt();
  }

}
