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
 * Task logic for Identifying Source and Target cells.

 * A cell is identified as a source cell if it's goal function score is less than
 * another cell's goal function score by an amount that is greater than, or equal to, the sector's goal function optimization threshold.
 *
 * All identified source cells will then be ranked by their unhappy users score. If two cells share the same unhappy users
 * score, then the cell with the lowest goal function score will be ranked higher. If this is also the same then a random cell
 * will be chosen.
 *
 * Each source cell will then identify a list of potential target cells. A cell is a target cell if the difference between
 * it's goal function score and the source cell's goal function score is greater than, or equal to, the goal function threshold.
 * When choosing a target cell we will use all cells in the sector, not a subset of cells.
 */
var SourceAndTargetCellIdentifier = (function () {
    var sourceCells = function (optimizationCellsArray) {
        var possibleSourceCells = [];
        var goalFunctionThreshold = parseFloat(optimizationCellsArray[0].settings.delta_gfs_optimization_threshold);
        for (var index1 in optimizationCellsArray) {
            for (var index2 in optimizationCellsArray) {
                if (index1 === index2) {
                    continue;
                }
                var possibleSourceGoalFunctionValue = parseFloat(optimizationCellsArray[index1].kpis.goal_function_resource_efficiency);
                var comparisonCellGoalFunctionValue = parseFloat(optimizationCellsArray[index2].kpis.goal_function_resource_efficiency);
                var goalFunctionValueDifference = comparisonCellGoalFunctionValue - possibleSourceGoalFunctionValue;

                if (goalFunctionValueDifference >= goalFunctionThreshold) {
                    possibleSourceCells.push(optimizationCellsArray[index1]);
                    break;
                }
            }
        }
        return possibleSourceCells;
    };

    var rankSourceCells = function (possibleSourceCells) {
        var possibleSourceCellsRankedByUnhappyUsers = possibleSourceCells.sort(function (a, b) {
            var sourceCellAUnhappyUsers = parseFloat(a.kpis.unhappy_users);
            var sourceCellBUnhappyUsers = parseFloat(b.kpis.unhappy_users);
            var sourceCellAGoalFunctionResourceEfficiency = parseFloat(a.kpis.goal_function_resource_efficiency);
            var sourceCellBGoalFunctionResourceEfficiency = parseFloat(b.kpis.goal_function_resource_efficiency);

            if (sourceCellAUnhappyUsers > sourceCellBUnhappyUsers) {
                return -1;
            } else if (sourceCellAUnhappyUsers < sourceCellBUnhappyUsers) {
                return 1;
            }

            if (sourceCellAGoalFunctionResourceEfficiency < sourceCellBGoalFunctionResourceEfficiency) {
                return -1;
            } else if (sourceCellAGoalFunctionResourceEfficiency > sourceCellBGoalFunctionResourceEfficiency) {
                return 1;
            }

            return 0;
        });

        return possibleSourceCellsRankedByUnhappyUsers;
    };

    var targetCells = function (possibleSourceCells, optimizationCellsArray, sectorId) {
        var sourceCellsAndPossibleTargetCells = [];
        var goalFunctionThreshold = parseFloat(optimizationCellsArray[0].settings.delta_gfs_optimization_threshold);
        var sourceCellRank = 0;
        for (var possibleSourceCell in possibleSourceCells) {
            var possibleTargetCells = [];
            sourceCellRank++;
            for (var possibleTargetCell in optimizationCellsArray) {
                if (possibleSourceCells[possibleSourceCell] === optimizationCellsArray[possibleTargetCell]) {
                    continue;
                }

                var sourceGoalFunctionValue = parseFloat(possibleSourceCells[possibleSourceCell].kpis.goal_function_resource_efficiency);
                var possibleTargetGoalFunctionValue = parseFloat(optimizationCellsArray[possibleTargetCell].kpis.goal_function_resource_efficiency);
                var goalFunctionValueDifference = possibleTargetGoalFunctionValue - sourceGoalFunctionValue;

                if (goalFunctionValueDifference >= goalFunctionThreshold) {
                    Log.cellInclusion(sectorId, possibleSourceCells[possibleSourceCell].ossId, optimizationCellsArray[possibleTargetCell].fdn, "Identified possible target cell for ranked " + sourceCellRank + " source cell " + possibleSourceCells[possibleSourceCell].fdn + ".");
                    var targetCell = JSON.stringify(optimizationCellsArray[possibleTargetCell]); // deep-copy
                    targetCell = JSON.parse(targetCell);
                    // Adding stepSize & numUsersToMove, if not done, AvroTypeExceptions are being thrown
                    targetCell.stepSize = "";
                    targetCell.numUsersToMove="";
                    possibleTargetCells.push(targetCell);
                }
            }
            var sourceCellAndTargetCell = {};
            sourceCellAndTargetCell.sourceCell = possibleSourceCells[possibleSourceCell];
            sourceCellAndTargetCell.targetCells = possibleTargetCells;
            sourceCellsAndPossibleTargetCells.push(sourceCellAndTargetCell);
        }
        return sourceCellsAndPossibleTargetCells;
    };

    // This will be called in the sourceAndTargetCellIdentifierMain.js and will run all 3 steps of identifying source, ranking and identifying targets.
    var possibleSourceAndTargetCells = function (optimizationCellsArray, sectorId) {
        var possibleSourceCells = sourceCells(optimizationCellsArray);
        var rankedSourceCells = rankSourceCells(possibleSourceCells);
        var possibleTargetCells = targetCells(rankedSourceCells, optimizationCellsArray, sectorId);
        return possibleTargetCells;
    };
    return {
        possibleSourceAndTargetCells: possibleSourceAndTargetCells
    };
})();