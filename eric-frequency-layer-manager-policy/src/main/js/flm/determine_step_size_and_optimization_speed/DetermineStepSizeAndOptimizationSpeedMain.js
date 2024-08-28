/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

var optimizationCellsArray = IOUtils.getInput("optimizationCells");
optimizationCellsArray = JSON.parse(optimizationCellsArray);

var sourceCells = IOUtils.getInput("sourceCells");
sourceCells = JSON.parse(sourceCells);

var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");
var maxUserToMove = "" + IOUtils.getInput("maxUserToMove").toString();
maxUserToMove = parseFloat(maxUserToMove);

Log.init("DetermineStepSizeAndOptimizationSpeed", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");
Log.i("Sector ID is : " + sectorId);

var topRankedSourceTargetCells = determineStepSizeAndOptimizationSpeed.sourceCellsWithDeterminedStepSizeAndOptimizationSpeed(sectorId, optimizationCellsArray, sourceCells);

IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("maxUserToMove", maxUserToMove);
IOUtils.setOutput("optimizationCells", JSON.stringify(optimizationCellsArray));
IOUtils.setOutput("topRankedSourceTargetCells", JSON.stringify(topRankedSourceTargetCells));