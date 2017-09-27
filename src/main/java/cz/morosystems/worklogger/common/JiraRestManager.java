package cz.morosystems.worklogger.common;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/27/2017.
 */
public class JiraRestManager {

  private static final Logger logger = LoggerFactory.getLogger(JiraRestManager.class);

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
    String url =
        specifics.getJiraUrl() + "/rest/api/2/issue/" + worklog.getIssueKey() + "/worklog/";
    new HttpMessages().makeHttpPost(json, url, specifics.getLoginData());
  }

  public HashMap<String, Issue> getWorkingIssuesOfUser(String jiraUri, String credentials)
      throws URISyntaxException {
    logger.info("Connecting to {}", jiraUri);
    //neposielame priamo http request, ale vyuzivame Jira Java Api
    AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    URI serverUri = new URI(jiraUri);
    JiraRestClient restClient = factory
        .create(serverUri, builder -> builder.setHeader("Authorization", "Basic " + credentials));
    SearchRestClient searchClient = restClient.getSearchClient();
    Promise<SearchResult> searchPromise = searchClient
        .searchJql("(status changed TO resolved by currentUser() AFTER startOfDay()) OR "
            + "(status changed to \"In progress\" by currentUser() and assignee = currentUser() and status = \"In progress\")  OR "
            + "(status changed to \"In progress\" by currentUser()  AFTER startOfDay())");
    HashMap<String, Issue> issuesMap = new HashMap<>();
    searchPromise.claim().getIssues().forEach(x -> issuesMap.put(x.getKey(), x));
    return issuesMap;
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
    return new HttpMessages().makeHttpGet(restUrl, specifics.getLoginData());
  }


  }