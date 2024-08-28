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

var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");
var optimizationCellsArray = IOUtils.getInput("optimizationCells");
optimizationCellsArray = JSON.parse(optimizationCellsArray);
var maxUserToMove = "" + IOUtils.getInput("maxUserToMove").toString();
maxUserToMove = parseFloat(maxUserToMove);
var topRankedSourceTargetCells = IOUtils.getInput("topRankedSourceTargetCells");
topRankedSourceTargetCells = JSON.parse(topRankedSourceTargetCells);
var proceedWithOptimization = true;

Log.init("NumericStepSizeAndDistributeUsers", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");
Log.d("Sector ID is " + sectorId);
if(maxUserToMove < 0.5){
    Log.sectorExclusion(sectorId , "Sector excluded due to Source Cell maxUserToMove < 0.5.");
    proceedWithOptimization = false;
} else {
    topRankedSourceTargetCells = numericStepSizeAndDistributeUsers.sourceCellsWithNumericStepSizeAndDistributeUsers(sectorId, maxUserToMove, topRankedSourceTargetCells);
}

IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("optimizationCells", JSON.stringify(optimizationCellsArray));
IOUtils.setOutput("topRankedSourceTargetCells", JSON.stringify(topRankedSourceTargetCells));
IOUtils.setOutput("ProceedWithOptimization", proceedWithOptimization);