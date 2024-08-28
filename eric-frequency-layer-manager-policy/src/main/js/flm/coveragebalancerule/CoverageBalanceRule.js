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
* Task logic Screen Cells Based on Coverage Balance Rule
*/
var CoverageBalanceRule = (function () {
    var sourceCellsAndScreenTargetCellWithCoverageBalanceRule = function (possibleSourceAndTargetCells, sectorId) {
        var sourceCellsAndTargetCellMapResult =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var sourceCellCoverageBalanceValue = parseFloat(source.kpis.coverage_balance_ratio_distance);
            var potentialTargetCells = [];
            if (sourceCellCoverageBalanceValue != 0) {
                var coverageBalanceThreshold = parseFloat(source.settings.target_source_coverage_balance_ratio_threshold);
                var synthCounterReliabilityThresholdSource = parseInt(source.settings.synthetic_counters_cell_reliability_threshold_in_rops);
                if(isReliabilityAboveThresholdForSource(source, synthCounterReliabilityThresholdSource, sectorId)){
                    for (var targetCell in targetList) {
                        var synthCounterReliabilityThresholdTarget = parseInt(targetList[targetCell].settings.synthetic_counters_cell_reliability_threshold_in_rops);
                        if(isReliabilityAboveThresholdForTarget(targetList, targetCell, synthCounterReliabilityThresholdTarget, sectorId)){
                            var targetCellCoverageBalanceValue = parseFloat(targetList[targetCell].kpis.coverage_balance_ratio_distance);
                            var coverageBalanceRatio = targetCellCoverageBalanceValue / sourceCellCoverageBalanceValue;
                            if (isThresholdMet(coverageBalanceRatio, coverageBalanceThreshold)) {
                                potentialTargetCells.push(targetList[targetCell]);
                            }else {
                                if (calculateSamplesOverlapPercentage(targetList, targetCell, source, sectorId)) {
                                    potentialTargetCells.push(targetList[targetCell]);
                                }
                            }
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
                }
            }else {
                Log.cellExclusion(sectorId, source.ossId, source.fdn, "Source cell excluded as source Coverage Balance Distance value is zero.");
            }
        }
        if (sourceCellsAndTargetCellMapResult.length == 0) {
            Log.sectorExclusion(sectorId, "Sector is excluded as all source cells for the sector are excluded, due to all their individual target cells being screened out.");
        } else {
            for (var cellList in sourceCellsAndTargetCellMapResult) {
                var src = sourceCellsAndTargetCellMapResult[cellList].sourceCell;
                var targets = sourceCellsAndTargetCellMapResult[cellList].targetCells;
                for (var target in targets) {
                    Log.cellInclusion(sectorId, src.ossId, targets[target].fdn, "Possible target cell after coverage balance rule screening for source cell " + src.fdn + ".");
                }
            }
        }
        return sourceCellsAndTargetCellMapResult;
    };

    var isReliabilityAboveThresholdForSource =  function(source, threshold, sectorId) {
        var synthCounterReliabilitySource = parseInt(source.kpis.synthetic_counter_cell_reliability_daily);
        if(isNull(synthCounterReliabilitySource)){
            Log.cellExclusion(sectorId, source.ossId, source.fdn, "Cell excluded as source due to synthetic counter cell reliability value missing or empty");
            return false;
        }
        if(isNull(threshold)){
            Log.cellUnexpectedExceptionExclusion(sectorId, source.ossId, source.fdn, "Cell excluded as source due to synthetic counter cell reliability threshold value missing or empty");
            return false;
        }
        if (synthCounterReliabilitySource < threshold){
            Log.cellExclusion(sectorId, source.ossId, source.fdn, "Cell excluded as source due to synthetic counter cell reliability threshold not met where reliability was: " + synthCounterReliabilitySource + " and threshold was: " + threshold);
            return false;
        }
        return true;
    };

    var isReliabilityAboveThresholdForTarget =  function(targetList, targetCell, threshold, sectorId) {
        var synthCounterReliabilityTarget = parseInt(targetList[targetCell].kpis.synthetic_counter_cell_reliability_daily);
        if(isNull(synthCounterReliabilityTarget)){
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to synthetic counter cell reliability value missing or empty");
            return false;
        }
        if(isNull(threshold)){
            Log.cellUnexpectedExceptionExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to synthetic counter cell reliability threshold value missing or empty");
            return false;
        }
        if (synthCounterReliabilityTarget < threshold){
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to synthetic counter cell reliability threshold not met where reliability was: " + synthCounterReliabilityTarget + " and threshold was: " + threshold);
            return false;
        }
        return true;
    };

    var calculateSamplesOverlapPercentage = function(targetList, targetCell, source, sectorId) {
        var sourceCellStSampleThreshold = parseFloat(source.settings.source_target_samples_overlap_threshold);
        var stSamplesOverlapPercentage = 0;
        var target_distance_q4 = parseFloat(targetList[targetCell].kpis.distance_q4);
        if(isNull(target_distance_q4)){
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty kpi distance_q4");
            return false;
        }
        var sourceCellDistances = generateMapOfDistances(source);
        var previousQValue = 0;
        for (var sourceCellKey in sourceCellDistances) {
            var sourceCellDistance = sourceCellDistances[sourceCellKey].distance;
            if(isNull(sourceCellDistance)){
                Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to empty kpi distance_q1 for source cell " + source.fdn);
                return false;
            }
            if (isTargetCellInSourceTAQuarter(sourceCellDistance, target_distance_q4)) {
                var percentageDiff = (target_distance_q4 - previousQValue) / (sourceCellDistance - previousQValue);
                stSamplesOverlapPercentage += (percentageDiff * sourceCellDistances[sourceCellKey].ue_percentage);
                Log.d("Percentage difference of the stopping quarter: " + percentageDiff);
                break;
            } else {
                stSamplesOverlapPercentage += sourceCellDistances[sourceCellKey].ue_percentage;
                previousQValue = sourceCellDistance;
            }
        }
        Log.d("stSamplesOverlapPercentage of source cell, " + source.fdn + ", is: " + stSamplesOverlapPercentage);
        if(isThresholdMet(stSamplesOverlapPercentage, sourceCellStSampleThreshold)){
            return true;
        }else{
            Log.cellExclusion(sectorId, targetList[targetCell].ossId, targetList[targetCell].fdn, "Cell excluded as target due to low Coverage Balance for source cell " + source.fdn + ", where Coverage Balance was: " + stSamplesOverlapPercentage );
            return false;
        }
    };

    var isThresholdMet = function(ratio, threshold) {
        return ratio >= threshold;
    };

     var isTargetCellInSourceTAQuarter = function(sourceCellDistance, target_distance_q4) {
        return sourceCellDistance > target_distance_q4;
    };

    var isNull = function(value) {
        if(value == null || value == "" || value == "null" || isNaN(value)){
            return true;
        }
        return false;
    };

    var generateMapOfDistances = function(sourceCell) {
        var sourceCellDistancesMap = {};

        sourceCellDistancesMap.q1 = {
            "distance": parseFloat(sourceCell.kpis.distance_q1),
            "ue_percentage": parseFloat(sourceCell.kpis.ue_percentage_q1)
        };

        sourceCellDistancesMap.q2 = {
            "distance": parseFloat(sourceCell.kpis.distance_q2),
            "ue_percentage": parseFloat(sourceCell.kpis.ue_percentage_q2)
        };

        sourceCellDistancesMap.q3 = {
            "distance": parseFloat(sourceCell.kpis.distance_q3),
            "ue_percentage": parseFloat(sourceCell.kpis.ue_percentage_q3)
        };

        sourceCellDistancesMap.q4 = {
            "distance": parseFloat(sourceCell.kpis.distance_q4),
            "ue_percentage": parseFloat(sourceCell.kpis.ue_percentage_q4)
        };

        return sourceCellDistancesMap;
    };
    return {
         sourceCellsAndScreenTargetCellWithCoverageBalanceRule: sourceCellsAndScreenTargetCellWithCoverageBalanceRule
    };
})();