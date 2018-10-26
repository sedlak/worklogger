package cz.morosystems.worklogger.service.common;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.atlassian.util.concurrent.Promise;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.*;
//import sun.util.logging.PlatformLogger.Level;

public class JiraClient {

	private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);

	public String makeHttpGet(String restUrl, String credentials) throws IOException {
		URLConnection connection = new URL(restUrl).openConnection();
		connection.setRequestProperty("Accept-Charset", "UTF-8");
		connection.setRequestProperty("Authorization", "Basic " + credentials);
//		sun.util.logging.PlatformLogger.getLogger("sun.net.www.protocol.http.HttpURLConnection").setLevel(Level.FINE);

		InputStream response = connection.getInputStream();
		String result = IOUtils.toString(response);
		logger.info("Successfully retrieved response");
		logger.debug("JSON: {}", result);
		return result;
	}

	public void makeHttpPost(String json, String url, String credentials) throws IOException {
		//
		logger.info("POST URL: {}", url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Authorization", "Basic " + credentials);
		// Send post request
		con.setDoOutput(true);
		OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
		w.write(json);
		w.close();

		int responseCode = 0;
		try {
			responseCode = con.getResponseCode();
		} catch (IOException e) {
			logger.error("Unable to get response code", e);
		}

		String result = null;
		try {
			InputStream response = con.getInputStream();
			result = IOUtils.toString(response);
		} catch (IOException e) {
			logger.error("Unable to get response body", e);
		}

		if (responseCode != 201) {
			logger.error("Response Code: {}", responseCode);
			logger.error("Response Body: {}", result);
			throw new IOException("Nepodarilo sa zapisat pracu! ResponseCode = " + responseCode);
		}
	}

	public SearchResult searchJql(String jiraUri, String credentials, String searchQuery)
		throws URISyntaxException {
		logger.info("Connecting to {}", jiraUri);
		AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		URI serverUri = new URI(jiraUri);
		JiraRestClient restClient = factory
			.create(serverUri, builder -> builder.setHeader("Authorization", "Basic " + credentials));
		SearchRestClient searchClient = restClient.getSearchClient();
		Promise<SearchResult> searchPromise = searchClient.searchJql(searchQuery);
		return searchPromise.claim();

	}

}
