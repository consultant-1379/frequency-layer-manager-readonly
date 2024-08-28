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

/*
* This class handles running the logic required to find the possible source and target cells, it is part of FlmPolicy's IdentifySourceAndTargetCellsTask.
* A proposedLbq will be declared in this class and will be passed to the final state, AddLbq, to be sent to the FlmPolicyOuputEvent.
* The proposedLbq is hardcoded for now.
*/
var optimizationCellsArray = IOUtils.getInput("optimizationCells");
optimizationCellsArray = JSON.parse(optimizationCellsArray);
var sectorId = IOUtils.getSectorId();
var executionId = IOUtils.getInput("executionId");

Log.init("IdentifySourceAndTargetCells", executionId);
Log.i("Starting Task Execution on " + Log.getInstanceName() + " State");

var sourceCells = SourceAndTargetCellIdentifier.possibleSourceAndTargetCells(optimizationCellsArray, sectorId);

IOUtils.setOutput("sectorId", sectorId);
IOUtils.setOutput("executionId", executionId);
IOUtils.setOutput("sourceCells", JSON.stringify(sourceCells));
IOUtils.setOutput("optimizationCells", JSON.stringify(optimizationCellsArray));
