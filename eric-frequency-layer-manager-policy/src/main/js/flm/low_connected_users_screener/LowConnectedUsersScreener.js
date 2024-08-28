/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

var LowConnectedUsersScreening = (function () {
    var screenedSourceAndTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells = [];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var minConnectedUsers = parseInt(source.settings.min_connected_users);
            var actualConnectedUsers = parseInt(source.kpis.connected_users);
            if (actualConnectedUsers < minConnectedUsers){
                Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as its connected users: " + actualConnectedUsers + " is below the threshold: " + minConnectedUsers + ".");
            } else {
                var sourceAndTargetCellsMap = {};
                sourceAndTargetCellsMap.sourceCell = source;
                sourceAndTargetCellsMap.targetCells = targetList;
                potentialSourceAndTargetCells.push(sourceAndTargetCellsMap);
            }
        }
        if (potentialSourceAndTargetCells.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual source or target cells being screened out.");
        }  
        return potentialSourceAndTargetCells;
    };
    return {
        screenedSourceAndTargetCells: screenedSourceAndTargetCells
    };
})();