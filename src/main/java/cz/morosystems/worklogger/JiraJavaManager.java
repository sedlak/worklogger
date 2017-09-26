package cz.morosystems.worklogger;



import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by moro on 9/14/2017.
 */
public class JiraJavaManager {
  private static final Logger logger = LoggerFactory.getLogger(JiraJavaManager.class);
  private final JiraRestClient restClient;


  public JiraJavaManager(String jiraUri, String credentials)
      throws URISyntaxException, IOException {

    logger.info("Connecting to {}", jiraUri);
    AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    URI serverUri;
    try {
      serverUri = new URI(jiraUri);
    } catch (URISyntaxException e) {
      logger.warn("Invalid JIRA URI:{}, error is: {}", jiraUri, e.toString());
      throw new IOException("InvalidURI");
    }
    this.restClient = factory.create(serverUri, builder -> builder.setHeader("Authorization", "Basic " + credentials));

  }

  public HashMap<String, Issue> getWorkingIssuesOfUser(){
    SearchRestClient searchClient = restClient.getSearchClient();
    Promise<SearchResult> searchPromise = searchClient.searchJql("(status changed TO resolved by currentUser() AFTER startOfDay()) OR "
        + "(status changed to \"In progress\" by currentUser() and assignee = currentUser() and status = \"In progress\")  OR "
        + "(status changed to \"In progress\" by currentUser()  AFTER startOfDay())");
    HashMap<String, Issue> issuesMap = new HashMap<>();

    searchPromise.claim().getIssues().forEach(x -> issuesMap.put(x.getKey(), x));
    return issuesMap;
  }

}
