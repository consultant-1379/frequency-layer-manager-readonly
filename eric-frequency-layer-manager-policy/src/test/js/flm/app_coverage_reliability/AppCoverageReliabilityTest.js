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
describe("Testing AppCoverageReliability Screener", function () {
    describe("Testing optimization cell that are not reliable for app coverage are screened out of the optimization cell array", function () {
        testAppCoverageReliabilityValues.forEach(function (test) {
            it(test.description, function () {
                assert.deepStrictEqual(AppCoverageReliabilityScreener.filterCells("1", test.data), test.result);
            });
        });
    });
});
