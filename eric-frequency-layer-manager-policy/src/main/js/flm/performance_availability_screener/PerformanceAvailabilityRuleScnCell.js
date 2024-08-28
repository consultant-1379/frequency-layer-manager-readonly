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
 * Task logic Screen Cells Based on performance and availability rule.
 */

var performanceAvailabilityRuleScnCell = (function () {
    var sourceCellAndScreenedTargetCells = function (possibleSourceAndTargetCells, sectorId) {
        var potentialSourceAndTargetCells =[];
        for (var sourceTargetList in possibleSourceAndTargetCells) {
            var source = possibleSourceAndTargetCells[sourceTargetList].sourceCell;
            var targetList = possibleSourceAndTargetCells[sourceTargetList].targetCells;
            var potentialTargetCells = [];
            for (var targetCell in targetList) {
                if (!validateTrgtCellForPerformanceAndAvailability(sectorId, targetList[targetCell], source)) {
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
                     Log.cellInclusion(sectorId, targets[target].ossId, targets[target].fdn, "Possible target cell after performance and availability screening for source cell " + src.fdn + ".");
                }
            }
        }
        return potentialSourceAndTargetCells;
    };

    var validateTrgtCellForPerformanceAndAvailability = function (sectorId, targetCell, source) {
        var settings = source.settings;
        var kpis = targetCell.kpis;

        if(validateLessThan(kpis.initial_and_added_e_rab_establishment_sr, settings.lb_threshold_for_initial_erab_estab_succ_rate)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to breach of Accessibility threshold for source cell " + source.fdn + ", where Accessibility was: " + kpis.initial_and_added_e_rab_establishment_sr + ".");
            return true;
        }
        if(validateLessThan(kpis.initial_and_added_e_rab_establishment_sr_for_qci1, settings.lb_threshold_for_initial_erab_estab_succ_rate_for_qci1)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to breach of Accessibility for QCI1 threshold for source cell " + source.fdn + ", where Accessibility for QCI1 was: " + kpis.initial_and_added_e_rab_establishment_sr_for_qci1 + ".");
            return true;
        }
        if(validateGreaterThan(kpis.e_rab_retainability_percentage_lost, settings.lb_threshold_for_erab_percentage_lost)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to breach of E-RAB Retainability threshold for source cell " + source.fdn + ", where E-RAB Retainability was: " + kpis.e_rab_retainability_percentage_lost + ".");
            return true;
        }
        if(validateGreaterThan(kpis.e_rab_retainability_percentage_lost_qci1, settings.lb_threshold_for_erab_percentage_lost_for_qci1)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to breach of E-RAB Retainability for QCI1 threshold for source cell " + source.fdn + ", where E-RAB Retainability for QCI1 was: " + kpis.e_rab_retainability_percentage_lost_qci1 + ".");
            return true;
        }
        if(validateLessThan(kpis.cell_handover_success_rate, settings.lb_threshold_for_cell_ho_succ_rate)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to breach of Cell HO Success Rate threshold for source cell " + source.fdn + ", where Cell Handover Success Rate was: " + kpis.cell_handover_success_rate + ".");
            return true;
        }

        if (kpis.cell_availability == null) {
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn ,"Cell excluded as target due to missing Cell Availability for source cell " + source.fdn + ".");
            return true;
        }
        if (kpis.cell_availability == "" || kpis.cell_availability == "null") {
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn ,"Cell excluded as target due to empty Cell Availability for source cell " + source.fdn + ".");
            return true;
        }

        if(validateLessThan(kpis.cell_availability, settings.lb_threshold_for_cell_availability)){
            Log.cellExclusion(sectorId, targetCell.ossId, targetCell.fdn, "Cell excluded as target due to low Cell Availability for source cell " + source.fdn + ", where Cell Availability was: " + kpis.cell_availability + ".");
            return true;
        }
        return false;
    };

    var validateLessThan = function (firstVar, secondVar) {
      firstVar = parseFloat(firstVar);
      secondVar = parseFloat(secondVar);
      if(isNaN(firstVar) || isNaN(secondVar)) {
         return false;
      }
      return firstVar < secondVar;
    };

    var validateGreaterThan = function (firstVar, secondVar) {
         firstVar = parseFloat(firstVar);
         secondVar = parseFloat(secondVar);
         if(isNaN(firstVar) || isNaN(secondVar)) {
           return false;
         }
          return firstVar > secondVar;
        };

    return {
        sourceCellAndScreenedTargetCells: sourceCellAndScreenedTargetCells
    };
})();
