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

var addLbq = (function () {
    var populateLbq = function (sectorId, proceedWithOptimization, topRankedSourceTargetCells) {
         var proposedLBQObject;

         if (proceedWithOptimization == false) {
             proposedLBQObject = {};
             proposedLBQObject.sourceCellFdn = "";
             proposedLBQObject.sourceCellOssId = -1;
             proposedLBQObject.sourceUsersMove = "";
             proposedLBQObject.targetCells = new Array(1);

             var emptyLBQTargetCell = {};
             emptyLBQTargetCell.targetCellFdn = "";
             emptyLBQTargetCell.targetCellOssId = -1;
             emptyLBQTargetCell.targetUsersMove = "";

             proposedLBQObject.targetCells[0] = emptyLBQTargetCell;
             Log.sectorInfo(sectorId, "Proposed LBQ: " + JSON.stringify(proposedLBQObject));
         } else {
             var sourceCell = topRankedSourceTargetCells.sourceCell;
             var targetCells = topRankedSourceTargetCells.targetCells;

             proposedLBQObject = {};
             proposedLBQObject.sourceCellFdn = sourceCell.fdn;
             proposedLBQObject.sourceCellOssId = sourceCell.ossId;
             proposedLBQObject.targetCells = new Array(targetCells.length);

             var totalTargetUsersToMove = 0;

             for (var i in targetCells) {
                 var targetCell = targetCells[i];
                 var lbqTargetCell = {};
                 lbqTargetCell.targetCellFdn = targetCell.fdn;
                 lbqTargetCell.targetCellOssId = targetCell.ossId;

                 var targetNumUsersMove = targetCell.numUsersToMove;
                 lbqTargetCell.targetUsersMove = targetNumUsersMove;
                 totalTargetUsersToMove += parseInt(targetNumUsersMove);
                 proposedLBQObject.targetCells[i] = lbqTargetCell;
             }

             proposedLBQObject.sourceUsersMove = "" + totalTargetUsersToMove.toString();
             Log.sectorInfo(sectorId, "Proposed LBQ: " + JSON.stringify(proposedLBQObject));
             Log.sectorInfo(sectorId, "LBQ calculation complete");
         }
         return proposedLBQObject;
    };
    return {
        populateLbq: populateLbq
    };
})();
