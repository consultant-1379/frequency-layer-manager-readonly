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
* Task logic for Screen Target Cells Based on % ENDC User
*/

var ENDCUserRuleScnCell = (function () {
    var sourceCellAndScreenedTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var sourceENDCThreshold = parseFloat(source.settings.lb_threshold_for_endc_users);
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;

            var potentialTargetCells = [];
                for (var targetCell in targetList) {
                    var targetCellENDCPercentageValue = parseFloat(targetList[targetCell].kpis.percentage_endc_users);
                    if (isNaN(targetCellENDCPercentageValue) || targetCellENDCPercentageValue <= sourceENDCThreshold) {
                        if (isNaN(targetCellENDCPercentageValue)) {
                            Log.cellInclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Skipping EN-DC screening due to % EN-DC users kpi not available in target cell.");
                        }
                        potentialTargetCells.push(targetList[targetCell]);
                    } else {
                        Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to high % EN-DC users for source cell " + source.fdn + ", where target % EN-DC was: " + targetCellENDCPercentageValue +", and source threshold was: " + sourceENDCThreshold + ".");
                    }
                }
                if (potentialTargetCells.length == 0) {
                    Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as all target cells are screened out due to high % EN-DC users.");
                } else {
                    var sourceAndTargetCellsMap = {};
                    sourceAndTargetCellsMap.sourceCell = source;
                    sourceAndTargetCellsMap.targetCells = potentialTargetCells;
                    potentialSourceAndTargetCells.push(sourceAndTargetCellsMap);
                }
        }
        if (potentialSourceAndTargetCells.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded due to high % EN-DC users.");
        } else {
            for (var cellList in potentialSourceAndTargetCells) {
                var src = potentialSourceAndTargetCells[cellList].sourceCell;
                var targets = potentialSourceAndTargetCells[cellList].targetCells;
                for (var target in targets) {
                   Log.cellInclusion(sectorId, targets[target].ossId, targets[target].fdn, "Possible target cell after EN-DC rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };
    return {
        sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();