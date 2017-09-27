package cz.morosystems.worklogger.daylogger;

import cz.morosystems.worklogger.common.CompanySpecifics;
import cz.morosystems.worklogger.common.Worklog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/20/2017.
 */
public class JiraRestManager {
  private static final Logger logger = LoggerFactory.getLogger(JiraRestManager.class);
  private String connectionCredentials;

  JiraRestManager(String connectionCredentials){
    this.connectionCredentials = connectionCredentials;
  }

  public String getWorklogs(CompanySpecifics specifics, String startDay, String endDay) throws IOException {
    logger.info("Getting {} worklogs for {} project", specifics.getPerspective(), specifics.getJiraProjectKey());
    String restUrl = specifics.getJiraUrl()
        //rest api
        + "/rest/tempo-timesheets/3/worklogs/"
        //parameters
        + "?"
        + "dateFrom=" + startDay + "&"
        + "dateTo=" + endDay + "&"
        + "username=" + specifics.getUsername();// + "&"
        //+ "projectKey=" + specifics.getJiraProjectKey();
    logger.info("URL: {}", restUrl);
    return makeHttpGet(restUrl);
  }

  public String makeHttpGet(String restUrl) throws IOException {
  URLConnection connection = new URL(restUrl).openConnection();
  connection.setRequestProperty("Accept-Charset", "UTF-8");
  connection.setRequestProperty("Authorization", "Basic " + connectionCredentials);
  InputStream response = connection.getInputStream();
  String result = IOUtils.toString(response);
  logger.info("Successfully retrieved response");
  logger.debug("JSON: {}", result);
  return result;
}

  public void makeHttpPost(String json, String url) throws IOException {
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

  public void writeWorklog(CompanySpecifics specifics, Worklog worklog) throws IOException {
    String comment = specifics.commentAlsoWithIssueInfo() ?
        worklog.getIssueKey() + " " + worklog.getIssueSummary() + " - " + worklog.getComment() :
        worklog.getComment();

    String dateStarted = worklog.getDateStarted();

    String json = "{"
        + "\"comment\": " + JSONObject.quote(comment) + ","
        + "\"started\": \"" + dateStarted + "\","
        + "\"timeSpentSeconds\": \"" + worklog.getTimeSpentSeconds() + "\""
        + "}";

    logger.info("Creating {} worklog: {}", specifics.getPerspective(), json);
    String url = specifics.getJiraUrl() + "/rest/api/2/issue/" + worklog.getIssueKey() + "/worklog/";
    makeHttpPost(json, url);
  }
}
