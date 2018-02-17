package com.itransition.jira.plugin.customfields.typeahead;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.GenericEntityException;
import ru.itransition.jira.sql.SqlExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

public class TypeaheadFieldCache {

    private static final Logger LOGGER = Logger.getLogger(TypeaheadFieldCache.class);
    private static final ConcurrentHashMap<String, List<Map.Entry<String, Long>>> VALUES_CACHE = new ConcurrentHashMap<>();

    /**
     * Returns an StringBuilder object that contains all the values of the
     * custom field, separated by commas with a space and sorted by popularity.
     *
     * @param  fieldId  id of the custom field
     */
    public StringBuilder getAvailableElements(String fieldId) {
        if (!VALUES_CACHE.containsKey(fieldId)) {
            putValuesToCache(fieldId);
        }
        final StringBuilder availableValues = new StringBuilder();
        VALUES_CACHE.get(fieldId).forEach(s -> availableValues.append(s.getKey() + ", "));
        return availableValues;
    }

    /**
     * Updates cache for all custom Field of type 'typeahead'.
     */
    public void updateCache() {
        final Enumeration<String> keys = VALUES_CACHE.keys();
        while (keys.hasMoreElements()) {
            putValuesToCache(keys.nextElement());
        }
    }

    private void putValuesToCache(final String fieldId) {
        final List<Map> dataBaseValuesMap = getValuesFromDb(fieldId);
        final List valuesList = new ArrayList();
        for (final Map value : dataBaseValuesMap) {
            final String words = (String) new ArrayList(value.values()).get(0);
            valuesList.addAll(Arrays.asList(words.split(", ")));
        }
        final Map<String, Long> wordsCountMap = getWordsCountMap(valuesList);
        VALUES_CACHE.put(fieldId, sortMapByWordsCount(wordsCountMap));
    }

    private static List getValuesFromDb(final String fieldId) {
        final String query = "SELECT distinct TEXTVALUE FROM customfieldvalue where customfield = ? ;";
        final ArrayList<String> executorParams = new ArrayList<>();
        executorParams.add(fieldId);
        List<Map<String, Object>> result = null;
        try (final SqlExecutor executor = new SqlExecutor(getJiraConnection())) {
            result = new ArrayList(executor.execute(query,
                    newArrayList(executorParams),
                    resultSet -> resultSetToMap(resultSet)));
        } catch (final SQLException error) {
            LOGGER.error(error);
        }
        return result;
    }

    private Map<String, Long> getWordsCountMap(final List<String> words) {
        final Map<String, Long> wordsCountMap = words.stream().collect(
                Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return wordsCountMap;
    }

    private List<Map.Entry<String, Long>> sortMapByWordsCount(final Map<String, Long> wordsCountMap) {
        final HashMap<String, Long> finalMap = new HashMap<>();
        wordsCountMap.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue()
                .reversed()).forEachOrdered(m -> finalMap.put(m.getKey(), m.getValue()));
        final List<Map.Entry<String, Long>> elements = new ArrayList(finalMap.entrySet());
        return elements;
    }

    private static Map resultSetToMap(final ResultSet rs) {
        final Map<String, Object> rowResult = new ListOrderedMap();
        try {
            final ResultSetMetaData rsMetaData = rs.getMetaData();
            final int numberOfColumns = rsMetaData.getColumnCount();
            for (int i = 1; i < numberOfColumns + 1; i++) {
                Object value = rs.getObject(i);
                if (value instanceof Clob) {
                    final Clob clob = (Clob) value;
                    final StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(clob.getCharacterStream());
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                    } catch (final IOException exception) {
                        LOGGER.error(exception);
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                    value = stringBuilder.toString();
                }
                rowResult.put(rsMetaData.getColumnLabel(i).toUpperCase(), value);
            }
        } catch (SQLException | IOException error) {
            LOGGER.error(error);
        }
        return rowResult;
    }

    private static Connection getJiraConnection() {
        try {
            return ConnectionFactory.getConnection("defaultDS");
        } catch (final SQLException | GenericEntityException error) {
            LOGGER.error(error);
        }
        return null;
    }

}
