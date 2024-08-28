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

var flmExecutionId = IOUtils.getInput("flmExecutionId");
var paExecutionId = IOUtils.getInput("paExecutionId");
var paWindow = IOUtils.getInput("paWindow");
var sector = IOUtils.getInput("sector");
sector = JSON.parse(sector);

Log.init("FLM_PA_Determine_Degradation_Status", paExecutionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");
Log.i("Received input message with sector ID: " + sector.sectorId);

var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(sector, paExecutionId, paWindow);

IOUtils.setOutput("flmExecutionId", flmExecutionId);
IOUtils.setOutput("paExecutionId", paExecutionId);
IOUtils.setOutput("sector", JSON.stringify(sector));
IOUtils.setOutput("degradationStatus", JSON.stringify(degradationStatus));
