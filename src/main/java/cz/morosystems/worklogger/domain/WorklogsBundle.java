package cz.morosystems.worklogger.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author Pavol Sedlak
 */
public class WorklogsBundle {

	private JiraQueryDetails specifics;
	private SortedMap<String, List<Worklog>> worklogsMap;

	public WorklogsBundle(JiraQueryDetails specifics, SortedMap<String, List<Worklog>> worklogsMap) {
		this.specifics = specifics;
		this.worklogsMap = worklogsMap;
	}

	public JiraQueryDetails getSpecifics() {
		return specifics;
	}

	public Map<String, List<Worklog>> getWorklogsMap() {
		return worklogsMap;
	}

	public int getWorklogsTotalCount() {
		return worklogsMap.values().stream().mapToInt(List::size).sum();
	}

	public int getWorklogsDays() {
		return worklogsMap.keySet().size();
	}

	public int getWorklogsInSyncCount() {
		return worklogsMap.values().stream().mapToInt(
			list -> list.stream().mapToInt(w -> w.isInSync() == null || w.isInSync() ? 1 : 0).sum())
			.sum();
	}

	public int getWorklogsNotInSyncCount() {
		return worklogsMap.values().stream().mapToInt(
			list -> list.stream()
				.mapToInt(w -> w.isInSync() != null && !w.isInSync() ? 1 : 0).sum()).sum();
	}

	public int getSuccessfullyCreatedWorklogsCount() {
		return worklogsMap.values().stream().mapToInt(
			list -> list.stream()
				.filter((w -> w.isOppositeWorklogCreated() != null))
				.mapToInt(w -> w.isOppositeWorklogCreated() ? 1 : 0).sum()).sum();
	}

	public int getNotCreatedWorklogsCount() {
		return worklogsMap.values().stream().mapToInt(
			list -> list.stream().mapToInt(w -> w.isOppositeWorklogCreated() ? 0 : 1).sum()).sum();
	}

	public int getSecondsForDay(String date) {
		return worklogsMap.getOrDefault(date, new ArrayList<>()).stream()
			.mapToInt((list -> list.getTimeSpentSeconds())).sum();
	}
}
