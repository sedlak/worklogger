package cz.morosystems.worklogger.common;

import com.atlassian.jira.rest.client.api.domain.Issue;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class WorklogService {
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


}
