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

Log.init("MaxSourceUsersMove", "101");
var sectorId = "testSectorId";
describe("Max Source users move", function() {
    maxSourceUserMoveTestCases.forEach(function (test) {
        it(test.description,  function() {
            var totalUsersToMove = maxSourceUserMove.calculateMaxSourceUserMove(test.data, sectorId);
            assert.deepEqual(test.result, totalUsersToMove);
        });
    });
});
