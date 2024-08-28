/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

/*
 * Task logic for screening out cells with missing mandatory kpis, settings or cm attributes.
 */
var ReliabilityScreener = (function () {

    var isOptimizationCellsReliable = function (sectorId, optimizationCellsArray) {
        var unreliableOptimizationCells = [];
        for (var index in optimizationCellsArray) {
            var optimizationCell = optimizationCellsArray[index];
            var cellReliabilityThreshold = parseInt(optimizationCell.settings.num_calls_cell_hourly_reliability_threshold_in_hours);
            var cellReliability = parseInt(optimizationCell.kpis.kpi_cell_reliability_daily);
            if (cellReliability < cellReliabilityThreshold) {
                Log.cellInfo(sectorId, optimizationCell.ossId, optimizationCell.fdn, "Cell excluded due to cell reliability value '" +
                    cellReliability + "' is less than threshold value '" + cellReliabilityThreshold + "'");
                unreliableOptimizationCells.push(optimizationCell);
            }
        }
        if(unreliableOptimizationCells.length > 0){
            Log.sectorExclusion(sectorId, "Sector is excluded from optimization as Sector Busy Hour is unreliable.");
            return false;
        }
        return true;
    };
    return {
         isOptimizationCellsReliable: isOptimizationCellsReliable
    };
 })();
