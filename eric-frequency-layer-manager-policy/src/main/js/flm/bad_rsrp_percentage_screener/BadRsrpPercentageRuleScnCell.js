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
* Task logic Screen Cells Based on Bad RSRP Percentage Rule
*/
var BadRsrpPercentageRuleScnCell = (function () {
    var sourceCellAndScreenedTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var sourceCellsAndTargetCellMapResult =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var percentageBadRsrpRatioThreshold = parseFloat(source.settings.percentage_bad_rsrp_ratio_threshold);
            var potentialTargetCells = [];

            if(!isNull(percentageBadRsrpRatioThreshold)) {
                for (var targetCell in targetList) {
                    if (isBadRsrpPercentageLessThanThreshold(targetList, targetCell, source, sectorId)) {
                        potentialTargetCells.push(targetList[targetCell]);
                    }
                }
                if (potentialTargetCells.length == 0) {
                    Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as all target cells are screened out.");
                } else {
                    var sourceCellsAndTargetCellMap = {};
                    sourceCellsAndTargetCellMap.sourceCell = source;
                    sourceCellsAndTargetCellMap.targetCells = potentialTargetCells;
                    sourceCellsAndTargetCellMapResult.push(sourceCellsAndTargetCellMap);
                }
            } else {
                Log.cellUnexpectedExceptionExclusion(sectorId, source.ossId, source.fdn, "Cell excluded as source due to Bad RSRP Percentage Ratio threshold value missing or empty");
            }
        }

        if (sourceCellsAndTargetCellMapResult.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        } else {
            for (var cellList in sourceCellsAndTargetCellMapResult) {
                var src = sourceCellsAndTargetCellMapResult[cellList].sourceCell;
                var targets = sourceCellsAndTargetCellMapResult[cellList].targetCells;
                for (var target in targets) {
                    Log.cellInclusion(sectorId, src.ossId, targets[target].fdn, "Possible target cell after bad rsrp percentage rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return sourceCellsAndTargetCellMapResult;
    };

    var isBadRsrpPercentageLessThanThreshold = function(targetList, targetCell, source, sectorId) {
        var percentageBadRsrpRatioThreshold = parseFloat(source.settings.percentage_bad_rsrp_ratio_threshold);

        var target_num_samples_rsrp_q1 = parseFloat(targetList[targetCell].kpis.num_samples_rsrp_ta_q1);
        var target_num_samples_rsrp_q2 = parseFloat(targetList[targetCell].kpis.num_samples_rsrp_ta_q2);
        var target_num_samples_rsrp_q3 = parseFloat(targetList[targetCell].kpis.num_samples_rsrp_ta_q3);
        var target_num_samples_rsrp_q4 = parseFloat(targetList[targetCell].kpis.num_samples_rsrp_ta_q4);

        var target_num_bad_samples_rsrp_q1 = parseFloat(targetList[targetCell].kpis.num_bad_samples_rsrp_ta_q1);
        var target_num_bad_samples_rsrp_q2 = parseFloat(targetList[targetCell].kpis.num_bad_samples_rsrp_ta_q2);
        var target_num_bad_samples_rsrp_q3 = parseFloat(targetList[targetCell].kpis.num_bad_samples_rsrp_ta_q3);
        var target_num_bad_samples_rsrp_q4 = parseFloat(targetList[targetCell].kpis.num_bad_samples_rsrp_ta_q4);

        var source_num_samples_rsrp_q1 = parseFloat(source.kpis.num_samples_rsrp_ta_q1);
        var source_num_samples_rsrp_q2 = parseFloat(source.kpis.num_samples_rsrp_ta_q2);
        var source_num_samples_rsrp_q3 = parseFloat(source.kpis.num_samples_rsrp_ta_q3);
        var source_num_samples_rsrp_q4 = parseFloat(source.kpis.num_samples_rsrp_ta_q4);

        var source_num_bad_samples_rsrp_q1 = parseFloat(source.kpis.num_bad_samples_rsrp_ta_q1);
        var source_num_bad_samples_rsrp_q2 = parseFloat(source.kpis.num_bad_samples_rsrp_ta_q2);
        var source_num_bad_samples_rsrp_q3 = parseFloat(source.kpis.num_bad_samples_rsrp_ta_q3);
        var source_num_bad_samples_rsrp_q4 = parseFloat(source.kpis.num_bad_samples_rsrp_ta_q4);

        var source_bad_rsrp_percentage_num = 0;
        var source_bad_rsrp_percentage_den = 0;
        var source_bad_rsrp_percentage = 0;
        var target_bad_rsrp_percentage_num = 0;
        var target_bad_rsrp_percentage_den = 0;
        var target_bad_rsrp_percentage = 0;

        var source_distance_q4 = parseFloat(source.kpis.distance_q4);
        var target_distance_q4 = parseFloat(targetList[targetCell].kpis.distance_q4);

        if (isNull(target_distance_q4) || isNull(source_distance_q4)) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty kpi distance_q4");
            return false;
        }

        if (source_distance_q4 < target_distance_q4) {
            var sourceCellDistances = generateMapOfDistances(source);
            source_bad_rsrp_percentage = calculateBadRsrpPercentage(sourceCellDistances, target_distance_q4);

            var targetCellDistances = generateMapOfDistances(targetList[targetCell]);
            target_bad_rsrp_percentage = calculateBadRsrpPercentage(targetCellDistances, source_distance_q4);
            Log.d("SamplesOverlapPercentage of source cell, " + source.fdn + ", is: " + target_bad_rsrp_percentage);
        } else if (source_distance_q4 > target_distance_q4) {
            var targetCellDistanceValues = generateMapOfDistances(targetList[targetCell]);
            target_bad_rsrp_percentage = calculateBadRsrpPercentage(targetCellDistanceValues, source_distance_q4);

            var sourceCellDistanceValues = generateMapOfDistances(source);
            source_bad_rsrp_percentage = calculateBadRsrpPercentage(sourceCellDistanceValues, target_distance_q4);
            Log.d("SamplesOverlapPercentage of source cell, " + source.fdn + ", is: " + source_bad_rsrp_percentage);
        } else {
            source_bad_rsrp_percentage_num = source_num_bad_samples_rsrp_q1 + source_num_bad_samples_rsrp_q2 + source_num_bad_samples_rsrp_q3 + source_num_bad_samples_rsrp_q4;
            source_bad_rsrp_percentage_den = source_num_samples_rsrp_q1 + source_num_samples_rsrp_q2 + source_num_samples_rsrp_q3 + source_num_samples_rsrp_q4;
            source_bad_rsrp_percentage = source_bad_rsrp_percentage_num / source_bad_rsrp_percentage_den;

            target_bad_rsrp_percentage_num = target_num_bad_samples_rsrp_q1 + target_num_bad_samples_rsrp_q2 + target_num_bad_samples_rsrp_q3 + target_num_bad_samples_rsrp_q4;
            target_bad_rsrp_percentage_den = target_num_samples_rsrp_q1 + target_num_samples_rsrp_q2 + target_num_samples_rsrp_q3 + target_num_samples_rsrp_q4;
            target_bad_rsrp_percentage = target_bad_rsrp_percentage_num / target_bad_rsrp_percentage_den;
        }

        if (target_bad_rsrp_percentage == -1) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty distance kpi.");
            return false;
        }

        if (source_bad_rsrp_percentage == -1) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty distance kpi for source cell " + source.fdn);
            return false;
        }

        if (target_bad_rsrp_percentage == -2) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty bad_rsrp_percentage kpi.");
            return false;
        }

        if (source_bad_rsrp_percentage == -2) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty bad_rsrp_percentage kpi for source cell " + source.fdn);
            return false;
        }

        var targetSourceRatio = target_bad_rsrp_percentage / source_bad_rsrp_percentage;

        if(isNaN(targetSourceRatio) && target_bad_rsrp_percentage == 0 && source_bad_rsrp_percentage == 0) {
            return true;
        } else if(!isFinite(targetSourceRatio) || targetSourceRatio > percentageBadRsrpRatioThreshold) {
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to poor DL coverage relative to source cell " + source.fdn + ". Where Target Source ratio was: " + targetSourceRatio);
            return false;
        } else {
            return true;
        }
    };


    var calculateBadRsrpPercentage = function(cellDistances, distance_q4) {
        var previousQValue = 0;
        var numSamplesRsrp = 0;
        var numSamplesBadRsrp = 0;
        for (var cellKey in cellDistances) {
            var cellDistance = cellDistances[cellKey].distance;
            var cellNumSamplesRsrp = cellDistances[cellKey].num_samples_rsrp_ta;
            var cellNumSamplesBadRsrp = cellDistances[cellKey].num_bad_samples_rsrp_ta;
            if(isNull(cellDistance)){
                return -1;
            }
            if(isNull(cellNumSamplesRsrp) || isNull(cellNumSamplesBadRsrp)){
                return -2;
            }
            if (cellDistance > distance_q4) {
                var percentageDiff = (distance_q4 - previousQValue) / (cellDistance - previousQValue);
                numSamplesRsrp += (percentageDiff * cellNumSamplesRsrp);
                numSamplesBadRsrp += (percentageDiff * cellNumSamplesBadRsrp);
                Log.d("Percentage difference of the stopping quarter: " + percentageDiff);
                break;
            } else {
                numSamplesRsrp += cellNumSamplesRsrp;
                numSamplesBadRsrp += cellNumSamplesBadRsrp;
                previousQValue = cellDistance;
            }
        }
        var badRsrpPercentage = numSamplesBadRsrp / numSamplesRsrp;
        return badRsrpPercentage;
    };

    var isNull = function(value) {
        if(value === null || value === "" || value === "null" || isNaN(value)){
            return true;
        }
        return false;
    };

    var generateMapOfDistances = function(cell) {
        var cellDistancesMap = {};

        cellDistancesMap.q1 = {
            "distance": parseFloat(cell.kpis.distance_q1),
            "num_samples_rsrp_ta": parseFloat(cell.kpis.num_samples_rsrp_ta_q1),
            "num_bad_samples_rsrp_ta": parseFloat(cell.kpis.num_bad_samples_rsrp_ta_q1)
        };

        cellDistancesMap.q2 = {
            "distance": parseFloat(cell.kpis.distance_q2),
            "num_samples_rsrp_ta": parseFloat(cell.kpis.num_samples_rsrp_ta_q2),
            "num_bad_samples_rsrp_ta": parseFloat(cell.kpis.num_bad_samples_rsrp_ta_q2)
        };

        cellDistancesMap.q3 = {
            "distance": parseFloat(cell.kpis.distance_q3),
            "num_samples_rsrp_ta": parseFloat(cell.kpis.num_samples_rsrp_ta_q3),
            "num_bad_samples_rsrp_ta": parseFloat(cell.kpis.num_bad_samples_rsrp_ta_q3)
        };

        cellDistancesMap.q4 = {
            "distance": parseFloat(cell.kpis.distance_q4),
            "num_samples_rsrp_ta": parseFloat(cell.kpis.num_samples_rsrp_ta_q4),
            "num_bad_samples_rsrp_ta": parseFloat(cell.kpis.num_bad_samples_rsrp_ta_q4)
        };

        return cellDistancesMap;
    };

    return {
         sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();