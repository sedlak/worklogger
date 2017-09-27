package cz.morosystems.worklogger.synchronizator;

import cz.morosystems.worklogger.common.CompanySpecifics;
import cz.morosystems.worklogger.common.Perspective;
import cz.morosystems.worklogger.common.Worklog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pavol Sedlak
 */
public class WorkLogSynchronizator {

	private static final Logger logger = LoggerFactory.getLogger(WorkLogSynchronizator.class);
	private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final Properties properties;
	private final SyncPeriod period;

	public WorkLogSynchronizator() throws IOException {
		properties = loadProperties();
		period = detectSyncPeriod();
	}

	private SyncPeriod detectSyncPeriod() {
		String periodProperty = System.getProperty("syncPeriod", SyncPeriod.CURRENT_MONTH.name());
		//TODO tu si skoncil
		//SyncPeriod.valueOf()
		//if()
		return SyncPeriod.CURRENT_MONTH;
	}

	public void syncAllDefinedProjects(){
		int projectToSynchronizeCount = properties.keySet().stream().mapToInt(key -> {
			String propKey = (String) key;
			return Integer.valueOf(propKey.split("\\.")[0]);
		}).max().getAsInt();

		logger.info("Found {} projects", projectToSynchronizeCount);

		for(int i=1; i <= projectToSynchronizeCount; i++){
			try{
				logger.info("");
				logger.info("");
				logger.info("Going to synchronize project #{}", i);

				syncWorklogs(
					new CompanySpecifics(i, Perspective.PRIMARY, properties),
					new CompanySpecifics(i, Perspective.MIRROR, properties)
				);
			}catch (IOException e){
				logger.error("Ooops, Unable to synchronize project number " + i, e);
			}
		}

	}

	private void syncWorklogs(CompanySpecifics primarySpecifics, CompanySpecifics mirrorSpecifics) throws IOException {
		WorklogsBundle primaryBundle = getWorklogsBundle(primarySpecifics);
		WorklogsBundle mirrorBundle = getWorklogsBundle(mirrorSpecifics);

		logVisualDelimiter();
		logger.info("Starting synchronization");

		//Porovnaj dni z primarneho systemu
		Set<String> daysToSync = findDaysToSync(primaryBundle, mirrorBundle);

		//Vysporiadaj dni, ktore mali menej hodin v cielovom systeme
		syncSpecificDays(primaryBundle, mirrorBundle, daysToSync);

		//Zaloguj vysledok synchronizacie
		//logSyncResult(primaryBundle, mirrorBundle);
	}

	private Set<String> findDaysToSync(WorklogsBundle primaryBundle, WorklogsBundle mirrorBundle) {
		logger.info("Comparing hours per day");
		logger.info("|------------|---------|--------|----------|");
		logger.info("| Date       | {} | {} | Status   |", primaryBundle.getSpecifics().getPerspective(), mirrorBundle.getSpecifics().getPerspective());
		logger.info("|------------|---------|--------|----------|");
		Set<String> daysToSync = new TreeSet<>();
		primaryBundle.getWorklogsMap().keySet().forEach((key) -> {
			int primaryDay = primaryBundle.getSecondsForDay(key);
			int mirrorDay = mirrorBundle.getSecondsForDay(key);
			//Ak mame v dany den menej hodin, tak to treba synchronizovat
			boolean toSync = mirrorDay < primaryDay;
			if (toSync){
				daysToSync.add(key);
			}
			logger.info("| {} |   {} |  {} |  {} |", key, secondsToHours(primaryDay), secondsToHours(mirrorDay), toSync ? "to sync" : "     ok");
		});
		logger.info("|------------|---------|--------|----------|");
		return daysToSync;
	}

