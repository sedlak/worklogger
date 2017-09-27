package cz.morosystems.worklogger.common;

import java.util.Base64;
import java.util.Properties;

/**
 * @author Pavol Sedlak
 */
public class CompanySpecifics {

	private Perspective perspective;
	private Properties props;
	private int sequenceNumber;

	public CompanySpecifics(int sequenceNumber, Perspective perspective, Properties props){
		this.sequenceNumber = sequenceNumber;
		this.perspective = perspective;
		this.props = props;
	}

	public String getPerspective() {
		return perspective.name();
	}

	public boolean commentAlsoWithIssueInfo() {
		return Boolean.valueOf(getProperty("commentWithIssueInfo"));
	}

	public String getUsername() {
		return getProperty("username");
	}

	public String getPassword() {
		return getProperty("password");
	}

	public String getJiraProjectKey() {
		return getProperty("jiraProjectKey");
	}

	public String getJiraIssueKey() {
		return getProperty("jiraIssueKey");
	}

	public String getLoginData() {
		String login = getUsername() + ":" + getPassword();
		return Base64.getEncoder().encodeToString(login.getBytes());
	}

	public String getJiraUrl() {
		String url = getProperty("jiraUrl");
		if(url.endsWith("/")){
			return url.substring(0, url.length()-1 );
		}else{
			return url;
		}
	}

	public boolean isSubtaskFilteringEnabled() {
		return getJiraParentIssueKey() != null;
	}

	public String getJiraParentIssueKey() {
		return getProperty("jiraParentIssueKey");
	}

		private String getProperty(String key){
		return props.getProperty(sequenceNumber + "." + perspective.name() + "." + key);
	}

	}
