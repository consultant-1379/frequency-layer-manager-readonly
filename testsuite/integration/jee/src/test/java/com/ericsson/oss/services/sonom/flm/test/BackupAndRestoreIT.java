/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2020 - 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.sonom.flm.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.bro.BackupRestoreConsumer;
import com.ericsson.oss.services.sonom.common.bro.BackupRestoreSubscriber;
import com.ericsson.oss.services.sonom.common.bro.notification.ActionType;
import com.ericsson.oss.services.sonom.common.bro.notification.Notification;
import com.ericsson.oss.services.sonom.common.bro.notification.NotificationStatus;
import com.ericsson.oss.services.sonom.common.kafka.exception.KafkaConsumerInstantiationException;
import com.ericsson.oss.services.sonom.common.test.rest.ResponseAssertions;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.InSequence;
import com.ericsson.oss.services.sonom.common.test.runner.ordered.OrderedTestRunner;
import com.ericsson.oss.services.sonom.flm.service.api.settings.Configuration;
import com.ericsson.oss.services.sonom.flm.test.util.BackupRestoreUtils;
import com.ericsson.oss.services.sonom.flm.test.util.ConfigurationBuilder;
import com.ericsson.oss.services.sonom.flm.test.util.ServiceHostnameAndPortProvider;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

