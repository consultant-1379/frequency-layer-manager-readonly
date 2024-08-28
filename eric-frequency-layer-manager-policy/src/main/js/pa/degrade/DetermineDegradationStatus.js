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
* Task logic for determining degradations for performance assurance
*/
var DetermineDegradationStatus = (function () {
    var degradationStatus = {};

    var determineIfSectorDegraded = function (sector, paExecutionId, paWindow) {
        var sectorId = sector.sectorId;
        degradationStatus.verdict = "NOT DEGRADED";
        degradationStatus.degradedSectorKpis = {};
        degradationStatus.degradedCellKpis = {};
        var numHoursDegrade = parseInt(sector.settings.numberOfKpiDegradedHoursThreshold);
        if(isValueValidNumber(numHoursDegrade)) {
            Log.d("numberOfKpiDegradedHoursThreshold setting for sector " + sectorId + " is: " + numHoursDegrade);
            processSectorLevelKpis(sector, numHoursDegrade, paExecutionId, paWindow);
            processCells(sector, numHoursDegrade, paExecutionId, paWindow);
            Log.sectorInfo(sectorId, "The sector has "  + degradationStatus.verdict + " during PA window " + paWindow);
        }else {
            Log.sectorWarn(sectorId, "Setting numberOfKpiDegradedHoursThreshold not present in input event for sector. " + 
                "The sector will not be evaluated for degradation");
        }
        return degradationStatus;
    };

     var processSectorLevelKpis = function(sector, numHoursDegrade, paExecutionId, paWindow) {
        var sectorKpiMap = sector.kpis;
        var sectorId = sector.sectorId;
        var kpiNames = Object.keys(sectorKpiMap);
        for(var kpiName in kpiNames){
            var currentKpiName = kpiNames[kpiName];
            if(sectorKpiMap[currentKpiName].enabled == true) {
                var kpiValues = sectorKpiMap[currentKpiName].kpiValue;
                if(kpiValues != "null" && kpiValues != null){
                    var lowerLimit = parseFloat(sectorKpiMap[currentKpiName].lowerRangeLimit);
                    var upperLimit = parseFloat(sectorKpiMap[currentKpiName].upperRangeLimit);
                    checkForDegradationInSectorLevelKpi(sectorId, currentKpiName, kpiValues, numHoursDegrade, lowerLimit, upperLimit, paWindow);
                }
            } else {
                Log.sectorInfo(sectorId, "Sector level KPI '" + currentKpiName + "' for this sector is not enabled and " +
                    "will not be checked for degradation during PA execution");
            }
        }
    };

    var checkForDegradationInSectorLevelKpi = function (sectorId, kpiName, kpiValues, numHoursDegrade, lowerLimit, upperLimit, paWindow) {
        var degradedTimestamps = [];
        for (var kpi in kpiValues) {
            var kpiValue = parseFloat(kpiValues[kpi].value);
            var kpiTimestamp = kpiValues[kpi].timestamp;
            var kpiThreshold = parseFloat(kpiValues[kpi].threshold);
            if (isValueValidNumber(kpiValue)){
                if (kpiValue >= lowerLimit && kpiValue <= upperLimit){
                    if (kpiValue < kpiThreshold) {
                        degradedTimestamps.push(kpiTimestamp);
                    }
                } else {
                    Log.sectorInfo(sectorId, "Sector level KPI: '" + kpiName + "' for this sector has value '" + kpiValue +
                        "' which is outside of acceptable range ('" + lowerLimit + "' to '" + upperLimit +
                        "') for timestamp '" + kpiTimestamp + "' during PA window '"  + paWindow + "'");
                }
            } else {
                Log.unmonitoredKpiWarn(sectorId, kpiName, paWindow, kpiTimestamp);
            }
        }
        if (degradedTimestamps.length >= numHoursDegrade) {
            degradationStatus.verdict = "DEGRADED";
            Log.sectorInfo(sectorId, "Sector level KPI: '" + kpiName + "' for this sector has degraded during PA window " + paWindow);
            degradationStatus.degradedSectorKpis[kpiName] = {sectorIdToDegradedTimestamps:{}};
            degradationStatus.degradedSectorKpis[kpiName].sectorIdToDegradedTimestamps[sectorId] = degradedTimestamps;
        }
    };

    var processCells = function(sector, numHoursDegrade, paExecutionId, paWindow) {
       var cells = sector.cells;
       var sectorId = sector.sectorId;
       for(var cell in cells){
            var currentCell = cells[cell];
            var cellKpiMap = currentCell.kpis;
            var kpiNames = Object.keys(cellKpiMap);
            for(var kpiName in kpiNames){
                var currentKpiName = kpiNames[kpiName];
                if(cellKpiMap[currentKpiName].enabled == true) {
                    var kpiValues = cellKpiMap[currentKpiName].kpiValue;
                    if(kpiValues != "null" && kpiValues != null){
                        var relevanceThreshold = cellKpiMap[currentKpiName].relevanceThreshold;
                        var relevanceThresholdType = cellKpiMap[currentKpiName].relevanceThresholdType;
                        checkForDegradationInCellLevelKpi(sectorId, currentCell.fdn, currentCell.ossId, currentKpiName, kpiValues,
                            numHoursDegrade, relevanceThreshold, relevanceThresholdType.toUpperCase(), paWindow);
                    }
                } else {
                    Log.cellInfo(sectorId, currentCell.ossId, currentCell.fdn, "Cell level KPI '" + currentKpiName +
                        "' is not enabled and will not be checked for degradation during PA execution " + paExecutionId);
                }
            }
       }
    };

    var checkForDegradationInCellLevelKpi = function (sectorId, fdn, ossId, kpiName, kpiValues, numHoursDegrade, kpiRelevanceThreshold, relevanceThresholdType, paWindow) {
        var degradedTimestamps = [];
        for(var kpi in kpiValues) {
            var kpiValue = parseFloat(kpiValues[kpi].value);
            var kpiThreshold = parseFloat(kpiValues[kpi].threshold);
            var kpiTimestamp = kpiValues[kpi].timestamp;
            if(isValueValidNumber(kpiValue)){
                if(kpiValue >= 0 && kpiValue <= 100){
                    if(relevanceThresholdType == "MAX"){
                        if(kpiValue > kpiRelevanceThreshold ) {
                            if(kpiValue > kpiThreshold) {
                                degradedTimestamps.push(kpiTimestamp);
                            }
                        } else {
                            Log.d("KPI: '" + kpiName + "' for cell FDN: '" + fdn + "' and OSS ID: '" + ossId +
                            "' satisfies relevance threshold and will not be checked for degradation");
                        }
                    } else if(relevanceThresholdType == "MIN") {
                        if(kpiValue < kpiRelevanceThreshold ) {
                            if(kpiValue < kpiThreshold){
                                degradedTimestamps.push(kpiTimestamp);
                            }
                        }else {
                            Log.d("KPI: '" + kpiName + "' for cell FDN: '" + fdn + "' and OSS ID: '" + ossId +
                            "' satisfies relevance threshold and will not be checked for degradation");
                        }
                    } else {
                        Log.w("Threshold type '" + relevanceThresholdType + "' is not valid for KPI '" + kpiName +
                        "' for cell FDN '" + fdn + "' with OSS ID '" + ossId + "'. Valid values are 'MIN', 'MAX'");
                    }
                } else {
                    Log.cellInfo(sectorId, ossId, fdn, "Cell level KPI: '" + kpiName + "' has value '" + kpiValue +
                        "' which is outside of acceptable range ('0' to '100') for timestamp '" +
                        kpiTimestamp + "' during PA window '"  + paWindow + "'");
                }
            } else {
                Log.unmonitoredKpiWarn(sectorId, kpiName, paWindow, kpiTimestamp, fdn, ossId);
            }
        }
        if(degradedTimestamps.length >= numHoursDegrade) {
            degradationStatus.verdict = "DEGRADED";
            Log.cellInfo(sectorId, ossId, fdn, "Cell level KPI: '" + kpiName + "' has degraded during PA window "  + paWindow);
            if(degradationStatus.degradedCellKpis[kpiName] == null) {
                degradationStatus.degradedCellKpis[kpiName] = {ossIdToFdnToDegradedTimestamps:{}};
            }
            if(degradationStatus.degradedCellKpis[kpiName].ossIdToFdnToDegradedTimestamps[ossId] == null) {
                degradationStatus.degradedCellKpis[kpiName].ossIdToFdnToDegradedTimestamps[ossId] = {};
            }
            degradationStatus.degradedCellKpis[kpiName].ossIdToFdnToDegradedTimestamps[ossId][fdn] = degradedTimestamps;
        }
    };

    var isValueValidNumber = function(kpiValue){
        return (kpiValue != null && kpiValue != "null" && !isNaN(kpiValue));
    };

    return {
        determineIfSectorDegraded: determineIfSectorDegraded
    };
})();
