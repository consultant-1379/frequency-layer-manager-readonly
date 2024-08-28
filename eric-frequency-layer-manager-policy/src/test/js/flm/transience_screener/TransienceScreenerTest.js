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
Log.init("TransienceScreener", "101" );
var sectorId = "TestSectorId";
describe("Testing Transience Screening on cells which will be optimized", function() {
    transienceScreeningTestCells.forEach(function (test) {
        it(test.description,  function() {
            var optimizationDecision = TransienceScreening.checkCellsForTransience(test.data);
            assert.equal(test.size, optimizationDecision.length);
            assert.deepEqual(test.result, optimizationDecision);
        });
    });
});