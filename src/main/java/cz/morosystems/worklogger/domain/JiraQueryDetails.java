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


  public JiraQueryDetails(Perspective perspective, Properties connectionProperties, Properties projectPropertie) {
    this.perspective = perspective;
    this.username = connectionProperties.getProperty("username");
    String login = connectionProperties.getProperty("username") + ":" + connectionProperties.getProperty("password");
    this.loginData = Base64.getEncoder().encodeToString(login.getBytes());
    this.jiraUrl = fixJiraUrl(connectionProperties.getProperty("jiraUrl"));
    this.commentAlsoWithIssueInfo = Boolean.valueOf(projectPropertie.getProperty("commentWithIssueInfo"));
    this.jiraProjectKey = projectPropertie.getProperty("jiraProjectKey");
    this.jiraIssueKey = projectPropertie.getProperty("jiraIssueKey");
    this.jiraParentIssueKey = projectPropertie.getProperty("jiraParentIssueKey");

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