	private void syncSpecificDays(WorklogsBundle primaryBundle, WorklogsBundle mirrorBundle, Set<String> daysToSync) {
		if(daysToSync.isEmpty()){
			logger.info("Well, nothing to synchronize");
			logger.info("You are very nice worklogger ;)");
			logger.info("Next time please let me do some stuff");
		}else{
			logger.info("Going to sync {} days that have less hours", daysToSync.size());
			primaryBundle.getWorklogsMap().forEach((key, list) -> {
				if(daysToSync.contains(key)){
					list.forEach(primaryWorklog -> {
						boolean isInSync = mirrorBundle.getWorklogsMap().getOrDefault(key, new ArrayList<>())
								.stream().anyMatch((mirrorWorklog -> mirrorBundle.getSpecifics().worklogCompare(primaryWorklog, mirrorWorklog)));
						primaryWorklog.setInSync(isInSync);
						if(!isInSync){
							try {
								createWorklog(mirrorBundle.getSpecifics(), primaryWorklog);
								primaryWorklog.setOppositeWorklogCreated(true);
							} catch (IOException e) {
								logger.error("Unable to create worklog to JIRA", e);
								primaryWorklog.setOppositeWorklogCreated(false);
							}
						}
					});
				}
			});
			logger.info("Job done! (at least, all I could do for you is done)");
		}
	}

	private void logSyncResult(WorklogsBundle primaryBundle, WorklogsBundle mirrorBundle) {
		String primaryCompany = primaryBundle.getSpecifics().getPerspective();
		String mirrorCompany = mirrorBundle.getSpecifics().getPerspective();

		logVisualDelimiter();
		logger.info("Synchronization from {} to {} is done", primaryCompany, mirrorCompany);
		logVisualDelimiter();
		logger.info("Statistics:");
		logger.info("{} Worklogs = {} in {} days", primaryCompany, primaryBundle.getWorklogsTotalCount(), primaryBundle.getWorklogsDays());
		logger.info("{} Worklogs = {} in {} days", mirrorCompany, mirrorBundle.getWorklogsTotalCount(), mirrorBundle.getWorklogsDays());
		logger.info("{} Worklogs In Sync = {}", primaryCompany, primaryBundle.getWorklogsInSyncCount());
		logger.info("{} Worklogs created = {}", mirrorCompany, primaryBundle.getSuccessfullyCreatedWorklogsCount());
	}

	private WorklogsBundle getWorklogsBundle(CompanySpecifics specifics) throws IOException {
		String jsonString = getWorklogs(specifics);
		SortedMap<String, List<Worklog>> result = new TreeMap<>();

		Set<String> issueKeyWhitelist = null;
		boolean parentFilteringEnabled = specifics.isSubtaskFilteringEnabled();
		if(parentFilteringEnabled){
			issueKeyWhitelist = getParentIssueSubtasks(specifics);
		}

		JSONArray worklogsArray = new JSONArray(jsonString);
		for (int i = 0; i < worklogsArray.length(); i++){
			JSONObject w = worklogsArray.getJSONObject(i);
			Worklog worklog = createWorklogFromJson(w);
			String key = worklog.getDate();
			List<Worklog> list = result.getOrDefault(key, new ArrayList<>());

			//Ak je zapnuty filtering cez parenta, tak pridavame iba subtasky daneho parenta
			if(parentFilteringEnabled){
				if(issueKeyWhitelist.contains(worklog.getIssueKey())){
					list.add(worklog);
				}
			}else{
				list.add(worklog);
			}
			result.put(key, list);
		}
		logger.info("Found: {} worklogs in {} days", worklogsArray.length(), result.size());
		return new WorklogsBundle(specifics, result);
	}

	public String getWorklogs(CompanySpecifics specifics) throws IOException {
		logVisualDelimiter();
		logger.info("Getting {} worklogs for {} project", specifics.getPerspective(), specifics.getJiraProjectKey());
		String restUrl = specifics.getJiraUrl()
				//rest api
				+ "/rest/tempo-timesheets/3/worklogs/"
				//parameters
				+ "?"
				+ "dateFrom=" + getStartOfCurrentPeriod() + "&"
				+ "dateTo=" + getEndOfCurrentPeriod() + "&"
				+ "username=" + specifics.getUsername() + "&"
				+ "projectKey=" + specifics.getJiraProjectKey();
		logger.info("URL: {}", restUrl);
		return makeHttpGet(specifics, restUrl);
	}

