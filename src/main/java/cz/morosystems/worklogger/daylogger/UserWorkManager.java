package cz.morosystems.worklogger.daylogger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import cz.morosystems.worklogger.common.CompanySpecifics;
import cz.morosystems.worklogger.common.JiraRestManager;
import cz.morosystems.worklogger.common.Perspective;
import cz.morosystems.worklogger.common.Worklog;
import cz.morosystems.worklogger.common.WorklogManipulation;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by moro on 9/18/2017.
 */
public class UserWorkManager {

  CompanySpecifics primarySpecific;
  JiraRestManager jiraRestManager;

  public UserWorkManager(String connectionPropertiesFileName)
      throws URISyntaxException, IOException {
    primarySpecific = new CompanySpecifics(1, Perspective.PRIMARY,
        loadProperties(connectionPropertiesFileName));
    jiraRestManager = new JiraRestManager();

  }


  private void createFullWorkLogs(HashMap<String, Issue> workingIssuesOfUser) throws Exception {
    List<Integer> timeSpent = computeSpentTime(8, workingIssuesOfUser.size());
    ArrayList<Issue> listWorkingIssuesOfUser = new ArrayList<>(workingIssuesOfUser.values());
    for (int i = 0; i < listWorkingIssuesOfUser.size(); i++) {
      Worklog worklog = new WorklogManipulation()
          .createWorkLogFromIssue(listWorkingIssuesOfUser.get(i),
              timeSpent.get(i) * 60 * 60);
      jiraRestManager.writeWorklog(primarySpecific, worklog);
    }
  }

  private List<Integer> computeSpentTime(int freeTime, int issueCount) throws Exception {
    int base = freeTime / issueCount;
    if (base == 0) {
      throw new Exception("Ooops, je viac issues ako casu na ne!");
    }
    int mod = freeTime % issueCount;
    ArrayList<Integer> times1 = new ArrayList<Integer>(Collections.nCopies(mod, (base + 1)));
    ArrayList<Integer> times2 = new ArrayList<Integer>(Collections.nCopies(issueCount - mod, base));
    times1.addAll(times2);
    return times1;
  }

  private void createReducedWorkLogs(HashMap<String, Worklog> issuesOfUserFromWorklog,
      HashMap<String, Issue> issuesOfUserWorkingOn)
      throws Exception {
    for (String issueId : issuesOfUserFromWorklog.keySet()) {
      issuesOfUserWorkingOn.remove(issueId);
    }
    int spentTime = issuesOfUserFromWorklog.values().stream()
        .mapToInt((list -> list.getTimeSpentSeconds())).sum();
    List<Integer> timeSpent = computeSpentTime(8 - (spentTime / (60 * 60)),
        issuesOfUserWorkingOn.size()); //TODO: je to nepresne
    ArrayList<Issue> issues = new ArrayList<Issue>(issuesOfUserWorkingOn.values());
    for (int i = 0; i < issues.size(); i++) {
      Worklog worklog = new WorklogManipulation()
          .createWorkLogFromIssue(issues.get(i), timeSpent.get(i) * 60 * 60);
      jiraRestManager.writeWorklog(primarySpecific, worklog);
    }
  }

  private HashMap<String, Worklog> getWorklogsOfUser(LocalDate startDate, LocalDate endDate)
      throws IOException {
    String start;
    String end;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    if (startDate == null || endDate == null) { //TODO: podmienka nie je uplne korektna
      start = LocalDate.now().format(dateFormatter);
      end = start;
    } else {
      start = startDate.format(dateFormatter);
      end = endDate.format(dateFormatter);
    }

    String jsonString = jiraRestManager.getWorklogsFromTimeSheet(primarySpecific, start, end);
    JSONArray worklogsArray = new JSONArray(jsonString);
    HashMap<String, Worklog> issueMap = new HashMap<>();
    for (int i = 0; i < worklogsArray.length(); i++) {
      JSONObject w = worklogsArray.getJSONObject(i);
      Worklog worklog = new WorklogManipulation().createWorklogFromJson(w);
      issueMap.put(worklog.getIssueKey(), worklog);
    }
    return issueMap;

  }

  public void generateWorkLogs() throws Exception {
    HashMap<String, Worklog> worklogsOfUser = getWorklogsOfUser(null, null);
    if (worklogsOfUser.size() == 0) {
      createFullWorkLogs(jiraRestManager
          .getWorkingIssuesOfUser(primarySpecific.getJiraUrl(), primarySpecific.getLoginData()));
    } else {
      createReducedWorkLogs(getWorklogsOfUser(null, null), jiraRestManager
          .getWorkingIssuesOfUser(primarySpecific.getJiraUrl(), primarySpecific.getLoginData()));
    }

  }


  private Properties loadProperties(String propertiesFileName) throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(propertiesFileName));
    return props;
  }
}
