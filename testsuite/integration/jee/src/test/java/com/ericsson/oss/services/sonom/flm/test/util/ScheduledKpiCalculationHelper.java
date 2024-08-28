/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test.util;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ericsson.oss.services.sonom.common.resources.utils.ResourceLoaderUtils;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.ericsson.oss.services.sonom.kpi.calculator.api.exception.KpiModelVerificationException;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationRequestHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiCalculationStateHandler;
import com.ericsson.oss.services.sonom.kpi.client.KpiServiceRestExecutor;
import com.ericsson.oss.services.sonom.kpi.service.rest.api.v1.CalculationRequestSuccessResponse;
import com.google.gson.Gson;

/**
 * Class to calculate scheduled kpis by converting them to on demand kpis.
 */
public class ScheduledKpiCalculationHelper {

    private static final String REQUIRED_KPIS_FILE_PATH = "RequiredKpis.json";
    private static final String KPI_DEFINITION_REQUEST_ATTRIBUTE_KPI_DEFINITIONS = "kpi_definitions";
    private static final String KPI_DEFINITION_REQUEST_ATTRIBUTE_NAME = "name";
    private static final String KPI_DEFINITION_REQUEST_ATTRIBUTE_CALCULATION_FREQUENCY = "calculation_frequency";
    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST_SECONDS = 20;
    private static final int MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE = 360;
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS = 30;
    private static final int RETRY_WAIT_DURATION_KPI_CALCULATION_STATE = 5;
    private static final KpiCalculationRequestHandler KPI_CALCULATION_REQUEST_HANDLER = new KpiCalculationRequestHandler(
            MAX_RETRY_ATTEMPTS_KPI_CALCULATION_REQUEST_SECONDS, RETRY_WAIT_DURATION_KPI_CALCULATION_REQUEST_SECONDS);
    private static final KpiServiceRestExecutor KPI_SERVICE_REST_EXECUTOR = new KpiServiceRestExecutor.Builder().withRestExecutor(new RestExecutor())
            .build();
    private static final KpiCalculationStateHandler KPI_CALCULATION_STATE_HANDLER = new KpiCalculationStateHandler(
            MAX_RETRY_ATTEMPTS_KPI_CALCULATION_STATE, RETRY_WAIT_DURATION_KPI_CALCULATION_STATE);

    private ScheduledKpiCalculationHelper() {
    }

    /**
     * This method changes scheduled kpi(s) to on demand kpi.
     * 
     * @param kpiDefinitionsToOverride
     *            Scheduled kpis which need to be converted to on-demand kpis
     * @throws IOException
     * @throws KpiModelVerificationException
     * @throws ParseException
     */
    public static void changeKpisToOnDemandKpis(final List<String> kpiDefinitionsToOverride)
            throws IOException, KpiModelVerificationException, ParseException {
        final RestResponse<String> requestResponse = KPI_SERVICE_REST_EXECUTOR
                .putKpis(ScheduledKpiCalculationHelper.getKpiDefinitionRequest(kpiDefinitionsToOverride));
        if (HttpStatus.SC_ACCEPTED != requestResponse.getStatus()) {
            throw new KpiModelVerificationException("Error validating KPIs: " + requestResponse.getEntity());
        }
    }

    /**
     * This method initiates on demand calculation request.
     * 
     * @param kpisToCalculate
     *            kpis to calculate
     * @throws KpiModelVerificationException
     */
    public static void requestOnDemandCalculationAndPollState(final List<String> kpisToCalculate) throws KpiModelVerificationException {
        try {
            final JSONObject request = new JSONObject();
            request.put("source", "FLM");
            request.put("kpi_names", kpisToCalculate);
            request.put("parameters", getScheduledKpiParametersMap());
            final RestResponse<String> response = KPI_CALCULATION_REQUEST_HANDLER.sendKpiCalculationRequest(request.toJSONString());
            if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                final CalculationRequestSuccessResponse calculationRequestSuccessResponse = new Gson().fromJson(response.getEntity(),
                        CalculationRequestSuccessResponse.class);
                final String calculationId = calculationRequestSuccessResponse.getCalculationId();
                KPI_CALCULATION_STATE_HANDLER.getKpiCalculationState(calculationId);
            }
        }
        catch (final Exception e) {
            throw new KpiModelVerificationException("Error on kpi calculation", e);
        }
    }

    private static Map<String, Object> getScheduledKpiParametersMap() {
        final LocalDate today = LocalDate.now();
        return getScheduledKpiParametersMap(today);
    }

    static Map<String, Object> getScheduledKpiParametersMap(final LocalDate today) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("param.start_date_time", Timestamp.valueOf(today.atTime(2, 0)).toString());
        parameters.put("param.end_date_time", Timestamp.valueOf(today.atTime(3, 0)).toString());
        return parameters;
    }

    /**
     * This method initiates on demand calculation request.
     *
     * @param kpiToCalculate
     *            kpi to calculate
     * @throws KpiModelVerificationException
     */
    public static void requestOnDemandCalculationAndPollState(final JSONObject kpiToCalculate) throws KpiModelVerificationException {
        try {
            final RestResponse<String> response = KPI_CALCULATION_REQUEST_HANDLER.sendKpiCalculationRequest(kpiToCalculate.toJSONString());
            if (response.getStatusCode() == HttpStatus.SC_CREATED) {
                final CalculationRequestSuccessResponse calculationRequestSuccessResponse = new Gson().fromJson(response.getEntity(),
                        CalculationRequestSuccessResponse.class);
                final String calculationId = calculationRequestSuccessResponse.getCalculationId();
                KPI_CALCULATION_STATE_HANDLER.getKpiCalculationState(calculationId);
            }
        }
        catch (final Exception e) {
            throw new KpiModelVerificationException("Error calculating KPIs. ", e);
        }
    }

    private static String getKpiDefinitionRequest(final List<String> kpiDefinitionsToOverride) throws ParseException, IOException {
        final JSONParser parser = new JSONParser();
        final JSONObject requiredKpis = (JSONObject) parser.parse(ResourceLoaderUtils.getClasspathResourceAsString(REQUIRED_KPIS_FILE_PATH));
        return removedCalculationFrequency(kpiDefinitionsToOverride, requiredKpis);
    }

    private static String removedCalculationFrequency(final List<String> kpiDefinitionsToOverwrite, final JSONObject requiredKpis) {
        final JSONArray kpiDefinitions = (JSONArray) requiredKpis.get(KPI_DEFINITION_REQUEST_ATTRIBUTE_KPI_DEFINITIONS);
        kpiDefinitions.removeIf(o -> !kpiDefinitionsToOverwrite.contains(((JSONObject) o).get(KPI_DEFINITION_REQUEST_ATTRIBUTE_NAME)));
        kpiDefinitions.forEach(o -> ((JSONObject) o).remove(KPI_DEFINITION_REQUEST_ATTRIBUTE_CALCULATION_FREQUENCY));
        return requiredKpis.toJSONString();
    }
}