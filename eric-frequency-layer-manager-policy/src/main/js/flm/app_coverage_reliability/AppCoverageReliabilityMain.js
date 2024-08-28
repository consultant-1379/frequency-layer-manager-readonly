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

Log.init("AppCoverageReliabilityScreener ", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");

var filteredOptimizationCells = AppCoverageReliabilityScreener.filterCells(sectorId, optimizationCellsArray);
var proceedWithOptimization = filteredOptimizationCells.length > 0;
if (proceedWithOptimization == false) {
    Log.sectorExclusion(sectorId, "Sector is excluded from optimization as all cells have unreliable KPI for App Coverage.");
}

IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("optimizationCells", JSON.stringify(filteredOptimizationCells));
IOUtils.setOutput("ProceedWithOptimization", proceedWithOptimization);
