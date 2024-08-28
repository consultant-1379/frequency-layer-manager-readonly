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

Log.init("ProceedWithOrSkipOptimization", executionId);
Log.d("Executing Decision Logic");

var ACTION_PROCEED_WITH_OPTIMIZATION = "ACTION_PROCEED_WITH_OPTIMIZATION";
var ACTION_SKIP = "ACTION_SKIP_OPTIMIZATION";
var proceedWithOptimization = IOUtils.getInput("ProceedWithOptimization");

Log.d("ProceedWithOptimization is " + proceedWithOptimization);

if(proceedWithOptimization == true) {
   IOUtils.setNextState(ACTION_PROCEED_WITH_OPTIMIZATION);
} else {
   IOUtils.setNextState(ACTION_SKIP);
}
Log.d("End of Decision Logic");
