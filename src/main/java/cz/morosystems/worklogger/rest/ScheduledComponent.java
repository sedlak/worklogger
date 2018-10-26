package cz.morosystems.worklogger.rest;

import cz.morosystems.worklogger.domain.WorkloggerProperties;
import cz.morosystems.worklogger.service.daylogger.DayloggerService;
import cz.morosystems.worklogger.service.synchronizator.SynchronizatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author pavol.liska
 * @date 10/8/2018
 */

@Service
public class ScheduledComponent {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledComponent.class);

//    @Scheduled(cron = "0 0 0 ? * * *")
    public void daily() throws IOException {
        sync();
    }

//    @Scheduled(cron = "0 * * * * ? *")
    public void minutely() throws IOException {
        sync();
    }

    public synchronized void sync() {
        SynchronizatorService synchronizator = null;
        try {
            synchronizator = new SynchronizatorService(getProperties());
        } catch (IOException e) {
            logger.error("{}", e);
        }
        synchronizator.syncAllDefinedProjects();
    }

    private void log() {
        DayloggerService dayloggerService = new DayloggerService(getProperties());
        try {
            dayloggerService.generateWorkLogs();
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    private WorkloggerProperties getProperties() {
        try {
            return new WorkloggerProperties(
                    "worklogger-jira-credentials.properties"
                    , "worklogger-jira-synchronizator.properties");
        } catch (IOException e) {
            logger.error("{}", e);
        }
        return null;
    }
}