@RunWith(OrderedTestRunner.class)
public class BackupAndRestoreIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupAndRestoreIT.class);

    private static final Gson GSON = new Gson();

    private static final String SCHEME_AND_AUTHORITY = "http://" + ServiceHostnameAndPortProvider.getFlmAlgorithmHostnameAndPort();
    private static final String CONTEXT_ROOT = "/son-om/algorithms/flm/v1";
    private static final String BASE_URI = SCHEME_AND_AUTHORITY + CONTEXT_ROOT;
    private static final String CONFIGURATIONS = "/configurations";

    private static final String CLIENT_ID = BackupAndRestoreIT.class.getSimpleName();
    private static final String GROUP_ID = BackupAndRestoreIT.class.getName();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final BackupRestoreITConsumer consumer = createConsumer();

    @BeforeClass
    public static void before() {
        BackupRestoreSubscriber.subscribe(consumer);
    }

    @Test
    @InSequence(1)
    public void setUp() {
        LOGGER.info("1:: Checking BRO Health and verifying consumer creation");

        softly.assertThat(BackupRestoreUtils.isBackupRestoreServiceHealthy())
                .as("Backup Restore Agent Health")
                .isTrue();

        softly.assertThat(consumer).isNotNull();
    }

    @Test
    @InSequence(2)
    public void createConfigurationInDatabase() throws IOException {
        LOGGER.info("2:: Creating Configuration");

        final Configuration flmConfiguration = new ConfigurationBuilder(GSON.fromJson(readJson("updateConfigurationSettings.json"), Configuration.class))
                .withNumberOfGroups(0).build();

        final String weekendDays = getWeekendDays();
        flmConfiguration.setWeekendDays(getWeekendDays());
        flmConfiguration.setSchedule("1 1 1 1 JAN ? 2099");
        flmConfiguration.setOpenLoop(true);

        final Response putResponse = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/1"), flmConfiguration);
        ResponseAssertions.assertThat(putResponse).hasStatusCode(HttpStatus.SC_OK);

        final Configuration configuration = getConfiguration().orElseThrow(() -> new IllegalStateException("0 Configurations returned"));
        softly.assertThat(configuration).isNotNull();
        softly.assertThat(configuration.getWeekendDays()).isEqualTo(weekendDays);
        softly.assertThat(configuration.isOpenLoop()).isTrue();
        softly.assertThat(configuration.getSchedule()).isEqualTo("1 1 1 1 JAN ? 2099");
    }

    @Test
    @InSequence(3)
    public void performBackup() throws IOException {
        LOGGER.info("3:: Performing Backup of data");

        softly.assertThat(consumer.isBackupCompleted()).isFalse();
        softly.assertThat(BackupRestoreUtils.backupData())
                .as("REST Call Backup Success")
                .isTrue();

        await()
                .pollDelay(5, TimeUnit.SECONDS)
                .atMost(360, TimeUnit.SECONDS)
                .pollInterval(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(consumer.isBackupCompleted()).isTrue());

        softly.assertThat(consumer.isBackupCompleted()).isTrue();
    }


    @Test
    @InSequence(4)
    public void modifyingContentsOfDb() throws IOException {
        LOGGER.info("4:: Modify Configuration before restore");

        Configuration flmConfiguration = getConfiguration().orElseThrow(() -> new IllegalStateException("0 Configurations returned"));
        flmConfiguration.setOpenLoop(false);

        final Response putResponse = httpPutRequest(getFlmServiceUri(CONFIGURATIONS + "/1"), flmConfiguration);

        softly.assertThat(putResponse.getStatus()).isEqualTo(HttpStatus.SC_OK);

        flmConfiguration = getConfiguration().orElseThrow(() -> new IllegalStateException("0 Configurations returned"));
        softly.assertThat(flmConfiguration.isOpenLoop()).isFalse();
    }

    @Test
    @InSequence(5)
    public void performRestore() throws IOException {
        LOGGER.info("5:: Performing Restore");

        softly.assertThat(consumer.isRestoreCompleted()).isFalse();
        softly.assertThat(BackupRestoreUtils.restoreData())
                .as("REST Call Restore Success")
                .isTrue();

        await()
                .pollDelay(5, TimeUnit.SECONDS)
                .atMost(360, TimeUnit.SECONDS)
                .pollInterval(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(consumer.isRestoreCompleted()).isTrue());

        softly.assertThat(consumer.isRestoreCompleted()).isTrue();
    }

    @Test
    @InSequence(6)
    public void validateRestoreContent() throws IOException {
        LOGGER.info("6:: Validating Restoration data");

        final Configuration configuration = getConfiguration().orElseThrow(() -> new IllegalStateException("Failed to get Configuration"));
        softly.assertThat(configuration).isNotNull();
        softly.assertThat(configuration.getWeekendDays()).isEqualTo(getWeekendDays());
        softly.assertThat(configuration.isOpenLoop()).isTrue();
        softly.assertThat(configuration.getSchedule()).isEqualTo("1 1 1 1 JAN ? 2099");
    }

    @Test
    @InSequence(999)
    public void cleanup() throws IOException {
        LOGGER.info("999:: Cleaning up database");

        softly.assertThat(BackupRestoreUtils.deleteBackup())
                .as("REST Call Backup Delete")
                .isTrue();
    }

    private static String getWeekendDays() {
        final String twoDaysAgo = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(2, ChronoUnit.DAYS).getDayOfWeek().name();
        final String oneDayAgo = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS).getDayOfWeek().name();
        return twoDaysAgo + "," + oneDayAgo;
    }

    private Optional<Configuration> getConfiguration() throws IOException {

        final Response getResponse = httpGetRequest(getFlmServiceUri(CONFIGURATIONS));
        ResponseAssertions.assertThat(getResponse).hasStatusCode(HttpStatus.SC_OK);

        final JsonArray configurations = GSON.fromJson(getResponse.readEntity(String.class), JsonArray.class);

        if (configurations.size() > 0) {
            LOGGER.info("Returned '{}' configurations", configurations.size());
            return Optional.of(GSON.fromJson(configurations.get(0).toString(), Configuration.class));
        }
        return Optional.empty();
    }

    private static String getFlmServiceUri(final String resource) {
        return BASE_URI + resource;
    }

    private static Response httpGetRequest(final String uri) {
        return buildHttpClient(uri).get();
    }

    private static Response httpPutRequest(final String uri, final Configuration configuration) {
        return buildHttpClient(uri).put(Entity.json(GSON.toJson(configuration)));
    }

    private static Invocation.Builder buildHttpClient(final String uri) {
        return ClientBuilder.newClient()
                .target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON);
    }

    private static String readJson(final String fileName) throws IOException {
        final InputStream jsonObjectStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        final List<String> linesAsList = IOUtils.readLines(jsonObjectStream, "UTF-8");
        return String.join("", linesAsList);
    }

    private static BackupRestoreITConsumer createConsumer() {
        try {
            return new BackupRestoreITConsumer();
        } catch (final KafkaConsumerInstantiationException e) {
            final String message = "Failed to create BackupRestoreITConsumer";
            LOGGER.warn(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    public static class BackupRestoreITConsumer extends BackupRestoreConsumer {
        private final AtomicBoolean backupCompleted = new AtomicBoolean(false);
        private final AtomicBoolean restoreCompleted = new AtomicBoolean(false);

        BackupRestoreITConsumer() throws KafkaConsumerInstantiationException {
            super(CLIENT_ID, GROUP_ID);
        }

        @Override
        public void processNotification(final ConsumerRecord<String, Notification> consumerRecord) {
            LOGGER.info("Notification Received: {}", consumerRecord);
            final Notification notification = consumerRecord.value();
            if (ActionType.CREATE_BACKUP == notification.getAction() && NotificationStatus.COMPLETED == notification.getStatus()) {
                backupCompleted.set(true);
            }

            if (ActionType.RESTORE == notification.getAction() && NotificationStatus.COMPLETED == notification.getStatus()) {
                restoreCompleted.set(true);
            }
        }

        boolean isBackupCompleted() {
            return backupCompleted.get();
        }

        boolean isRestoreCompleted() {
            return restoreCompleted.get();
        }
    }

}

