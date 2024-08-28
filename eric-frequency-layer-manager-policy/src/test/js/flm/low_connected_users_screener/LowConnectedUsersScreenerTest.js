/*
 * *------------------------------------------------------------------------------
 * ******************************************************************************
 *  COPYRIGHT Ericsson 2022
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 * ******************************************************************************
 * ------------------------------------------------------------------------------
 */

Log.init("LowConnectedUsersScreener", "101");
var sectorId = "TestSectorId";
describe("Testing Low Connected Users Screening on source cells", function() {
    lowConnectedUsersScreeningTestCells.forEach(function (test) {
        it(test.description, function() {
            var screenedSourceAndTargetCells = LowConnectedUsersScreening.screenedSourceAndTargetCells(test.data, sectorId);
            assert.deepStrictEqual(test.result, screenedSourceAndTargetCells);
        });
    });
});