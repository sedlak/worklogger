package cz.morosystems.worklogger.service.common;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;

import cz.morosystems.worklogger.domain.JiraQueryDetails;
import cz.morosystems.worklogger.domain.Worklog;

public class JiraService {

	private static final Logger logger = LoggerFactory.getLogger(JiraService.class);
	private JiraClient jiraClient = new JiraClient();

	public void writeWorklog(JiraQueryDetails jiraDetails, String issueKey, Worklog worklog) throws IOException {
		String comment = jiraDetails.isCommentAlsoWithIssueInfo() ?
			worklog.getIssueKey() + " " + worklog.getIssueSummary() + " - " + worklog.getComment() :
			worklog.getComment();

		String dateStarted = worklog.getDateStarted();
	/*
	* String dateStarted = worklog.getDateStarted().contains("+") ?
				worklog.getDateStarted() :
				worklog.getDateStarted() + "+0000";
				*/

		String json = "{"
			+ "\"comment\": " + JSONObject.quote(comment) + ","
			+ "\"started\": \"" + dateStarted + "\","
			+ "\"timeSpentSeconds\": \"" + worklog.getTimeSpentSeconds() + "\""
			+ "}";

		logger.info("Creating {} worklog: {}", jiraDetails.getPerspective(), json);
		String url = jiraDetails.getJiraUrl() + "/rest/api/2/issue/" + issueKey + "/worklog/";
		jiraClient.makeHttpPost(json, url, jiraDetails.getLoginData());
	}

	public HashMap<String, Issue> getWorkingIssuesOfUser(String jiraUri, String credentials)
		throws URISyntaxException {
		String searchQuery = "(status changed TO resolved by currentUser() AFTER startOfDay()) OR "
			+ "(status changed to \"In progress\" by currentUser() and assignee = currentUser() and status = \"In progress\")  OR "
			+ "(status changed to \"In progress\" by currentUser()  AFTER startOfDay())";
		SearchResult searchResult = jiraClient.searchJql(jiraUri, credentials, searchQuery);
		HashMap<String, Issue> issuesMap = new HashMap<>();
		searchResult.getIssues().forEach(x -> issuesMap.put(x.getKey(), x));
		return issuesMap;
	}

	public String getWorklogsFromTimeSheet(JiraQueryDetails specifics, String startDay, String endDay)
		throws IOException {
		return getWorklogsFromTimeSheet(specifics, startDay, endDay, "");
	}

	public String getWorklogsFromTimeSheet(JiraQueryDetails specifics, String startDay, String endDay,
										   String additionalSelectionCriteria) throws IOException {
		logger.info("Getting {} worklogs for {} project", specifics.getPerspective(),
			specifics.getJiraProjectKey());
		String restUrl = specifics.getJiraUrl()
			//rest api
			+ "/rest/tempo-timesheets/3/worklogs/"
			//parameters
			+ "?"
			+ "dateFrom=" + startDay + "&"
			+ "dateTo=" + endDay + "&"
			+ "username=" + specifics.getUsername() + "&"
			+ additionalSelectionCriteria;
		logger.info("URL: {}", restUrl);
		return jiraClient.makeHttpGet(restUrl, specifics.getLoginData());
	}

	public String getIssueDetail(JiraQueryDetails specifics, String issueKey) throws IOException {
		String restUrl = specifics.getJiraUrl() + "/rest/api/2/issue/" + issueKey;
		logger.info("URL: {}", restUrl);
		return jiraClient.makeHttpGet(restUrl, specifics.getLoginData());

	}

}