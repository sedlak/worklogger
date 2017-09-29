package cz.morosystems.worklogger.domain;

import cz.morosystems.worklogger.domain.Perspective;
import java.util.Base64;
import java.util.Properties;

/**
 * @author Pavol Sedlak
 */
public class JiraQueryDetails {

  private Perspective perspective;
  private boolean commentAlsoWithIssueInfo;
  private String username;
  private String loginData;
  private String jiraProjectKey;
  private String jiraIssueKey;
  private String jiraUrl;
  private String jiraParentIssueKey;


  public JiraQueryDetails(Perspective perspective, Properties props) {
    this.perspective = perspective;
    this.commentAlsoWithIssueInfo = Boolean.valueOf(props.getProperty("commentWithIssueInfo"));
    this.username = props.getProperty("username");
    String login = props.getProperty("username") + ":" + props.getProperty("password");
    this.loginData = Base64.getEncoder().encodeToString(login.getBytes());
    this.jiraProjectKey = props.getProperty("jiraProjectKey");
    this.jiraIssueKey = props.getProperty("jiraIssueKey");
    this.jiraUrl = fixJiraUrl(props.getProperty("jiraUrl"));
    this.jiraParentIssueKey = props.getProperty("jiraParentIssueKey");

  }

  public boolean isCommentAlsoWithIssueInfo() {
    return commentAlsoWithIssueInfo;
  }

  public String getPerspective() {
    return perspective.name();
  }

  public String getUsername() {
    return username;
  }


  public String getJiraProjectKey() {
    return jiraProjectKey;
  }

  public String getJiraIssueKey() {
    return jiraIssueKey;
  }

  public String getLoginData() {
    return loginData;
  }

  public String fixJiraUrl(String url) {
    if (url.endsWith("/")) {
      return url.substring(0, url.length() - 1);
    } else {
      return url;
    }
  }

  public String getJiraUrl() {
    return jiraUrl;
  }

  public boolean isSubtaskFilteringEnabled() {
    return getJiraParentIssueKey() != null;
  }

  public String getJiraParentIssueKey() {
    return jiraParentIssueKey;
  }

}
