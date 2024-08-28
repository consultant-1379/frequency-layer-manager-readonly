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
* Task logic Determine step size and optimization speed
*/
var determineStepSizeAndOptimizationSpeed = (function () {
    var sourceCellsWithDeterminedStepSizeAndOptimizationSpeed = function (sectorId, optimizationCellsArray, sourceCells) {
        var defaultToSlowOptimizationSpeed = false;
        var firstOptimizationCellOptimizationSpeed = optimizationCellsArray[0].settings.optimization_speed;
        for (var optimizationCellIndex in optimizationCellsArray) {
            var optimizationCell = optimizationCellsArray[optimizationCellIndex];
            var optimizationCellOptimizationSpeed = optimizationCell.settings.optimization_speed;
            if ((optimizationCellOptimizationSpeed == null || optimizationCellOptimizationSpeed == "" || optimizationCellOptimizationSpeed == "null") || (firstOptimizationCellOptimizationSpeed != optimizationCellOptimizationSpeed)) {
                defaultToSlowOptimizationSpeed = true;
                Log.cellInfo(sectorId, optimizationCell.ossId, optimizationCell.fdn, "Optimization speed defaulted to 'slow' due to inconsistent optimizationSpeed setting across cells in the sector");
                break;
            }
        }
        var defaultToSmallStepSize = false;
        var SOURCE_CELL = 'sourceCell';
        var TARGET_CELLS = 'targetCells';

        var topRankedSourceTargetCells = sourceCells[0];
        var topRankedSourceCell = topRankedSourceTargetCells[SOURCE_CELL];
        var targetList = topRankedSourceTargetCells[TARGET_CELLS];

        var sourceNumValuesUsedForMcuCdfCalculationDailyKpiValue = topRankedSourceCell.kpis.num_values_used_for_mcu_cdf_calculation_daily;
        var sourceMinNumCellForCdfCalculationSetting = topRankedSourceCell.settings.min_num_cell_for_cdf_calculation;

        if (sourceNumValuesUsedForMcuCdfCalculationDailyKpiValue == null || sourceNumValuesUsedForMcuCdfCalculationDailyKpiValue == "" || sourceNumValuesUsedForMcuCdfCalculationDailyKpiValue == "null") {
            Log.cellInfo(sectorId, topRankedSourceCell.ossId, topRankedSourceCell.fdn, "Small step size chosen as numValuesUsedForMCUCdfCalculation is missing or empty");
            defaultToSmallStepSize = true;
        } else if (parseFloat(sourceNumValuesUsedForMcuCdfCalculationDailyKpiValue) < parseFloat(sourceMinNumCellForCdfCalculationSetting)) {
             Log.cellInfo(sectorId, topRankedSourceCell.ossId, topRankedSourceCell.fdn, "Small step size chosen as not enough cells for CDF calculation");
             defaultToSmallStepSize = true;
        }

        for (var targetCellIndex in targetList) {
            var targetCell = targetList[targetCellIndex];

            if (defaultToSlowOptimizationSpeed) {
                targetCell.settings.optimization_speed = "slow";
            }

            var step = "small";

            if (!defaultToSmallStepSize) {
                var maxConnectedUsersDaily = targetCell.kpis.max_connected_users_daily;
                var connectedUsers = targetCell.kpis.connected_users;
                var qosForCapacityEstimation = targetCell.settings.qos_for_capacity_estimation;
                var pFailingRMbps = targetCell.kpis.p_failing_r_mbps;
                var emptyOrMissingKpi = false;

                if (maxConnectedUsersDaily == null || maxConnectedUsersDaily == "" || maxConnectedUsersDaily == "null") {
                    Log.cellInfo(sectorId, topRankedSourceCell.ossId, topRankedSourceCell.fdn, "Small step size chosen as Max Connected users is missing or empty");
                    emptyOrMissingKpi = true;
                }

                if (!emptyOrMissingKpi && (parseFloat(maxConnectedUsersDaily) > parseFloat(connectedUsers) && parseFloat(pFailingRMbps) < parseFloat(qosForCapacityEstimation))) {
                    step = "large";
                }
            }

            targetCell.stepSize = step;
            Log.i("Setting target cell's - " + targetCell.fdn + " step size to: " + step);

            var optimizationSpeedFactorTable = targetCell.settings.optimization_speed_factor_table;
            var elements = optimizationSpeedFactorTable.split(',').map(function(item) {
                return item.trim();
            });
            var result = {};

            for (var index in elements) {
                var split = elements[index].split('=');
                result[split[0]] = split[1];
            }

            Log.d("Target cell " + targetCell.fdn + " optimization speed factor value: " + result[targetCell.settings.optimization_speed]);
        }
        return topRankedSourceTargetCells;
    };
    return {
         sourceCellsWithDeterminedStepSizeAndOptimizationSpeed: sourceCellsWithDeterminedStepSizeAndOptimizationSpeed
    };
})();