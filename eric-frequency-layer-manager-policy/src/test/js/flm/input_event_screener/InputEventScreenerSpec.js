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

describe("Testing InputEvent Screener", function () {
    testEmptyMissingValues.forEach(function (test) {
        describe("Testing missing or empty optimization cell values are screened out of the optimization cell array", function () {
            it(test.description, function () {
                assert.deepStrictEqual(EmptyOrMissingScreener.filterCells("1", test.data), test.result);
            });
        });
    });
});


describe("Cell Reliability Test", function () {
    reliabilityScreenerTestData.forEach(function (test) {
        it(test.description, function () {
            assert.equal(ReliabilityScreener.isOptimizationCellsReliable("1", test.data),test.result);
        });
    });
});