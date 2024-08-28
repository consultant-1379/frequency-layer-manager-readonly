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

var optimizationCellsArray = IOUtils.getInput("optimizationCells");
optimizationCellsArray = JSON.parse(optimizationCellsArray);
var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");

Log.init("InputEventIntegrityScreener", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");
Log.i("Received input message with sector ID: " + sectorId);

var filteredOptimizationCells = EmptyOrMissingScreener.filterCells(sectorId, optimizationCellsArray);
var proceedWithOptimization = filteredOptimizationCells.length != 0;
if (proceedWithOptimization == false) {
    Log.sectorExclusion(sectorId, "Sector is excluded from optimization as none of the cells have complete information.");
} else {
    proceedWithOptimization = ReliabilityScreener.isOptimizationCellsReliable(sectorId, optimizationCellsArray);
}

IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("optimizationCells", JSON.stringify(filteredOptimizationCells));
IOUtils.setOutput("ProceedWithOptimization", proceedWithOptimization);
