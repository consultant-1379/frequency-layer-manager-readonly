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
 * Task logic for screening out cells where the app_coverage_reliability KPI is false.
 */
var AppCoverageReliabilityScreener = (function () {
    var filterCells = function (sectorId, optimizationCellsArray) {

        var validOptimizationCellsArray = [];
        for (var index in optimizationCellsArray) {
            var optimizationCell = optimizationCellsArray[index];
            var appCoverageReliability = parseBoolean(optimizationCell.kpis.app_coverage_reliability);

            if (appCoverageReliability) {
                validOptimizationCellsArray.push(optimizationCell);
            } else {
                Log.cellExclusion(sectorId, optimizationCell.ossId, optimizationCell.fdn, "Cell excluded due to unreliable KPI for App Coverage.");
            }
        }
        return validOptimizationCellsArray;
    };

    var parseBoolean = function (attribute) {
        return attribute.toLowerCase() === "true";
    };

    return {
        filterCells: filterCells
    };
})();