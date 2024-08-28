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
Log.init("DetermineDegradationStatus", "101" );
var sectorId = "173290459927812150";
var paExecutionId = "FLM_123456789_1";
var paWindow = 1;
describe("Testing degradation evaluation carried out by the performance assurance policy", function() {
   possibleInputEventsToPaPolicyThresholdComparisonTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicyNullHandlingTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicySettingsTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicyRelevanceThresholdTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicyRelevanceThresholdTypeTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicyKpiEnabledTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
    possibleInputEventsToPaPolicyRangeTest.forEach(function (test) {
        it(test.description,  function() {
            var degradationStatus = DetermineDegradationStatus.determineIfSectorDegraded(test.data, sectorId, paExecutionId, paWindow);
            assert.deepStrictEqual(degradationStatus, test.result);
        });
    });
});