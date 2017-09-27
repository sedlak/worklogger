package cz.morosystems.worklogger.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/27/2017.
 */
public class HttpMessages {
  private static final Logger logger = LoggerFactory.getLogger(HttpMessages.class);

  public String makeHttpGet(String restUrl, String connectionCredentials) throws IOException {
    URLConnection connection = new URL(restUrl).openConnection();
    connection.setRequestProperty("Accept-Charset", "UTF-8");
    connection.setRequestProperty("Authorization", "Basic " + connectionCredentials);
    InputStream response = connection.getInputStream();
    String result = IOUtils.toString(response);
    logger.info("Successfully retrieved response");
    logger.debug("JSON: {}", result);
    return result;
  }

  public void makeHttpPost(String json, String url, String connectionCredentials) throws IOException {
    //
    logger.info("POST URL: {}", url);
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Authorization", "Basic " + connectionCredentials);
    // Send post request
    con.setDoOutput(true);
    OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
    w.write(json);
    w.close();

    int responseCode = 0;
    try{
      responseCode = con.getResponseCode();
    }catch (IOException e){
      logger.error("Unable to get response code", e);
    }

    String result = null;
    try{
      InputStream response = con.getInputStream();
      result = IOUtils.toString(response);
    }catch (IOException e){
      logger.error("Unable to get response body", e);
    }

    if(responseCode != 201){
      logger.error("Response Code: {}", responseCode);
      logger.error("Response Body: {}", result);
      throw new IOException("Nepodarilo sa zapisat pracu! ResponseCode = " + responseCode);
    }
  }

}
