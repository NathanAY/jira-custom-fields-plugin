package com.itransition.jira.plugin.customfields.typeahead;

import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.searchers.AllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.util.NotNull;

public class TypeaheadCustomField extends AbstractSingleFieldType<String> implements AllTextCustomFieldSearcherClauseHandler {

    public TypeaheadCustomField(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    public String getStringFromSingularObject(final String singularObject) {
        if (singularObject == null) {
            return null;
        } else {
            return singularObject.toString();
        }
    }

    @Override
    public String getSingularObjectFromString(final String singularObject) throws FieldValidationException {
        return singularObject;
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    @Override
    protected String getObjectFromDbValue(@NotNull final Object databaseValue) throws FieldValidationException {
        return getSingularObjectFromString((String) databaseValue);
    }

    @Override
    protected Object getDbValueFromObject(final String customFieldObject) {
        new TypeaheadFieldCache().updateCache();
        return getStringFromSingularObject(customFieldObject);
    }

}
