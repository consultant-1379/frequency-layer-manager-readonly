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

/*
* Task logic for AddLbqTask, which takes in the cells to be optimized and returns them as an output event
*/
var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");
var proceedWithOptimization = IOUtils.getInput("ProceedWithOptimization");
var topRankedSourceTargetCells = IOUtils.getInput("topRankedSourceTargetCells");
topRankedSourceTargetCells = JSON.parse(topRankedSourceTargetCells);

Log.init("Add LBQ", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");

var proposedLoadBalancingQuanta = addLbq.populateLbq(sectorId, proceedWithOptimization, topRankedSourceTargetCells);

IOUtils.setOutput("proposedLoadBalancingQuanta", JSON.stringify(proposedLoadBalancingQuanta));