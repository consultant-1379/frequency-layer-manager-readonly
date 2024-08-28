/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test.util;

import static com.ericsson.oss.services.sonom.common.env.Environment.getEnvironmentValue;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.bro.notification.ActionType;
import com.ericsson.oss.services.sonom.common.rest.utils.RestExecutor;
import com.ericsson.oss.services.sonom.common.rest.utils.RestResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;

public class BackupRestoreUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRestoreUtils.class);

    private static final RestExecutor REST_EXECUTOR = new RestExecutor();

    private static final Gson GSON = new Gson();

    private static final String ERIC_CTRL_BRO_SERVICE_NAME = getEnvironmentValue("BRO_HOSTNAME", "eric-ctrl-bro");
    private static final String ERIC_CTRL_BRO_SERVICE_PORT = getEnvironmentValue("BRO_PORT", "7001");
    private static final String ERIC_CTRL_BRO_SERVICE_BASE_URL = String.format("http://%s:%d", ERIC_CTRL_BRO_SERVICE_NAME,
            Integer.parseInt(ERIC_CTRL_BRO_SERVICE_PORT));
    private static final String ERIC_CTRL_BRO_HEALTH_URL = String.format("%s/v1/health", ERIC_CTRL_BRO_SERVICE_BASE_URL);
    private static final String ERIC_CTRL_BRO_ACTION_URL = String.format("%s/v1/backup-manager/DEFAULT/action", ERIC_CTRL_BRO_SERVICE_BASE_URL);
    private static final String STATUS = "status";
    private static final String BACKUP_NAME = "ecson-backup-" + System.currentTimeMillis();

    private static final int RETRY_ATTEMPTS = Integer.parseInt(getEnvironmentValue("BRO_SERVICE_RETRIES", "5"));
    private static final int RETRY_INTERVAL_IN_MS = 10_000;

    private BackupRestoreUtils() {
    }

    public static boolean isBackupRestoreServiceHealthy() {
        Supplier<Boolean> serviceHealthy = BackupRestoreUtils::isServiceHealthy;
        serviceHealthy = Retry.decorateSupplier(getRetry(), serviceHealthy);
        return Try.ofSupplier(serviceHealthy).get();
    }

    public static boolean backupData() throws IOException {
        final String json = getActionJson(ActionType.CREATE_BACKUP);
        return postAction(json);
    }

    public static boolean restoreData() throws IOException {
        final String json = getActionJson(ActionType.RESTORE);
        return postAction(json);
    }

    public static boolean deleteBackup() throws IOException {
        final String json = getActionJson(ActionType.DELETE_BACKUP);
        return postAction(json);
    }

    private static boolean isServiceHealthy() {
        LOGGER.info("Checking if '{}' is healthy", ERIC_CTRL_BRO_SERVICE_NAME);
        try {
            final HttpGet httpGet = new HttpGet(ERIC_CTRL_BRO_HEALTH_URL);
            final RestResponse<String> restResponse = REST_EXECUTOR.sendGetRequest(httpGet);
            if (HttpStatus.SC_OK != restResponse.getStatus()) {
                return false;
            }

            final JsonObject json = GSON.fromJson(restResponse.getEntity(), JsonObject.class);
            return json.has(STATUS) && json.get(STATUS).getAsString().equalsIgnoreCase("Healthy");
        } catch (final IOException e) {
            LOGGER.warn("Failed to contact '{}'", ERIC_CTRL_BRO_HEALTH_URL, e);
            return false;
        }
    }

    private static Retry getRetry() {
        final RetryConfig config = RetryConfig.custom()
                .maxAttempts(RETRY_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(RETRY_INTERVAL_IN_MS, 1.3))
                .retryExceptions(Throwable.class)
                .build();
        return Retry.of("BackupRestoreServiceRetry", config);
    }

    private static String getActionJson(final ActionType action) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action.name());

        final JsonObject payload = new JsonObject();
        payload.addProperty("backupName", BACKUP_NAME);
        jsonObject.add("payload", payload);

        return jsonObject.toString();
    }

    private static boolean postAction(final String json) throws IOException {
        final HttpPost httpPost = new HttpPost(ERIC_CTRL_BRO_ACTION_URL);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON);
        httpPost.setEntity(new StringEntity(json));

        final RestResponse<String> restResponse = REST_EXECUTOR.sendPostRequest(httpPost);
        return HttpStatus.SC_CREATED == restResponse.getStatus();
    }

}
