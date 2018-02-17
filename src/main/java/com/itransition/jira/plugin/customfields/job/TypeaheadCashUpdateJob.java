package com.itransition.jira.plugin.customfields.job;

import com.itransition.jira.plugin.customfields.typeahead.TypeaheadFieldCache;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

import javax.annotation.Nullable;

public class TypeaheadCashUpdateJob implements JobRunner {

    @Nullable
    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request) {
        new TypeaheadFieldCache().updateCache();
        return JobRunnerResponse.success("Updated successfully.");
    }

}
