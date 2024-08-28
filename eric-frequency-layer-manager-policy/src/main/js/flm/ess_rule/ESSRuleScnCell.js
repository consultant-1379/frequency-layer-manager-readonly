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
* Task logic for Screening Target Cells Based on ESS
*/

var ESSRuleScnCell = (function() {
    var sourceCellAndScreenedTargetCells = function(possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells = [];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;

            var potentialTargetCells = [];
            for (var targetCell in targetList) {
                var targetESSThreshold = targetList[targetCell].settings.ess_enabled;
                var targetlteNrSpectrumShared = targetList[targetCell].cmAttributes.lteNrSpectrumShared;
                if (targetESSThreshold == "t" && targetlteNrSpectrumShared == "yes") {
                    Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target as cell is ESS.");
                } else {
                    potentialTargetCells.push(targetList[targetCell]);
                }
            }
            if (potentialTargetCells.length == 0) {
                Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as all target cells are screened out.");
            } else {
                var sourceAndTargetCellsMap = {};
                sourceAndTargetCellsMap.sourceCell = source;
                sourceAndTargetCellsMap.targetCells = potentialTargetCells;
                potentialSourceAndTargetCells.push(sourceAndTargetCellsMap);
            }
        }
        if (potentialSourceAndTargetCells.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        } else {
            for (var cellList in potentialSourceAndTargetCells) {
                var src = potentialSourceAndTargetCells[cellList].sourceCell;
                var targets = potentialSourceAndTargetCells[cellList].targetCells;
                for (var target in targets) {
                    Log.cellInclusion(sectorId, targets[target].ossId, targets[target].fdn, "Possible target cell after ESS rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };
    return {
        sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();