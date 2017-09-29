package cz.morosystems.worklogger.domain;

/**
 * @author Pavol Sedlak
 */
public class Worklog {

  private String issueKey;
  private String issueSummary;
  private String dateStarted;
  private String comment;
  private int timeSpentSeconds;

  private Boolean inSync;
  private Boolean oppositeWorklogCreated;

  public Worklog(String issueKey, String issueSummary, String dateStarted, String comment,
      int timeSpentSeconds) {
    this.issueKey = issueKey;
    this.issueSummary = issueSummary;
    this.dateStarted = dateStarted;
    this.comment = comment;
    this.timeSpentSeconds = timeSpentSeconds;
  }

  public String getIssueKey() {
    return issueKey;
  }

  public String getIssueSummary() {
    return issueSummary;
  }

  public String getDateStarted() {
    return dateStarted;
  }

  public String getComment() {
    return comment;
  }

  public int getTimeSpentSeconds() {
    return timeSpentSeconds;
  }

  public String getDate() {
    return dateStarted.substring(0, 10);
  }

  public void setInSync(boolean inSync) {
    this.inSync = inSync;
  }

  public Boolean isInSync() {
    return inSync;
  }

  public void setOppositeWorklogCreated(boolean oppositeWorklogCreated) {
    this.oppositeWorklogCreated = oppositeWorklogCreated;
  }

  public Boolean isOppositeWorklogCreated() {
    return oppositeWorklogCreated;
  }

  @Override
  public String toString() {
    return "Worklog{" + "issueKey='" + issueKey + '\'' + "issueSummary='" + issueSummary + '\''
        + ", dateStarted='" + dateStarted + '\'' + ", comment='" + comment + '\''
        + ", timeSpentSeconds=" + timeSpentSeconds + '}';
  }
}
