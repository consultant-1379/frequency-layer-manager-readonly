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


var optimizationCellsArray = IOUtils.getInput("optimizationCells");
optimizationCellsArray = JSON.parse(optimizationCellsArray);
var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");
var ACTION_PROPOSE = "ACTION_PROCEED_WITH_OPTIMIZATION";
var ACTION_SKIP = "ACTION_SKIP_OPTIMIZATION";

Log.init("GF_Sector_Screener", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");

var proceedWithOptimization = GoalFunScrSecr.isOptimizationRequired(optimizationCellsArray,sectorId);
IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("optimizationCells", JSON.stringify(optimizationCellsArray));
IOUtils.setOutput("ProceedWithOptimization", proceedWithOptimization);