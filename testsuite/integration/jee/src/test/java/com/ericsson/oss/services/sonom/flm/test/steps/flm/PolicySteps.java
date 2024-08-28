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

package com.ericsson.oss.services.sonom.flm.test.steps.flm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.assertj.core.util.Lists;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.sonom.common.kafka.consumer.single.AbstractKafkaConsumer;
import com.ericsson.oss.services.sonom.flm.service.api.lbq.ProposedLoadBalancingQuanta;
import com.ericsson.oss.services.sonom.flm.service.api.targetcell.TargetCell;
import com.ericsson.oss.services.sonom.flm.service.cell.OptimizationCell;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyInputEvent;
import com.ericsson.oss.services.sonom.flm.service.events.PolicyOutputEvent;
import com.ericsson.oss.services.sonom.flm.test.helpers.CucumberData;
import com.ericsson.oss.services.sonom.flm.test.util.DataEntry;
import com.ericsson.oss.services.sonom.flm.test.util.ExclusionLogParser;
import com.ericsson.oss.services.sonom.flm.test.util.KafkaMessageBusTestUtil;
import com.ericsson.oss.services.sonom.flm.test.util.PolicyDeploymentUtil;
import com.ericsson.oss.services.sonom.flm.test.util.PolicyStepsUtil;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Class containing the steps defined for the Policy Cucumber integration tests.
 */
public class PolicySteps {
    private static final String KPI = "KPI";
    private static final String SETTING = "SETTING";
    private static final String CM_ATTRIBUTE = "CM_ATTRIBUTE";

    private PolicyInputEvent policyInputEvent;
    private PolicyOutputEvent returnedPolicyOutputEvent;

    private static final String BLANK_TARGET_CELL_FDN = "";
    private static final Integer BLANK_TARGET_CELL_OSSID = -1;
    private static final String BLANK_TARGET_USERS_MOVE = "";
    private static final String BLANK_SOURCE_CELL_FDN = "";
    private static final Integer BLANK_SOURCE_CELL_OSSID = -1;
    private static final String BLANK_SOURCE_USERS_MOVE = "";
    private static final String DEPLOY_POLICY_PAYLOAD = "policy/DeployPolicyPayload.json";

