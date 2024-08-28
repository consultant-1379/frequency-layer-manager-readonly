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
var sourceCells = IOUtils.getInput("sourceCells");
sourceCells = JSON.parse(sourceCells);
var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");

Log.init("Contiguity_Screener", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");

var sourceCellAndScreenedTargetCells = ContiguityRuleScnCell.sourceCellAndScreenedTargetCells(sourceCells, sectorId);
var proceedWithOptimization = sourceCellAndScreenedTargetCells.length != 0;

Log.setInstanceName("ContiguityScreener");
IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("optimizationCells", JSON.stringify(optimizationCellsArray));
IOUtils.setOutput("sourceCells", JSON.stringify(sourceCellAndScreenedTargetCells));
IOUtils.setOutput("ProceedWithOptimization", proceedWithOptimization);