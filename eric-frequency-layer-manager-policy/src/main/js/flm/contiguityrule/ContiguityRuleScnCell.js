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
 * Task logic Screen Cells Based on Contiguity Rule.
 */

var ContiguityRuleScnCell = (function () {
    var sourceCellAndScreenedTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var sourceCellContiguityValue = parseFloat(source.kpis.contiguity);
            var targetSourceContiguityThreshold = parseFloat(source.settings.target_source_contiguity_ratio_threshold);
            var potentialTargetCells = [];
            if (sourceCellContiguityValue != 0) {
                for (var targetCell in targetList) {
                    var targetCellContiguityValue = parseFloat(targetList[targetCell].kpis.contiguity);
                    var targetSourceContiguityRatio = targetCellContiguityValue / sourceCellContiguityValue;
                    if (targetSourceContiguityRatio >= targetSourceContiguityThreshold) {
                        potentialTargetCells.push(targetList[targetCell]);
                    } else {
                        Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to low Contiguity for source cell " + source.fdn + ", where Contiguity was: " + targetSourceContiguityRatio );
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
            } else {
               Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as source Contiguity value is zero.");
            }
        }
        if (potentialSourceAndTargetCells.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        } else {
            for (var cellList in potentialSourceAndTargetCells) {
                var src = potentialSourceAndTargetCells[cellList].sourceCell;
                var targets = potentialSourceAndTargetCells[cellList].targetCells;
                for (var target in targets) {
                   Log.cellInclusion(sectorId, targets[target].ossId, targets[target].fdn, "Possible target cell after contiguity rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };
    return {
        sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();