    private static Boolean awaitFlag = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicySteps.class);

    @Before
    public void beforeScenario() {
        try {
            PolicyDeploymentUtil.deployPolicy(DEPLOY_POLICY_PAYLOAD);
        } catch (final Exception e) {
            LOGGER.info("Error deploying the FLM policy", e);
        }
    }

    @Given("^Optimization Cells (.*)$")
    public void setOptimizationCells(final String id) {
        final List<OptimizationCell> optimizationCells = CucumberData.cellFactory(id);
        LOGGER.info("Setting optimization cells to {}", optimizationCells);
        PolicyStepsUtil.setOptimizationCells(optimizationCells);
    }

    @Given("Create Default Optimization Cells")
    public void createOptimizationCells(final DataTable dataTable) {
        final List<String> fdnList = dataTable.asList(String.class);

        final List<OptimizationCell> optimizationCells = createCells(fdnList);
        LOGGER.info("Setting optimization cells to {}", optimizationCells);

        PolicyStepsUtil.setOptimizationCells(optimizationCells);
    }

    @And("^Policy Input Event$")
    public void setPolicyInputEvent(final PolicyInputEvent policyInputEvent) {
        LOGGER.info("Setting input event to {}", policyInputEvent);
        this.policyInputEvent = policyInputEvent;
    }

    @And("^Missing Mandatory Optimization Cells Data$")
    public void missing_mandatory_data(final DataTable dataTable) {
        final List<DataEntry> dataEntryList = dataTable.asList(DataEntry.class);

        for (final DataEntry dataEntry : dataEntryList) {
            LOGGER.info("Deleting mandatory data type {} with name {} for fdn {}", dataEntry.getDataType(),
                    dataEntry.getDataName(), dataEntry.getFdn());

            applyConsumerOnOptimizationCellForDataEntry(dataEntry, (map) -> map.remove(dataEntry.getDataName()));
        }
    }

    @And("^Set Optimization Cells Data$")
    public void set_data(final DataTable dataTable) {

        final List<DataEntry> dataEntryList = dataTable.asList(DataEntry.class);

        for (final DataEntry dataEntry : dataEntryList) {
            LOGGER.info("Setting data type {} with name {} for fdn {} to {}", dataEntry.getDataType(),
                    dataEntry.getDataName(), dataEntry.getFdn(), dataEntry.getDataValue());

            applyConsumerOnOptimizationCellForDataEntry(dataEntry, (map) -> map.put(dataEntry.getDataName(), dataEntry.getDataValue()));
        }

    }

    @When("^Putting Policy Input Event onto Kafka Topic$")
    public void putPolicyInputEventOntoKafkaTopic() throws Exception {
        final AbstractKafkaConsumer<String, PolicyInputEvent> consumer = KafkaMessageBusTestUtil.getPolicyInputTopicConsumer();
        final AbstractKafkaConsumer<String, PolicyOutputEvent> outputConsumer = KafkaMessageBusTestUtil.getPolicyOutputTopicConsumer();

        if (!awaitFlag) {
            await("Ensure consumer is subscribed to topic and healthy.")
                    .pollDelay(15, TimeUnit.SECONDS)
                    .atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(consumer.isConsumerHealthy()).isTrue());
            await("Ensure consumer is subscribed to topic and healthy.")
                    .pollDelay(15, TimeUnit.SECONDS)
                    .atMost(30, TimeUnit.SECONDS)
                    .untilAsserted(() -> assertThat(outputConsumer.isConsumerHealthy()).isTrue());
            awaitFlag = true;
        }

        final Future<RecordMetadata> recordFuture = KafkaMessageBusTestUtil.sendPolicyInputToPolicyInputTopic(policyInputEvent);
        assertThat(recordFuture.get(15, TimeUnit.SECONDS)).isNotNull();

        with().pollInterval(Duration.ONE_SECOND)
                .await("Ensure message has been consumed by consumer.")
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> KafkaMessageBusTestUtil.hasConsumedFromPolicyInputTopic(policyInputEvent.getExecutionId()));

        final List<PolicyInputEvent> consumedPolicyInputEvents = KafkaMessageBusTestUtil
                .getLatestPolicyInputFromConsumerByExecutionId(policyInputEvent.getExecutionId());
        assertThat(consumedPolicyInputEvents).containsExactly(policyInputEvent);

        with().pollInterval(Duration.ONE_SECOND)
                .await("Ensure PolicyOutputEvent message has been consumed by consumer.")
                .atMost(30, TimeUnit.SECONDS)
                .until(() -> KafkaMessageBusTestUtil.hasConsumedFromPolicyOutputTopic(policyInputEvent.getExecutionId()));

        final List<PolicyOutputEvent> consumedPolicyOutputEvents = KafkaMessageBusTestUtil
                .getLatestPolicyOutputFromConsumerByExecutionId(policyInputEvent.getExecutionId());

        LOGGER.info("Getting the latest PolicyOutputEvent from consumed events");
        returnedPolicyOutputEvent = consumedPolicyOutputEvents.get(consumedPolicyOutputEvents.size() - 1);
        LOGGER.info("Retrieved Event {}", returnedPolicyOutputEvent);
    }

    @Then("^Optimization is skipped and Expected Proposed Load Balancing Quanta is empty$")
    public void setExpectedProposedLoadBalancingQuanta() {
        final List<TargetCell> targetCells = new ArrayList<>();
        final TargetCell emptyTargetCell = new TargetCell(BLANK_TARGET_CELL_FDN, BLANK_TARGET_CELL_OSSID, BLANK_TARGET_USERS_MOVE);
        targetCells.add(emptyTargetCell);

        final ProposedLoadBalancingQuanta proposedLoadBalancingQuanta = new ProposedLoadBalancingQuanta(BLANK_SOURCE_CELL_FDN,
                BLANK_SOURCE_CELL_OSSID, BLANK_SOURCE_USERS_MOVE, targetCells);
        LOGGER.info("Asserting Load Balancing Quanta is {}", proposedLoadBalancingQuanta);
        assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta()).isEqualTo(proposedLoadBalancingQuanta);
    }

    @And("Optimization proceeds with the following source cells")
    public void optimizationProceedsWithTheFollowingSourceCells(final ProposedLoadBalancingQuanta expectedProposedLBQ) {
        LOGGER.info("Asserting Load Balancing Quanta is {}", expectedProposedLBQ);
        PolicyStepsUtil.setProposedLoadBalancingQuanta(expectedProposedLBQ);
        assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getSourceCellFdn()).isEqualTo(expectedProposedLBQ.getSourceCellFdn());
        assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getSourceCellOssId()).isEqualTo(expectedProposedLBQ.getSourceCellOssId());
        assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getSourceUsersMove()).isEqualTo(expectedProposedLBQ.getSourceUsersMove());
        for (final TargetCell targetCell : expectedProposedLBQ.getTargetCells()) {
            assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getTargetCells()).contains(targetCell);
        }
    }

    @Then("Optimization proceeds with the following target cells")
    public void optimizationProceedsWithTheFollowingTargetCells(final List<TargetCell> expectedTargetCells) {
        LOGGER.info("Setting target cells {}", expectedTargetCells);
        PolicyStepsUtil.setTargetCells(expectedTargetCells);
        assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getTargetCells().size()).isEqualTo(expectedTargetCells.size());
        for (final TargetCell targetCell : expectedTargetCells) {
            assertThat(returnedPolicyOutputEvent.getLoadBalancingQuanta().getTargetCells()).contains(targetCell);
        }
    }

    @Given("^Imbalanced_Sector_with_(.*)_Cells_(.*)$")
    public void imbalancedSector(final String numCells, final String id) {
        final List<OptimizationCell> optimizationCells = CucumberData.cellFactory("Imbalanced_Sector_with_" + numCells + "_Cells_" + id);
        LOGGER.info("Setting optimization cells to {}", optimizationCells);
        PolicyStepsUtil.setOptimizationCells(optimizationCells);
    }

    @Given("^Balanced_Sector_with_(.*)_Cells_(.*)$")
    public void balancedSector(final String numCells, final String id) {
        final List<OptimizationCell> optimizationCells = CucumberData.cellFactory("Balanced_Sector_with_" + numCells + "_Cells_" + id);
        LOGGER.info("Setting optimization cells to {}", optimizationCells);
        PolicyStepsUtil.setOptimizationCells(optimizationCells);
    }

    @And("Sector excluded due to cell reliability threshold not met")
    public void sectorExcludedDueToCellReliabilityThresholdNotMet(final PolicyInputEvent policyInputEvent) {
        final String expectedLog = "Execution_ID: " + policyInputEvent.getExecutionId() + ", Sector_ID: " + policyInputEvent.getSectorId() + ", " +
                "Exclusion_Reason: Sector is excluded from optimization as Sector Busy Hour is unreliable.";
        await("Verifying exclusion log for cell reliability threshold.")
                .pollDelay(1, TimeUnit.SECONDS)
                .atMost(30, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(ExclusionLogParser.containsExpectedLog(expectedLog)).isTrue());
    }

    private List<OptimizationCell> createCells(final List<String> fdnList) {
        final Set<OptimizationCell> optimizationCellsSet = new HashSet<>();
        fdnList.forEach(fdn -> optimizationCellsSet.add(CucumberData.buildDefaultOptimizationCell(fdn)));
        return Lists.newArrayList(optimizationCellsSet);
    }

    private void applyConsumerOnOptimizationCellForDataEntry(final DataEntry dataEntry, final Consumer<Map<String, String>> consumer) {
        final String fdn = dataEntry.getFdn();
        switch (dataEntry.getDataType()) {
            case KPI:
                PolicyStepsUtil.getOptimizationCells().stream()
                        .filter(byFdn(fdn))
                        .map(OptimizationCell::getKpis)
                        .forEach(consumer::accept);
                break;
            case SETTING:
                PolicyStepsUtil.getOptimizationCells().stream()
                        .filter(byFdn(fdn))
                        .map(OptimizationCell::getSettings)
                        .forEach(consumer::accept);
                break;
            case CM_ATTRIBUTE:
                PolicyStepsUtil.getOptimizationCells().stream()
                        .filter(byFdn(fdn))
                        .map(OptimizationCell::getCmAttributes)
                        .forEach(consumer::accept);
                break;
            default:
                throw new IllegalArgumentException("Should handle all enums.");
        }
    }

    private Predicate<OptimizationCell> byFdn(final String fdn) {
        return optimizationCell -> optimizationCell.getFdn().equals(fdn);
    }
}
