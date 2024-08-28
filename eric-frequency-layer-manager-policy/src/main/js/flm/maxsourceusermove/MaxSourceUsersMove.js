/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2021 - 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

/*jshint sub:true*/
var maxSourceUserMove = (function() {
    var calculateMaxSourceUserMove = function(sourceAndTargetCells, sectorId) {
        var SOURCE_CELL = 'sourceCell';
        var TARGET_CELLS = 'targetCells';
        var probabilityValues = [];
        var topRankedCells = sourceAndTargetCells[0];
        var sourceCell = topRankedCells[SOURCE_CELL];
        var targetCells = topRankedCells[TARGET_CELLS];
        var CAIMC_ENABLED = sourceCell.cmAttributes.caimc;

        var sourceCellProbability = parseFloat(sourceCell.kpis.p_failing_r_mbps);
        for (var i = 0; i < targetCells.length; i++) {
            var targetCell = targetCells[i];
            var targetCelProbability = parseFloat(targetCell.kpis.p_failing_r_mbps);
            probabilityValues.push(targetCelProbability);
        }

        var percentageEndcUsers = sourceCell.kpis.percentage_endc_users;
        var lowestProbabilityInTargetCelList = Math.min.apply(Math, probabilityValues);
        var probDelta = sourceCellProbability - lowestProbabilityInTargetCelList;
        var connectedUserSourceCell = parseFloat(sourceCell.kpis.connected_users);
        var endcSpid115UesSourceCell = sourceCell.kpis.endc_spid115_ues;
        var totalNoOfUsersToMove = 0;

        if (endcSpid115UesSourceCell == null || endcSpid115UesSourceCell == "" || endcSpid115UesSourceCell == "null") {
            endcSpid115UesSourceCell = 0;
        } else {
            endcSpid115UesSourceCell = parseFloat(endcSpid115UesSourceCell);
            if (endcSpid115UesSourceCell < 0) {
                Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "KPI endc_spid115_ues is negative : " + endcSpid115UesSourceCell);
                endcSpid115UesSourceCell = 0;
            }
        }

        if (percentageEndcUsers == null || percentageEndcUsers == "" || percentageEndcUsers == "null") {
            percentageEndcUsers = 0;
        } else {
            percentageEndcUsers = parseFloat(percentageEndcUsers);
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "KPI percentageEndcUsers detected for fdn. Max Source Users will be adjusted.");
        }

        if (CAIMC_ENABLED == "ACTIVATED") {
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "Capabilty Aware Idle Mode Control is ACTIVATED");
            totalNoOfUsersToMove = (((connectedUserSourceCell)*(1-(percentageEndcUsers/100)))*probDelta);
            totalNoOfUsersToMove = Math.round(totalNoOfUsersToMove * 100)/100;
        } else if (CAIMC_ENABLED == "DEACTIVATED" && endcSpid115UesSourceCell > 0) {
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "Capabilty Aware Idle Mode Control is DEACTIVATED. Falling back to SPID115");
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "KPI endc_spid115_ues detected for fdn. Max Source Users will be adjusted");
            totalNoOfUsersToMove = (connectedUserSourceCell - endcSpid115UesSourceCell) * probDelta;
        } else if (endcSpid115UesSourceCell > 0) {
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "KPI endc_spid115_ues detected for fdn. Max Source Users will be adjusted");
            totalNoOfUsersToMove = (connectedUserSourceCell - endcSpid115UesSourceCell) * probDelta;
        } else {
            Log.cellInfo(sectorId, sourceCell.ossId, sourceCell.fdn, "SPID115 and Capabilty Aware Idle Mode Control are not defined");
            totalNoOfUsersToMove = connectedUserSourceCell * probDelta;
        }

        Log.i("Sector ID : " + sectorId + " , Number of Connected Users : " + connectedUserSourceCell + ", endc_spid115_ues : " + sourceCell.kpis.endc_spid115_ues + " , Delta : " + probDelta + " , Max source users move : " + totalNoOfUsersToMove + " , percentage_endc_users : " + percentageEndcUsers );
        return totalNoOfUsersToMove;
    };

    return {
        calculateMaxSourceUserMove: calculateMaxSourceUserMove
    };
})();