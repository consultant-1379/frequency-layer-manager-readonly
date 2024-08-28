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

var TransienceScreening = (function () {
    var checkCellsForTransience = function (possibleSourceAndTargetCells) {
        var potentialSourceAndTargetCells = [];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var potentialTargetCells = [];
            var sourceAndTargetCellsMap = {};
            if (areThresholdsPresent(source)) {
                if (!isTransient(source, true) && lowerThresholdLessThanUpper(source)) {
                    for (var target in targetList) {
                        var targetCell = targetList[target];
                        if (areThresholdsPresent(targetCell)) {
                            if (!isTransient(targetCell, false) && lowerThresholdLessThanUpper(targetCell)) {
                                potentialTargetCells.push(targetList[target]);
                            } else {
                                Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target as Probability Transient Low for source cell " + source.fdn + ", where Probability Transient was: " + targetCell.kpis.p_failing_r_mbps_detrended + ".");
                            }
                        } else {
                            potentialTargetCells.push(targetList[target]);
                            Log.i("Transient not calculated, thresholds for target cell " + targetCell.fdn + " not set.");
                        }
                    }
                    if (potentialTargetCells.length == 0) {
                         Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as all target cells are screened out.");
                    } else {
                        sourceAndTargetCellsMap.sourceCell = source;
                        sourceAndTargetCellsMap.targetCells = potentialTargetCells;
                        potentialSourceAndTargetCells.push(sourceAndTargetCellsMap);
                    }
                } else {
                    Log.cellExclusion(sectorId, source.ossId, source.fdn, "Cell excluded as source as Probability Transient High, where Probability Transient was: " + source.kpis.p_failing_r_mbps_detrended + ".");
                }
            } else {
                sourceAndTargetCellsMap.sourceCell = source;
                sourceAndTargetCellsMap.targetCells = targetList;
                potentialSourceAndTargetCells.push(sourceAndTargetCellsMap);
                Log.i("Transient not calculated, thresholds for source cell " + source.fdn + " not set.");
            }
        }
        if (potentialSourceAndTargetCells.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        } else {
            for (var cellList in potentialSourceAndTargetCells) {
                var src = potentialSourceAndTargetCells[cellList].sourceCell;
                var targets = potentialSourceAndTargetCells[cellList].targetCells;
                for (var t in targets) {
                    Log.cellInclusion(sectorId, targets[t].ossId, targets[t].fdn, "Possible target cell after transience rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };

    var areThresholdsPresent = function (cell) {
        if ((cell.kpis.lower_threshold_for_transient == null) || (cell.kpis.lower_threshold_for_transient == "") || (cell.kpis.lower_threshold_for_transient == "null")) {
            return false;
        }

        if ((cell.kpis.upper_threshold_for_transient == null) || (cell.kpis.upper_threshold_for_transient == "") || (cell.kpis.upper_threshold_for_transient == "null")) {
            return false;
        }

        if ((cell.kpis.p_failing_r_mbps_detrended == null) || (cell.kpis.p_failing_r_mbps_detrended == "") || (cell.kpis.p_failing_r_mbps_detrended == "null")) {
            return false;
        }
        return true;
    };

    var isTransient = function (cell, isSource) {
        var lowerBreached = false;
        var upperBreached = false;

        if (parseFloat(cell.kpis.lower_threshold_for_transient) > parseFloat(cell.kpis.p_failing_r_mbps_detrended)) {
            lowerBreached = true;
        }

        if (parseFloat(cell.kpis.upper_threshold_for_transient) < parseFloat(cell.kpis.p_failing_r_mbps_detrended)) {
            upperBreached = true;
        }

        if (upperBreached && isSource) {
            Log.d("Source " + cell.fdn + " has breached upper threshold for transient " + cell.kpis.upper_threshold_for_transient);
            return true;
        }

        if (lowerBreached && !isSource) {
            Log.d("Target " + cell.fdn + " has breached lower threshold for transient " + cell.kpis.lower_threshold_for_transient);
            return true;
        }
        return false;
    };

    var lowerThresholdLessThanUpper = function (cell) {
        if (parseFloat(cell.kpis.lower_threshold_for_transient) <= parseFloat(cell.kpis.upper_threshold_for_transient)) {
            return true;
        }
        Log.e("Lower threshold for transient " + cell.kpis.lower_threshold_for_transient + " is greater than upper threshold for transient " + cell.kpis.upper_threshold_for_transient);
        return false;
    };

    return {
        checkCellsForTransience: checkCellsForTransience
    };

})();
