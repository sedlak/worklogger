package cz.morosystems.worklogger.common;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/27/2017.
 */
public class WorklogManipulation {
  private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


  public boolean worklogCompare(Worklog primaryWorklog, Worklog mirrorWorklog, CompanySpecifics mirrorSpecifics) {
    if(mirrorSpecifics.commentAlsoWithIssueInfo()){
      return (primaryWorklog.getTimeSpentSeconds() == mirrorWorklog.getTimeSpentSeconds()
          && mirrorWorklog.getComment().contains(primaryWorklog.getIssueKey()));
    }else{
      return (primaryWorklog.getTimeSpentSeconds() == mirrorWorklog.getTimeSpentSeconds());
      //&& mirrorWorklog.getComment().contains(primaryWorklog.getComment());
    }
  }

  public Worklog createWorklogFromJson(JSONObject w) {
    return new Worklog(
        w.getJSONObject("issue").getString("key"),
        w.getJSONObject("issue").getString("summary"),
        w.getString("dateStarted"),
        w.getString("comment"),
        w.getInt("timeSpentSeconds")
    );
  }

  public Worklog createWorkLogFromIssue(Issue issue, int time) {
    return new Worklog(
        issue.getKey(), //key
        issue.getSummary(), //summary
        ZonedDateTime.now().format(dateTimeFormat), //dateStarted TODO: aky cas?
        "Implementation.",
        time); //timeSpentSeconds

  }


  /**
   * Created by moro on 9/20/2017.
   */

}
