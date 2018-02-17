package com.itransition.jira.plugin.customfields.job;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import java.util.Locale;

public class CustomFieldsJobManager implements LifecycleAware, DisposableBean {

    private final static Logger LOGGER = Logger.getLogger(CustomFieldsJobManager.class);
    private final static JobRunnerKey JOB_RUNNER_KEY_TYPEAHEAD = JobRunnerKey.of("CustomFieldsJobRunner");
    private final static JobId JOB_ID_TYPEAHEAD = JobId.of("Typeahead cash update Job Id");
    private final SchedulerService schedulerService;

    public CustomFieldsJobManager(final SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public void onStart() {
        final I18nHelper i18n = ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault());
        final String cron = new String(i18n.getText("typeahead.job.cron"));
        schedulerService.registerJobRunner(JOB_RUNNER_KEY_TYPEAHEAD, new TypeaheadCashUpdateJob());
        final JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY_TYPEAHEAD)
                .withRunMode(RunMode.RUN_LOCALLY)
                .withSchedule(Schedule.forCronExpression(cron));
        try {
            schedulerService.scheduleJob(JOB_ID_TYPEAHEAD, jobConfig);
        } catch (SchedulerServiceException schedulerServiceException) {
            LOGGER.error(schedulerServiceException);
        }
    }

    @Override
    public void onStop() {
        schedulerService.unregisterJobRunner(JOB_RUNNER_KEY_TYPEAHEAD);
        schedulerService.unscheduleJob(JOB_ID_TYPEAHEAD);
    }

    @Override
    public void destroy() throws Exception {
        schedulerService.unregisterJobRunner(JOB_RUNNER_KEY_TYPEAHEAD);
        schedulerService.unscheduleJob(JOB_ID_TYPEAHEAD);
    }
}
