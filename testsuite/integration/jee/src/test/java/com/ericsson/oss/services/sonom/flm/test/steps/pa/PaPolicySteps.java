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

package com.ericsson.oss.services.sonom.flm.test.steps.pa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.awaitility.Duration;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.single.AbstractKafkaConsumer;
import com.ericsson.oss.services.sonom.flm.database.pa.execution.PAExecutionDao;
import com.ericsson.oss.services.sonom.flm.pa.policy.FlmPaPolicyInputEventHandler;
import com.ericsson.oss.services.sonom.flm.service.api.pa.PAExecution;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.cell.Cell;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.degradation.DegradationStatus;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.elements.sector.Sector;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.performance.assurance.events.PaPolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.test.util.KafkaMessageBusTestUtil;
import com.ericsson.oss.services.sonom.flm.test.util.PaPolicyStepsUtil;
import com.ericsson.oss.services.sonom.flm.test.util.PolicyDeploymentUtil;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PaPolicySteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaPolicySteps.class);
    private static final String DEPLOY_PA_POLICY_PAYLOAD = "policy/DeployPaPolicyPayload.json";
    private static final PAExecutionDao dao = Mockito.mock(PAExecutionDao.class);
    private static final FlmPaPolicyInputEventHandler eventHandler = new FlmPaPolicyInputEventHandler(dao);

    private static PaPolicyInputEvent paInputEvent;
    private static PaPolicyOutputEvent paOutputEvent;
    private static Boolean awaitFlag = false;

    @Before
    public void beforeScenario() {
        try {
            PolicyDeploymentUtil.deployPolicy(DEPLOY_PA_POLICY_PAYLOAD);
            Mockito.when(dao.update(Mockito.any())).thenReturn(1);
        } catch (final Exception e) {
            LOGGER.info("Error deploying the FLM PA policy", e);
        }
    }

    @Given("^The Cells In The Sector$")
    public void createCells(final DataTable dataTable) {
        final List<Cell> cells = dataTable.asList(Cell.class);
        LOGGER.info("Setting cells to {}", cells);
        PaPolicyStepsUtil.setCells(cells);
    }

    @And("^The Sector For The PA Input Event$")
    public void createSector(final Sector sector) {
        LOGGER.info("Setting sector to '{}'", sector);
        PaPolicyStepsUtil.setSector(sector);
    }

    @And("^The PA Policy Input Event$")
    public void createPaPolicyInputEvent(final PaPolicyInputEvent policyInputEvent) {
        LOGGER.info("The policy input event is '{}'", policyInputEvent);
        paInputEvent = policyInputEvent;
    }

    @When("^The PA Policy Input event Is Published To Kafka$")
    public void publishInputEventToKafka() throws Exception {
        final Timestamp testTimestamp = new Timestamp(System.currentTimeMillis());
        final PAExecution execution = new PAExecution(paInputEvent.getPaExecutionId(), 1, null, testTimestamp,
                testTimestamp, testTimestamp, paInputEvent.getFlmExecutionId());

        final AbstractKafkaConsumer<String, PaPolicyOutputEvent> outputConsumer = KafkaMessageBusTestUtil.getPaPolicyOutputTopicConsumer();
        if (!awaitFlag) {
            await("Ensure consumer is subscribed to topic and healthy")
                    .pollDelay(15, TimeUnit.SECONDS)
                    .atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(outputConsumer.isConsumerHealthy()).isTrue());
            awaitFlag = true;
        }

        eventHandler.sendToKafkaTopic(Arrays.asList(paInputEvent.toJson()), execution);

        with().pollInterval(Duration.ONE_SECOND)
                .await("Ensure PaPolicyOutputEvent message has been consumed by output consumer")
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> KafkaMessageBusTestUtil.hasConsumedFromPaPolicyOutputTopic(paInputEvent.getPaExecutionId()));

        final List<PaPolicyOutputEvent> consumedPaPolicyOutputEvents = KafkaMessageBusTestUtil
                .getLatestPaPolicyOutputFromConsumerByPaExecutionId(paInputEvent.getPaExecutionId());

        LOGGER.info("Getting the latest PaPolicyOutputEvent from consumed events");
        paOutputEvent = consumedPaPolicyOutputEvents.get(consumedPaPolicyOutputEvents.size() - 1);
        LOGGER.info("Retrieved Event {}", paOutputEvent);
    }

    @Then("^The PA Policy Output Event Shows The KPIs That Have Degraded$")
    public void checkStatusOfDegradedSector(final DegradationStatus degradationStatus) {
        LOGGER.info("Expected Degradation status: '{}'", degradationStatus);
        assertThat(paOutputEvent.getDegradationStatus()).isEqualTo(degradationStatus);
    }

    @Then("^The PA Policy Output Event Shows No KPIs Have Degraded$")
    public void checkStatusOfNotDegradedSector() {
        final DegradationStatus degradationStatus = new DegradationStatus("NOT DEGRADED",
                new HashMap<>(0), new HashMap<>(0));
        assertThat(paOutputEvent.getDegradationStatus()).isEqualTo(degradationStatus);
    }

}
