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

Log.init("addLbqTest", "101");
var sectorId = "testSectorId";
describe("Testing the population of source and target cells into the proposedLbq", function() {
    addLbqTestCases.forEach(function (test) {
        it(test.description, function() {
            var proposedLbqResult = addLbq.populateLbq(sectorId, test.proceedWithOptimization, test.data);
            assert.deepEqual(test.result, JSON.stringify(proposedLbqResult));
        });
    });
});