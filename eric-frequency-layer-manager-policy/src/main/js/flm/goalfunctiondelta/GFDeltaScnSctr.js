/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

/*
* Task logic for Goal Function Delta Sector Screen
*/
var GoalFunScrSecr = (function () {
    var isOptimizationRequired = function (optimizationElementsArray,sectorId) {
        var goalFunResEffList = [];
        var rThroughPutFirstCellValue = parseFloat(optimizationElementsArray[0].settings.target_throughput_r);
        var goalFunDeltaThresholdFirstCellValue = parseFloat(optimizationElementsArray[0].settings.delta_gfs_optimization_threshold);
        for (var index in optimizationElementsArray) {
            var rValueInLoop = parseFloat(optimizationElementsArray[index].settings.target_throughput_r);
            var deltaThresholdInLoopValue = parseFloat(optimizationElementsArray[index].settings.delta_gfs_optimization_threshold);
            if (rThroughPutFirstCellValue !=  rValueInLoop  ) {
                Log.sectorExclusion(sectorId , "Sector excluded due to inconsistent Target Throughput R(targetThroughputR(Mbps)).");
                return false;
            } else if (goalFunDeltaThresholdFirstCellValue != deltaThresholdInLoopValue) {
                Log.sectorExclusion(sectorId , "Sector excluded due to inconsistent Goal Function Score Delta Optimization Threshold (deltaGFSOptimizationThreshold).");
                return false;
            } else {
                goalFunResEffList.push(parseFloat(optimizationElementsArray[index].kpis.goal_function_resource_efficiency));
            }
         }
        var min = Math.min.apply(null, goalFunResEffList);
        var max = Math.max.apply(null, goalFunResEffList);
        var diff = max - min;
        if (diff >= goalFunDeltaThresholdFirstCellValue) {
           Log.i("Sector : " + sectorId + ".  The Delta GFS exceeds the configured threshold value:  "+ goalFunDeltaThresholdFirstCellValue +" ");
           return true;
        } else {
           Log.sectorExclusion(sectorId ,"Sector not suitable for optimization as Goal Function Score Delta Threshold (deltaGFSOptimizationThreshold) is not met, where the Delta GFS was: " + diff + ", and the threshold was: " + goalFunDeltaThresholdFirstCellValue);
        }
        return false;
        };
    return {
    isOptimizationRequired: isOptimizationRequired
    };
})();