	private Set<String> getParentIssueSubtasks(CompanySpecifics specifics) throws IOException {
		String parentIssueKey = specifics.getJiraParentIssueKey();
		logger.info("Getting {} parent issue detail {}", specifics.getPerspective(), parentIssueKey);
		String restUrl = specifics.getJiraUrl() + "/rest/api/2/issue/" + parentIssueKey;
		logger.info("URL: {}", restUrl);
		String parentIssueJsonString = makeHttpGet(specifics, restUrl);

		Set<String> result = new HashSet<>();
		result.add(parentIssueKey);

		JSONObject issue = new JSONObject(parentIssueJsonString);
		JSONObject fields = issue.getJSONObject("fields");
		if(fields.has("subtasks")) {
			JSONArray subtasks = fields.getJSONArray("subtasks");
			for (int i = 0; i < subtasks.length(); i++){
				result.add(((JSONObject)subtasks.get(i)).getString("key"));
			}
		}else {
			throw new IllegalStateException("Defined Parent Issue " + parentIssueKey + "has no subtasks!");
		}
		return result;
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

	public void createWorklog(CompanySpecifics specifics, Worklog worklog) throws IOException {
		//Do MORO JIRI vytvarame comment aj s additional information: "Key+Summary"
		String comment = specifics.commentAlsoWithIssueInfo() ?
				worklog.getIssueKey() + " " + worklog.getIssueSummary() + " - " + worklog.getComment() :
				worklog.getComment();

		String dateStarted = worklog.getDateStarted().contains("+") ?
				worklog.getDateStarted() :
				worklog.getDateStarted() + "+0000";

		String json = "{"
				+ "\"comment\": " + JSONObject.quote(comment) + ","
				+ "\"started\": \"" + dateStarted + "\","
				+ "\"timeSpentSeconds\": \"" + worklog.getTimeSpentSeconds() + "\""
				+ "}";

		logger.info("Creating {} worklog: {}", specifics.getPerspective(), json);
		makeHttpPost(specifics, json);
	}

	private void makeHttpPost(CompanySpecifics specifics, String json) throws IOException {
		String url = specifics.getJiraUrl() + "/rest/api/2/issue/" + specifics.getJiraIssueKey() + "/worklog/";
		logger.info("POST URL: {}", url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Authorization", "Basic " + specifics.getLoginData());
		// Send post request
		con.setDoOutput(true);
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(json);
		w.close();

		int responseCode = 0;
		try{
			responseCode = con.getResponseCode();
		}catch (IOException e){
			logger.error("Unable to get response code", e);
		}

		String result = null;
		try{
			InputStream response = con.getInputStream();
			result = IOUtils.toString(response);
		}catch (IOException e){
			logger.error("Unable to get response body", e);
		}

		if(responseCode != 201){
			logger.error("Response Code: {}", responseCode);
			logger.error("Response Body: {}", result);
			throw new IOException("Nepodarilo sa zapisat pracu! ResponseCode = " + responseCode);
		}
	}

	private String makeHttpGet(CompanySpecifics specifics, String restUrl) throws IOException {
		URLConnection connection = new URL(restUrl).openConnection();
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("Authorization", "Basic " + specifics.getLoginData());
		InputStream response = connection.getInputStream();
		String result = IOUtils.toString(response);
		logger.info("Successfully retrieved response");
		logger.debug("JSON: {}", result);
		return result;
	}

	private String getStartOfCurrentPeriod() {
		LocalDate date = LocalDate.now();
		if(youAreLazyAndWantToSyncPreviousMonth()){
			date = date.minusMonths(1);
		}
		return date.withDayOfMonth(1).format(dateTimeFormat);
	}

	private String getEndOfCurrentPeriod() {
		LocalDate date = LocalDate.now();
		if(youAreLazyAndWantToSyncPreviousMonth()){
			date = date.minusMonths(1);
		}
		return date.withDayOfMonth(date.lengthOfMonth()).format(dateTimeFormat);
	}

	private boolean youAreLazyAndWantToSyncPreviousMonth() {
		System.getProperty("month");
		return false;
	}

	private String secondsToHours(int seconds) {
		if((seconds / 60) % 60 == 0){
			return "  " + seconds / 60 / 60 + " h";
		}else{
			DecimalFormat decimalFormat = new DecimalFormat("0.0");
			return decimalFormat.format(seconds / 60 / 60.0) + " h";
		}
	}

	private void logVisualDelimiter() {
		logger.info("-----------------------------------------------------------");
	}

	private Properties loadProperties() throws IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("wls.properties"));
		return props;
	}

	public Properties getProperties(){
		return properties;
	}

}
