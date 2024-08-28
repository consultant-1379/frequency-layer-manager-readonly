/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.sonom.flm.settings;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.flm.metric.FlmMetricHelper;
import com.ericsson.oss.services.sonom.flm.service.cell.CellIdentifier;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.FlmMetric;
import com.ericsson.oss.services.sonom.flm.service.metrics.api.MetricHelper;

/**
 * This class collects all the KPIs for each cellIdentifier in the sectors.
 */
public class CellSettingCollection {

    private static final Logger LOGGER = LoggerFactory.getLogger(CellSettingCollection.class);

    private final CellFlmSettingsRetriever cellFlmSettingsRetriever;
    private final MetricHelper flmMetricHelper;

    private Map<CellIdentifier, Map<String, String>> cellSettings = new HashMap<>();

    public CellSettingCollection() {
        cellFlmSettingsRetriever = new CellFlmSettingsRetriever();
        flmMetricHelper = new FlmMetricHelper();
    }

    // required for Mockito JUnit
    public CellSettingCollection(final CellFlmSettingsRetriever cellFlmSettingsRetriever,
            final FlmMetricHelper flmMetricHelper) {
        this.cellFlmSettingsRetriever = cellFlmSettingsRetriever;
        this.flmMetricHelper = flmMetricHelper;
    }

    /**
     * Method to collect cell settings.
     * @param executionId execution id
     * @return collected cell settings.
     * @throws SQLException thrown if an error occurred retrieving cell settings.
     */
    public Map<CellIdentifier, Map<String, String>> collect(final String executionId) throws SQLException {
        retrieveSettingsForCells(executionId);

        return cellSettings;
    }

    private void retrieveSettingsForCells(final String executionId) throws SQLException {
        final long cellSettingsLoadStartTime = System.nanoTime();
        cellSettings = cellFlmSettingsRetriever.retrieveCellSettings(executionId);
        LOGGER.info("Cell settings have been loaded successfully");
        flmMetricHelper.incrementFlmMetric(FlmMetric.FLM_SETTINGS_LOAD_IN_MILLIS,
                flmMetricHelper.getTimeElapsedInMillis(cellSettingsLoadStartTime));
    }

}
