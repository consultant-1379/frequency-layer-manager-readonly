/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

/*
 * Task logic Screen Cells Based on UL PUSCH SINR Thresholds.
 */
var UlPuschSinrRuleScnCell = (function () {
    var sourceCellAndScreenedTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var sourceCellUlPuschSinrValue = parseFloat(source.kpis.ul_pusch_sinr_hourly);
            var ulPuschSinrRatioThreshold = parseFloat(source.settings.uplink_pusch_sinr_ratio_threshold);
            var minTargetUlPuschSinr = parseFloat(source.settings.min_target_uplink_pusch_sinr);
            var potentialTargetCells = [];

            for (var targetCell in targetList) {
                var targetCellUlPuschSinrValue = parseFloat(targetList[targetCell].kpis.ul_pusch_sinr_hourly);
                var ulPuschSinrRatio = sourceCellUlPuschSinrValue * ulPuschSinrRatioThreshold;
                if (targetCellUlPuschSinrValue < minTargetUlPuschSinr) {
                    Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to low absolute value of UL PUSCH SINR, for source cell " + source.fdn + ". Where Target Cell UL PUSCH SINR value was: " + targetCellUlPuschSinrValue);
                } else if (targetCellUlPuschSinrValue < ulPuschSinrRatio) {
                    Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to low value of UL PUSCH SINR relative to source cell " + source.fdn + ". Where Target Cell UL PUSCH SINR value was: " + targetCellUlPuschSinrValue);
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
                   Log.cellInclusion(sectorId, targets[target].ossId, targets[target].fdn, "Possible target cell after UL PUSCH SINR rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };
    return {
        sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();
