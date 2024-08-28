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
* Task logic for Determining numeric value of Step Size and distributing Users
*/
/*jshint sub:true*/
var numericStepSizeAndDistributeUsers = (function () {
    var sourceCellsWithNumericStepSizeAndDistributeUsers = function (sectorId, maxUserToMove, topRankedSourceTargetCells) {

        if (maxUserToMove < 1) {
            maxUserToMove = 1;
        }
        var TARGET_CELLS = 'targetCells';
        var SOURCE_CELL = 'sourceCell';
        var LARGE_STEP_SIZE = "large";
        var SMALL_STEP_SIZE= "small";
        var totalNumOfUsersToMove = 0;
        var sourceCell = topRankedSourceTargetCells[SOURCE_CELL];
        var targetCellCount = topRankedSourceTargetCells[TARGET_CELLS].length;

        for (var targetCellIndex = 0; targetCellIndex < targetCellCount; targetCellIndex++) {
            var targetCell = topRankedSourceTargetCells[TARGET_CELLS][targetCellIndex];
            var bandwidthToStepSizeTable = targetCell.settings.bandwidth_to_step_size_table;
            var bandwidthElements = bandwidthToStepSizeTable.split(',').map(function(item) {
                return item.trim();
            });
            var bandwidthResult = {};
            for (var bandwidthIndex in bandwidthElements) {
                var bandwidthSplit = bandwidthElements[bandwidthIndex].split('=').map(function(item) {
                    return item.trim();
                });
                bandwidthResult[bandwidthSplit[0]] = bandwidthSplit[1];
            }
            var bandwidth = targetCell.cmAttributes.bandwidth;
            var targetCellStepSize = parseInt(bandwidthResult[bandwidth]);
            if (targetCell.stepSize == SMALL_STEP_SIZE ) {
                  targetCell.numUsersToMove = targetCellStepSize.toString();
                Log.d("Setting target cell's - " + targetCell.fdn + " numUsersToMove to: " + targetCell.numUsersToMove);
            } else if (targetCell.stepSize == LARGE_STEP_SIZE) {
                var optimizationSpeedFactorTable = targetCell.settings.optimization_speed_factor_table;
                var optimizationSpeedFactorElements = optimizationSpeedFactorTable.split(',').map(function(item) {
                    return item.trim();
                });
                var optimizationSpeedFactorResult = {};
                for (var optimizationSpeedFactorIndex in optimizationSpeedFactorElements) {
                    var optimizationSpeedFactorSplit = optimizationSpeedFactorElements[optimizationSpeedFactorIndex].split('=');
                    optimizationSpeedFactorResult[optimizationSpeedFactorSplit[0]] = optimizationSpeedFactorSplit[1];
                }
                var optimizationSpeed = optimizationSpeedFactorResult[targetCell.settings.optimization_speed];
                var usersToMove = Math.round(targetCell.kpis.target_cell_capacity/optimizationSpeed);
                var maxOfCalculatedAndBandwidth = Math.max(usersToMove, targetCellStepSize);
                targetCell.numUsersToMove = maxOfCalculatedAndBandwidth.toString();
                Log.d("Setting target cell's - " + targetCell.fdn + " numUsersToMove to: " + targetCell.numUsersToMove);
            }
            totalNumOfUsersToMove = totalNumOfUsersToMove + parseInt(targetCell.numUsersToMove);
        }
        /*
          If the total number of users to move to the target cells is greater than the max source user move,
          distribute the max source user move across each cell as a proportion of the users to move calculated above.
          If there is only 1 target cell, then the total users to move equals the num users to move, hence we give the MSU
          to that target cell.
        */
        if (totalNumOfUsersToMove > maxUserToMove) {
            var totalUser = 0.0;
            var targetCellWithHighestUserMove;
            var maxNumberOfUsersToMove = -1;
            for (var index = 0; index < targetCellCount; index++) {
                var targetCellToUpdate = topRankedSourceTargetCells[TARGET_CELLS][index];
                if (parseInt(targetCellToUpdate.numUsersToMove) > maxNumberOfUsersToMove) {
                    maxNumberOfUsersToMove = parseInt(targetCellToUpdate.numUsersToMove);
                    targetCellWithHighestUserMove = targetCellToUpdate;
                }
                targetCellToUpdate.numUsersToMove = Math.round(parseInt(targetCellToUpdate.numUsersToMove) * maxUserToMove / totalNumOfUsersToMove).toString();
                totalUser = totalUser + parseFloat(targetCellToUpdate.numUsersToMove);
                Log.d("Setting target cell's - " + targetCellToUpdate.fdn + " numUsersToMove to: " + targetCellToUpdate.numUsersToMove);
            }
            if(totalUser == 0){
                targetCellWithHighestUserMove.numUsersToMove = Math.round(maxUserToMove).toString();
            }
        }
        topRankedSourceTargetCells[TARGET_CELLS] = topRankedSourceTargetCells[TARGET_CELLS].filter(function(u) { return u.numUsersToMove != '0';});
        return topRankedSourceTargetCells;
    };
    return {
        sourceCellsWithNumericStepSizeAndDistributeUsers: sourceCellsWithNumericStepSizeAndDistributeUsers
    };
})();