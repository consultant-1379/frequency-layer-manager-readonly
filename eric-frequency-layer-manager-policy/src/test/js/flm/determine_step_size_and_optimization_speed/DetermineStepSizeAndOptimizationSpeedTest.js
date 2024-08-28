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

Log.init("DetermineStepSizeAndOptimizationSpeed", "101");
var sectorId = "testSectorId";
describe("Testing determining of the step size and optimization speed", function() {
    determineStepSizeAndOptimizationSpeedTest.forEach(function (test) {
        it(test.description, function() {
            var result = determineStepSizeAndOptimizationSpeed.sourceCellsWithDeterminedStepSizeAndOptimizationSpeed(sectorId, test.optimizationCells, test.data);
            assert.deepStrictEqual(test.result, result);
        });
    });
});
