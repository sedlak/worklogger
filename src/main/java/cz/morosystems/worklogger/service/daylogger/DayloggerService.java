package cz.morosystems.worklogger.service.daylogger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import cz.morosystems.worklogger.domain.WorkloggerProperties;
import cz.morosystems.worklogger.domain.JiraQueryDetails;
import cz.morosystems.worklogger.service.common.JiraService;
import cz.morosystems.worklogger.domain.Perspective;
import cz.morosystems.worklogger.domain.Worklog;
import cz.morosystems.worklogger.service.common.WorklogService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DayloggerService {

  private static final Logger logger = LoggerFactory.getLogger(DayloggerService.class);
  private JiraQueryDetails companySpecific;

  // ready for spring in future
  private JiraService jiraService = new JiraService();
  private WorklogService worklogService = new WorklogService();

  public DayloggerService(WorkloggerProperties properties) {
    companySpecific = new JiraQueryDetails(Perspective.PRIMARY,
        properties.getCredentialsProperties(Perspective.PRIMARY),
        properties.getProjectProperties(1, Perspective.PRIMARY));
  }


  public void generateWorkLogs() throws Exception {
  	logger.info("Going to create worklogs based on your JIRA activity");
    HashMap<String, Worklog> worklogsOfUser = getWorklogsOfUserToday();
    if (worklogsOfUser.size() == 0) {
      createFullWorkLogs(jiraService
          .getWorkingIssuesOfUser(companySpecific.getJiraUrl(), companySpecific.getLoginData()));
    } else {
      createReducedWorkLogs(worklogsOfUser, jiraService
          .getWorkingIssuesOfUser(companySpecific.getJiraUrl(), companySpecific.getLoginData()));
    }

  }


  private void createFullWorkLogs(HashMap<String, Issue> workingIssuesOfUser) throws Exception {
    List<Integer> timeSpent = computeSpentTime(8, workingIssuesOfUser.size());
    ArrayList<Issue> listWorkingIssuesOfUser = new ArrayList<>(workingIssuesOfUser.values());
    writeIssuesToWorklog(listWorkingIssuesOfUser, timeSpent);
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
    ArrayList<Issue> listWorkingIssuesOfUser = new ArrayList<>(issuesOfUserWorkingOn.values());
    writeIssuesToWorklog(listWorkingIssuesOfUser, timeSpent);
  }

  private List<Integer> computeSpentTime(int freeTime, int issueCount) throws Exception {
    int base = freeTime / issueCount;
    if (base == 0) {
      //logger.error("Je viac issues ako casu na ne!");
      throw new Exception("Ooops, je viac issues ako casu na ne!");
    }
    int mod = freeTime % issueCount;
    ArrayList<Integer> times1 = new ArrayList<Integer>(Collections.nCopies(mod, (base + 1)));
    ArrayList<Integer> times2 = new ArrayList<Integer>(Collections.nCopies(issueCount - mod, base));
    times1.addAll(times2);
    return times1;
  }


  private void writeIssuesToWorklog(ArrayList<Issue> issues, List<Integer> timeSpent) {
    for (int i = 0; i < issues.size(); i++) {
      Worklog worklog = worklogService
          .createWorkLogFromIssue(issues.get(i), timeSpent.get(i) * 60 * 60);
      try {
        jiraService.writeWorklog(companySpecific, worklog);
      } catch (Exception e) {
        logger.error("Unable to write worklog " + worklog.toString());
      }
    }
  }

  private HashMap<String, Worklog> getWorklogsOfUserToday() throws IOException {
    return getWorklogsOfUser(LocalDate.now(), LocalDate.now());
  }

  private HashMap<String, Worklog> getWorklogsOfUser(LocalDate startDate, LocalDate endDate)
      throws IOException {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    String jsonString = jiraService
        .getWorklogsFromTimeSheet(companySpecific, startDate.format(dateFormatter),
            endDate.format(dateFormatter));
    JSONArray worklogsArray = new JSONArray(jsonString);
    HashMap<String, Worklog> issueMap = new HashMap<>();
    for (int i = 0; i < worklogsArray.length(); i++) {
      JSONObject w = worklogsArray.getJSONObject(i);
      Worklog worklog = worklogService.createWorklogFromJson(w);
      issueMap.put(worklog.getIssueKey(), worklog);
    }
    return issueMap;

  }


}
