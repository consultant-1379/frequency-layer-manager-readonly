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

Log.init("NumericStepSizeAndDistributeUsers", "101");
var sectorId = "testSectorId";
describe("Testing Build Large/Small Step Size to a Numeric Step Size and Distribute Users to Targets Tas", function() {
    numericStepSizeAndDistributeUsersTest.forEach(function (test) {
        it(test.description, function() {
            var result = numericStepSizeAndDistributeUsers.sourceCellsWithNumericStepSizeAndDistributeUsers(sectorId, test.maxUserToMoveFloat, test.data);
            assert.deepStrictEqual(test.result, result);
        });
    });
});