/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */

package com.ericsson.oss.services.sonom.flm.kpi;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder;
import com.ericsson.oss.services.sonom.flm.kpi.util.RequestPayloadBuilder.AdditionalKpiParameters;
import com.ericsson.oss.services.sonom.flm.service.api.executions.ExecutionState;
import com.ericsson.oss.services.sonom.kpi.calculator.api.model.KpiCalculationRequestPayload;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class to construct KPI calculation request dynamically.
 */
public final class KpiCalculationRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(KpiCalculationRequest.class);

    private final String requestPayload;
    private final ExecutionState executionState;
    private final boolean isResumed;

    private KpiCalculationRequest(final String requestPayload, final ExecutionState executionState, final boolean isResumed) {
        this.requestPayload = requestPayload;
        this.executionState = executionState;
        this.isResumed = isResumed;
    }

    public ExecutionState getExecutionState() {
        return executionState;
    }

    public boolean isResumed() {
        return isResumed;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    /**
     * Class to create {@link KpiCalculationRequest}.
     */
    public static final class KpiCalculationRequestCreator {
        private final ExecutionState executionState;
        private final boolean isResumed;

        private KpiCalculationRequestCreator(final ExecutionState executionState, final boolean isResumed) {
            this.executionState = executionState;
            this.isResumed = isResumed;
        }

        public static KpiCalculationRequestLoader create(final ExecutionState executionState, final boolean isResumed) {
            return new KpiCalculationRequestLoader(new KpiCalculationRequestCreator(executionState, isResumed));
        }

        public static KpiCalculationRequest empty(final ExecutionState executionState, final boolean isResumed) {
            return new KpiCalculationRequest(StringUtils.EMPTY, executionState, isResumed);
        }

        public ExecutionState getExecutionState() {
            return executionState;
        }

        public boolean isResumed() {
            return isResumed;
        }
    }

    /**
     * Class to load group KPI JSONs for {@link KpiCalculationRequest}.
     */
    public static final class KpiCalculationRequestLoader {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final KpiCalculationRequestCreator requestCreator;

        private KpiCalculationRequestLoader(final KpiCalculationRequestCreator requestCreator) {
            this.requestCreator = requestCreator;
        }

        /**
         * Method loads KPI groups from the given JSON.
         *
         * @param fileName
         *            fileName to load KPIs from
         * @return {@link KpiCalculationPayloadBuilder} to adjust the payload
         * @throws IOException
         *             thrown if there is any error loading the file or converting it
         */
        public KpiCalculationPayloadBuilder loadGroupKpis(final String fileName) throws IOException {
            final KpiCalculationRequestPayload kpiCalculationRequestPayload = OBJECT_MAPPER.readerFor(KpiCalculationRequestPayload.class)
                    .readValue(loadResource(fileName));
            final RequestPayloadBuilder requestPayloadBuilder = RequestPayloadBuilder.from(kpiCalculationRequestPayload);
            return new KpiCalculationPayloadBuilder(requestPayloadBuilder, requestCreator);
        }

        private static String loadResource(final String fileName) throws IOException {
            try {
                return ResourceLoaderUtils.getClasspathResourceAsString(fileName);
            } catch (final IllegalArgumentException | IOException e) { //NOSONAR Exception suitably logged
                LOGGER.warn("Error loading resource through filepath: {}", fileName, e);
                throw e;
            }
        }
    }

    /**
     * Class to build {@link KpiCalculationRequest} payload.
     */
    public static final class KpiCalculationPayloadBuilder {
        private final RequestPayloadBuilder requestPayloadBuilder;
        private final KpiCalculationRequestCreator requestCreator;

        private KpiCalculationPayloadBuilder(final RequestPayloadBuilder requestPayloadBuilder, final KpiCalculationRequestCreator requestCreator) {
            this.requestPayloadBuilder = requestPayloadBuilder;
            this.requestCreator = requestCreator;
        }

        public KpiCalculationPayloadBuilder withCurrentDate(final String currentDate) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.CURRENT_DATE, currentDate);
            return this;
        }

        public KpiCalculationPayloadBuilder withStartDateTime(final String startDateTime) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.START_DATE_TIME, startDateTime);
            return this;
        }

        public KpiCalculationPayloadBuilder withEndDateTime(final String endDateTime) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.END_DATE_TIME, endDateTime);
            return this;
        }

        public KpiCalculationPayloadBuilder withRecalculationDate(final String recalculationDate) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.RECALCULATION_DATE, recalculationDate);
            return this;
        }

        public KpiCalculationPayloadBuilder withExecutionId(final String executionId) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.EXECUTION_ID, executionId);
            return this;
        }

        public KpiCalculationPayloadBuilder withAdditionalParameters(final Map<String, String> additionalParameters) {
            requestPayloadBuilder.addAdditionalParameter(additionalParameters);
            return this;
        }

        public KpiCalculationPayloadBuilder withPreviousExecutionId(final String previousExecutionId) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.PREVIOUS_EXECUTION_ID, previousExecutionId);
            return this;
        }
        
        public KpiCalculationPayloadBuilder withStartTimeStamp(final String startTimeStamp) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.START_TIMESTAMP, startTimeStamp);
            return this;
        }

        public KpiCalculationPayloadBuilder withEndTimeStamp(final String endTimeStamp) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.END_TIMESTAMP, endTimeStamp);
            return this;
        }

        public KpiCalculationPayloadBuilder withWeekendDays(final String weekendDays) {
            requestPayloadBuilder.addAdditionalParameter(AdditionalKpiParameters.WEEKEND_DAYS, weekendDays);
            return this;
        }

        /**
         * Builds the required {@link KpiCalculationRequest}.
         * 
         * @return {@link KpiCalculationRequest}
         */
        public KpiCalculationRequest build() {
            return new KpiCalculationRequest(requestPayloadBuilder.build(),
                    requestCreator.getExecutionState(),
                    requestCreator.isResumed());
        }
    }
}
