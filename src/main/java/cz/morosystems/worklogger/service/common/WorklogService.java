package cz.morosystems.worklogger.service.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;
import com.atlassian.jira.rest.client.api.domain.Issue;

import cz.morosystems.worklogger.domain.JiraQueryDetails;
import cz.morosystems.worklogger.domain.Worklog;

public class WorklogService {

	private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter
		.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public boolean worklogCompare(Worklog primaryWorklog, Worklog mirrorWorklog,
								  JiraQueryDetails mirrorSpecifics) {
		if (mirrorSpecifics.isCommentAlsoWithIssueInfo()) {
			return (primaryWorklog.getTimeSpentSeconds() == mirrorWorklog.getTimeSpentSeconds()
				&& mirrorWorklog.getComment().contains(primaryWorklog.getIssueKey()));
		} else {
			return (primaryWorklog.getTimeSpentSeconds() == mirrorWorklog.getTimeSpentSeconds());
			//&& mirrorWorklog.getComment().contains(primaryWorklog.getComment());
		}
	}

	public Worklog createWorklogFromJson(JSONObject w) {
		String dateStarted = LocalDateTime.parse(w.getString("dateStarted")).atZone(ZoneId.systemDefault()).format(dateTimeFormat);
		return new Worklog(
			w.getJSONObject("issue").getString("key"),
			w.getJSONObject("issue").getString("summary"),
			dateStarted,
			w.getString("comment"),
			w.getInt("timeSpentSeconds")
		);
	}

	public Worklog createWorkLogFromIssue(Issue issue, int time) {
		return new Worklog(
			issue.getKey(), //key
			issue.getSummary(), //summary
			ZonedDateTime.now().format(dateTimeFormat),
			"Implementation.",
			time); //timeSpentSeconds

	}
}
