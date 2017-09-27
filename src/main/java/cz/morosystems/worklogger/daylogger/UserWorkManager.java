package cz.morosystems.worklogger.daylogger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import cz.morosystems.worklogger.common.CompanySpecifics;
import cz.morosystems.worklogger.common.ConnectionProperties;
import cz.morosystems.worklogger.common.Perspective;
import cz.morosystems.worklogger.common.Worklog;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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

  private JiraJavaManager jiraInfo;
  CompanySpecifics primarySpecific;
  JiraRestManager jiraRestManager;// = new JiraRestManager();
  private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public UserWorkManager(String connectionPropertiesFileName)
      throws URISyntaxException, IOException {
    ConnectionProperties conProp = new ConnectionProperties(connectionPropertiesFileName);
    jiraInfo = new JiraJavaManager(conProp.getUri(), conProp.getCredentials());//(conProp.getUri(), conProp.getCredentials(), "WAYNEEPS");
    primarySpecific = new CompanySpecifics(1, Perspective.PRIMARY, loadProperties());
    jiraRestManager = new JiraRestManager(primarySpecific.getLoginData());

  }

  private Worklog createWorkLogFromIssue(Issue issue, int time) {
    return new Worklog(
        issue.getKey(), //key
        issue.getSummary(), //summary
        ZonedDateTime.now().format(dateTimeFormat), //dateStarted TODO: aky cas?
        "Implementation.",
        time); //timeSpentSeconds

  }


  private void createFullWorkLogs(HashMap<String, Issue> workingIssuesOfUser ) throws Exception {
    List<Integer> timeSpent = computeSpentTime(8, workingIssuesOfUser.size());
    ArrayList<Issue> listWorkingIssuesOfUser = new ArrayList<>(workingIssuesOfUser.values());
    for (int i=0;i<listWorkingIssuesOfUser.size();i++) {
      Worklog worklog = createWorkLogFromIssue(listWorkingIssuesOfUser.get(i), timeSpent.get(i)*60*60);
      jiraRestManager.writeWorklog(primarySpecific,worklog);
    }
  }

  private List<Integer> computeSpentTime(int freeTime, int issueCount) throws Exception {
    int base = freeTime / issueCount;
    if (base == 0)
      throw new Exception("Ooops, je viac issues ako casu na ne!");
    int mod = freeTime % issueCount;
    ArrayList<Integer> times1 = new ArrayList<Integer>(Collections.nCopies(mod,(base+1)));
    ArrayList<Integer> times2 = new ArrayList<Integer>(Collections.nCopies(issueCount-mod, base));
    times1.addAll(times2);
    return times1;
  }

  private void createReducedWorkLogs(HashMap<String, Worklog> issuesOfUserFromWorklog, HashMap<String, Issue> issuesOfUserWorkingOn)
      throws Exception {
    //ArrayList<Issue> issues = jiraInfo.getIssuesOfUser();
    for(String issueId: issuesOfUserFromWorklog.keySet())
      issuesOfUserWorkingOn.remove(issueId);
    int spentTime = issuesOfUserFromWorklog.values().stream().mapToInt((list -> list.getTimeSpentSeconds())).sum();
    List<Integer> timeSpent = computeSpentTime(8-(spentTime/(60*60)), issuesOfUserWorkingOn.size()); //TODO: je to nepresne
    ArrayList<Issue> issues = new ArrayList<Issue>(issuesOfUserWorkingOn.values());
    for (int i=0;i<issues.size();i++) {
      Worklog worklog = createWorkLogFromIssue(issues.get(i), timeSpent.get(i)*60*60);
      jiraRestManager.writeWorklog(primarySpecific,worklog);
    }
  }

  private HashMap<String, Worklog> getWorklogsOfUser() throws IOException {
    LocalDate date = LocalDate.now();//.format(dateTimeFormat);
    String jsonString = jiraRestManager.getWorklogs(primarySpecific,date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                                                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    JSONArray worklogsArray = new JSONArray(jsonString);
    HashMap<String, Worklog> issueMap = new HashMap<>();
    for (int i = 0; i < worklogsArray.length(); i++) {
      JSONObject w = worklogsArray.getJSONObject(i);
      Worklog worklog = createWorklogFromJson(w);
      issueMap.put(worklog.getIssueKey(), worklog);
    }
    return issueMap;

  }

  public void generateWorkLogs() throws Exception {
    HashMap<String, Worklog> worklogsOfUser = getWorklogsOfUser();
    if (worklogsOfUser.size() == 0)
      createFullWorkLogs(jiraInfo.getWorkingIssuesOfUser());
    else
      createReducedWorkLogs(getWorklogsOfUser(), jiraInfo.getWorkingIssuesOfUser());

  }

  private Worklog createWorklogFromJson(JSONObject w) {
    return new Worklog(
        w.getJSONObject("issue").getString("key"),
        w.getJSONObject("issue").getString("summary"),
        w.getString("dateStarted"),
        w.getString("comment"),
        w.getInt("timeSpentSeconds")
    );
  }

  private Properties loadProperties() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream("wls.properties"));
    return props;
  }
}
