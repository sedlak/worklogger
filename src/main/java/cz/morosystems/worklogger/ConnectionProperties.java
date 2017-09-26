package cz.morosystems.worklogger;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/18/2017.
 */
public class ConnectionProperties {
  private static final Logger logger = LoggerFactory.getLogger(ConnectionProperties.class);
  private String uri;
  private String credentials;

  public ConnectionProperties(String fileName){
    Properties prop = loadProperties(fileName);
    this.uri = prop.getProperty("uri");
    this.credentials = prop.getProperty("credentials");
  }

  public String getUri() {
    return uri;
  }



  public String getCredentials() {
    return credentials;
  }



  private Properties loadProperties(String propertiesFilename) {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getResourceAsStream(propertiesFilename));
    } catch (IOException e) {
      logger.error("Prolem with connection properties file read", e);
    }
    return properties;
  }

}
