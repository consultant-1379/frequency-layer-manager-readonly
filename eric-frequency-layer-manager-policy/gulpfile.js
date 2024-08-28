/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

const NODE_ENV = "" + (process.env.NODE_ENV);
const devBuild = (NODE_ENV.trim().toLowerCase() === 'development');

var gulp = require('gulp');
var concat = require('gulp-concat');
var jshint = require('gulp-jshint');
var reporterStylish = require('jshint-stylish');
var uglify = require('gulp-uglify');


var JSBuilder = (function () {

    var _TARGET_MAINS_FOLDER = "target/js/main";
    var _TARGET_TESTS_FOLDER = "target/js/tests";

    var _UTILS = [
        'src/main/js/utils/Log.js',
        'src/main/js/utils/IOUtils.js'
    ];
    var _APEX = [
        'src/main/js/apex/ApexRequirement.js'
    ];
    var _TEST_SETUPS = [
        'src/test/js/setups/TestSetup.js'
    ];


    var _buildJS = function (sourceFileArray, targetFileName, targetFolder) {
        if(devBuild) { // don't uglify in dev mode for code coverage
            gulp.src(sourceFileArray)
              .pipe(jshint())
              .pipe(jshint.reporter(reporterStylish))
              .pipe(jshint.reporter('fail'))
              .pipe(concat(targetFileName))
              .pipe((gulp.dest(targetFolder)));
        } else {
            gulp.src(sourceFileArray)
              .pipe(jshint())
              .pipe(jshint.reporter(reporterStylish))
              .pipe(jshint.reporter('fail'))
              .pipe(concat(targetFileName))
              .pipe(uglify())
              .pipe((gulp.dest(targetFolder)));
        }
    };

    var buildSource = function (sourceFileArray, targetFileName) {
        _buildJS(
            _UTILS.concat(sourceFileArray).concat(_APEX),
            targetFileName,
            _TARGET_MAINS_FOLDER
        );
    };

    var buildTests = function (testsFileArray, targetFileName) {
        _buildJS(
            _TEST_SETUPS.concat(_UTILS).concat(testsFileArray),
            targetFileName,
            _TARGET_TESTS_FOLDER
        );
    };

    return {
        buildSource: buildSource,
        buildTests: buildTests
    };
})();

gulp.task('build', function (done) {

    ///////////////////
    /// FLM POLICY ///
    ///////////////////


    //Build InputEventScreener state
     var InputEventIntegrityScreenerSources = [
        'src/main/js/flm/input_event_screener/OptimizationField.js',
        'src/main/js/flm/input_event_screener/FieldName.js',
        'src/main/js/flm/input_event_screener/EmptyOrMissingScreener.js',
        'src/main/js/flm/input_event_screener/ReliabilityScreener.js',
        'src/main/js/flm/input_event_screener/InputEventScreenerMain.js'
     ];

     var InputEventIntegrityScreenerSourcesTests = [
        'src/main/js/flm/input_event_screener/OptimizationField.js',
        'src/main/js/flm/input_event_screener/FieldName.js',
        'src/main/js/flm/input_event_screener/EmptyOrMissingScreener.js',
        'src/main/js/flm/input_event_screener/ReliabilityScreener.js',
        'src/test/js/flm/input_event_screener/EmptyOrMissingScreenerTestsAndResults.js',
        'src/test/js/flm/input_event_screener/reliabilityScreenerTest.js',
        'src/test/js/flm/input_event_screener/InputEventScreenerSpec.js'
     ];

     JSBuilder.buildSource(InputEventIntegrityScreenerSources, 'InputEventIntegrityScreener.js');
     JSBuilder.buildTests(InputEventIntegrityScreenerSourcesTests, 'InputEventIntegrityScreenerTest.js');


    //Build AppCoverageReliabilityScreener state
    var AppCoverageReliabilityScreenerSources = [
        'src/main/js/flm/app_coverage_reliability/AppCoverageReliabilityScreener.js',
        'src/main/js/flm/app_coverage_reliability/AppCoverageReliabilityMain.js'
    ];

    var AppCoverageReliabilityScreenerSourcesTests = [
        'src/main/js/flm/app_coverage_reliability/AppCoverageReliabilityScreener.js',
        'src/test/js/flm/app_coverage_reliability/AppCoverageReliabilityTestsAndResults.js',
        'src/test/js/flm/app_coverage_reliability/AppCoverageReliabilityTest.js'
     ];

    JSBuilder.buildSource(AppCoverageReliabilityScreenerSources, 'AppCoverageReliabilityScreener.js');
    JSBuilder.buildTests(AppCoverageReliabilityScreenerSourcesTests, 'AppCoverageReliabilityScreenerTest.js');

    //Build GFDeltaScnSctr state
    var GfDeltaScnSources = [
                'src/main/js/flm/goalfunctiondelta/GFDeltaScnSctr.js',
                'src/main/js/flm/goalfunctiondelta/GFDeltaScnSctrMain.js'
    ];

    var GFDeltaTest = [
                'src/main/js/flm/goalfunctiondelta/GFDeltaScnSctr.js',
                'src/test/js/flm/TestCaseOptimizationCellsWithExpectedResults.js',
                'src/test/js/flm/GFDeltaScnSctrTest.js'
    ];

    JSBuilder.buildSource(GfDeltaScnSources, 'GFDeltaScnSctr.js');
    JSBuilder.buildTests(GFDeltaTest, 'GFDeltaScnSctrTest.js');

    //Build logic for choosing next state based on whether optimization will occur
    var StateSelectProceedWithOptimizationOrSkipOptimization = [
                'src/main/js/flm/apex_state_selectors/ProceedWithOptimizationOrSkipOptimization.js'
    ];

    JSBuilder.buildSource(StateSelectProceedWithOptimizationOrSkipOptimization, 'StateSelectProceedWithOptimizationOrSkipOptimization.js');

    //Build SourceCellIdentifier state
    var SourceAndTargetCellIdentifierSources = [
        'src/main/js/flm/sourcecellidentifier/SourceAndTargetCellIdentifier.js',
        'src/main/js/flm/sourcecellidentifier/SourceAndTargetCellIdentifierMain.js'
    ];

    var SourceAndTargetCellIdentifierTest = [
        'src/main/js/flm/sourcecellidentifier/SourceAndTargetCellIdentifier.js',
        'src/test/js/flm/sourcecellidentifier/SourceAndTargetCellIdentifierTestCaseWithExpectedResults.js',
        'src/test/js/flm/sourcecellidentifier/SourceAndTargetCellIdentifierTest.js'
    ];

    JSBuilder.buildSource(SourceAndTargetCellIdentifierSources, 'SourceAndTargetCellIdentifier.js');
    JSBuilder.buildTests(SourceAndTargetCellIdentifierTest, 'SourceAndTargetCellIdentifierTest.js');

    //Build LowConnectedUsersScreener state
    var LowConnectedUsersScreener = [
        'src/main/js/flm/low_connected_users_screener/LowConnectedUsersScreener.js',
        'src/main/js/flm/low_connected_users_screener/LowConnectedUsersScreenerMain.js'
    ];

    var LowConnectedUsersScreenerTest = [
        'src/main/js/flm/low_connected_users_screener/LowConnectedUsersScreener.js',
        'src/test/js/flm/low_connected_users_screener/LowConnectedUsersScreenerTestCaseWithExpectedResults.js',
        'src/test/js/flm/low_connected_users_screener/LowConnectedUsersScreenerTest.js'
    ];

    JSBuilder.buildSource(LowConnectedUsersScreener, 'LowConnectedUsersScreener.js');
    JSBuilder.buildTests(LowConnectedUsersScreenerTest, 'LowConnectedUsersScreenerTest.js');

    //Build TransienceScreener state
    var TransienceScreener = [
        'src/main/js/flm/transience_screener/TransienceScreener.js',
        'src/main/js/flm/transience_screener/TransienceScreenerMain.js'
    ];

    var TransienceScreenerTest = [
        'src/main/js/flm/transience_screener/TransienceScreener.js',
        'src/test/js/flm/transience_screener/TransienceScreenerTestCaseWithExpectedResults.js',
        'src/test/js/flm/transience_screener/TransienceScreenerTest.js'
    ];

    JSBuilder.buildSource(TransienceScreener, 'TransienceScreener.js');
    JSBuilder.buildTests(TransienceScreenerTest, 'TransienceScreenerTest.js');

    //Build contiguity screener state
    var ContiguityRuleTargetCellScreener = [
        'src/main/js/flm/contiguityrule/ContiguityRuleScnCell.js',
        'src/main/js/flm/contiguityrule/ContiguityRuleScnCellMain.js'
    ];

    var ContiguityRuleTargetCellScreenerTest = [
        'src/main/js/flm/contiguityrule/ContiguityRuleScnCell.js',
        'src/test/js/flm/contiguityrule/ContiguityRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/contiguityrule/ContiguityRuleScnCellTest.js'
    ];

    JSBuilder.buildSource(ContiguityRuleTargetCellScreener, 'ContiguityRuleScnCell.js');
    JSBuilder.buildTests(ContiguityRuleTargetCellScreenerTest, 'ContiguityRuleScnCellTest.js');

    //Build performance availability screener state
    var PerformanceAvailabilityRuleScreener = [
        'src/main/js/flm/performance_availability_screener/PerformanceAvailabilityRuleScnCell.js',
        'src/main/js/flm/performance_availability_screener/PerformanceAvailabilityScreenerMain.js'
    ];

     var PerformanceAvailabilityRuleScreenerTest = [
        'src/main/js/flm/performance_availability_screener/PerformanceAvailabilityRuleScnCell.js',
        'src/test/js/flm/performance_availability_screener/pfmnceAvabltyRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/performance_availability_screener/performanceAvailabilityRuleScnCellTest.js'
    ]

    JSBuilder.buildSource(PerformanceAvailabilityRuleScreener, 'PerformanceAvailabilityRuleScnCell.js');
    JSBuilder.buildTests(PerformanceAvailabilityRuleScreenerTest, 'performanceAvailabilityRuleScnCellTest.js');

    //Build percentage ENDC users screener state
    var ENDCUserRuleTargetCellScreener = [
        'src/main/js/flm/endc_users_rule/ENDCUserRuleScnCell.js',
        'src/main/js/flm/endc_users_rule/ENDCUserRuleScnCellMain.js'
    ];

    var ENDCUserRuleTargetCellScreenerTest = [
        'src/main/js/flm/endc_users_rule/ENDCUserRuleScnCell.js',
        'src/test/js/flm/endc_users_rule/ENDCUserRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/endc_users_rule/ENDCUserRuleScnCellTest.js'
    ]

    JSBuilder.buildSource(ENDCUserRuleTargetCellScreener, 'ENDCUserRuleScnCell.js');
    JSBuilder.buildTests(ENDCUserRuleTargetCellScreenerTest, 'ENDCUserRuleScnCellTest.js')

     //Build UL_Pusch_Sinr screener state
    var UlPuschSinrRuleScreener = [
        'src/main/js/flm/ul_pusch_sinr_screener/UlPuschSinrRuleScnCell.js',
        'src/main/js/flm/ul_pusch_sinr_screener/UlPuschSinrRuleScnCellMain.js'
    ];

    var UlPuschSinrRuleScreenerTest = [
        'src/main/js/flm/ul_pusch_sinr_screener/UlPuschSinrRuleScnCell.js',
        'src/test/js/flm/ul_pusch_sinr_screener/UlPuschSinrRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/ul_pusch_sinr_screener/UlPuschSinrScnCellTest.js'
    ];

    JSBuilder.buildSource(UlPuschSinrRuleScreener, 'UlPuschSinrRuleScnCell.js');
    JSBuilder.buildTests(UlPuschSinrRuleScreenerTest, 'UlPuschSinrScnCellTest.js');

     //Build Bad RSRP % screener state
    var BadRsrpPercentageScreener = [
        'src/main/js/flm/bad_rsrp_percentage_screener/BadRsrpPercentageRuleScnCell.js',
        'src/main/js/flm/bad_rsrp_percentage_screener/BadRsrpPercentageRuleScnCellMain.js'
    ];

    var BadRsrpPercentageScreenerTest = [
        'src/main/js/flm/bad_rsrp_percentage_screener/BadRsrpPercentageRuleScnCell.js',
        'src/test/js/flm/bad_rsrp_percentage_screener/BadRsrpPercentageRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/bad_rsrp_percentage_screener/BadRsrpPercentageRuleScnCellTest.js'
    ];

    JSBuilder.buildSource(BadRsrpPercentageScreener, 'BadRsrpPercentageRuleScnCell.js');
    JSBuilder.buildTests(BadRsrpPercentageScreenerTest, 'BadRsrpPercentageRuleScnCellTest.js');

    //Build Coverage Balance screener state
    var CoverageBalanceRuleSources = [
        'src/main/js/flm/coveragebalancerule/CoverageBalanceRule.js',
        'src/main/js/flm/coveragebalancerule/CoverageBalanceRuleMain.js'
    ];

    var CoverageBalanceRuleTest = [
        'src/main/js/flm/coveragebalancerule/CoverageBalanceRule.js',
        'src/test/js/flm/coveragebalancerule/CoverageBalanceRuleTestCaseWithExpectedResult.js',
        'src/test/js/flm/coveragebalancerule/CoverageBalanceRuleTest.js'
    ];

    JSBuilder.buildSource(CoverageBalanceRuleSources, 'CoverageBalanceRule.js');
    JSBuilder.buildTests(CoverageBalanceRuleTest, 'CoverageBalanceRuleTest.js');

    //Build ESS screener state
    var ESSRuleTargetCellScreener = [
        'src/main/js/flm/ess_rule/ESSRuleScnCell.js',
        'src/main/js/flm/ess_rule/ESSRuleScnCellMain.js'
    ];

    var ESSRuleTargetCellScreenerTest = [
        'src/main/js/flm/ess_rule/ESSRuleScnCell.js',
        'src/test/js/flm/ess_rule/ESSRuleScnCellTestCaseWithExpectedResult.js',
        'src/test/js/flm/ess_rule/ESSRuleScnCellTest.js'
    ]

    JSBuilder.buildSource(ESSRuleTargetCellScreener, 'ESSRuleScnCell.js');
    JSBuilder.buildTests(ESSRuleTargetCellScreenerTest, 'ESSRuleScnCellTest.js');

    //Build determine step size and optimization speed state
    var DetermineStepSizeAndOptimizationSpeed = [
        'src/main/js/flm/determine_step_size_and_optimization_speed/DetermineStepSizeAndOptimizationSpeed.js',
        'src/main/js/flm/determine_step_size_and_optimization_speed/DetermineStepSizeAndOptimizationSpeedMain.js'
    ];

    var DetermineStepSizeAndOptimizationSpeedTest = [
        'src/main/js/flm/determine_step_size_and_optimization_speed/DetermineStepSizeAndOptimizationSpeed.js',
        'src/test/js/flm/determine_step_size_and_optimization_speed/DetermineStepSizeAndOptimizationSpeedTestCaseWithExpectedResult.js',
        'src/test/js/flm/determine_step_size_and_optimization_speed/DetermineStepSizeAndOptimizationSpeedTest.js'
    ];

    JSBuilder.buildSource(DetermineStepSizeAndOptimizationSpeed, 'DetermineStepSizeAndOptimizationSpeed.js');
    JSBuilder.buildTests(DetermineStepSizeAndOptimizationSpeedTest, 'DetermineStepSizeAndOptimizationSpeedTest.js');

    //Build Max Source Users Move
    var MaxSourceUsersMoveSources = [
        'src/main/js/flm/maxsourceusermove/MaxSourceUsersMove.js',
        'src/main/js/flm/maxsourceusermove/MaxSourceUsersMoveMain.js'
    ];

    var MaxSourceUsersMoveSourcesTest = [
        'src/main/js/flm/maxsourceusermove/MaxSourceUsersMove.js',
        'src/test/js/flm/maxsourceusermove/maxSourceUserMoveTestCasesWithExpectedResult.js',
        'src/test/js/flm/maxsourceusermove/maxSourceUserMoveTest.js'
    ];

    JSBuilder.buildSource(MaxSourceUsersMoveSources, 'MaxSourceUsersMove.js');
    JSBuilder.buildTests(MaxSourceUsersMoveSourcesTest, 'MaxSourceUsersMoveSourcesTest.js');

    //Build Numeric Step Size And Distribute Users State
    var NumericStepSizeAndDistributeUsers = [
        'src/main/js/flm/numericstepsizeanddistributeusers/NumericStepSizeAndDistributeUsers.js',
        'src/main/js/flm/numericstepsizeanddistributeusers/NumericStepSizeAndDistributeUsersMain.js'
    ];

    var NumericStepSizeAndDistributeUsersTest = [
        'src/main/js/flm/numericstepsizeanddistributeusers/NumericStepSizeAndDistributeUsers.js',
        'src/test/js/flm/numericstepsizeanddistributeusers/NumericStepSizeAndDistributeUsersTestCaseWithExpectedResult.js',
        'src/test/js/flm/numericstepsizeanddistributeusers/NumericStepSizeAndDistributeUsersTest.js'
    ];

    JSBuilder.buildSource(NumericStepSizeAndDistributeUsers, 'NumericStepSizeAndDistributeUsers.js');
    JSBuilder.buildTests(NumericStepSizeAndDistributeUsersTest, 'NumericStepSizeAndDistributeUsersTest.js');

    //Build AddLbq State
    var AddLbqSources = [
        'src/main/js/flm/AddLbq.js',
        'src/main/js/flm/AddLbqMain.js'
    ];

    var AddLbqTest = [
        'src/main/js/flm/AddLbq.js',
        'src/test/js/flm/addLbqTestCasesWithExpectedResults.js',
        'src/test/js/flm/addLbqTest.js'
    ];

    JSBuilder.buildSource(AddLbqSources, 'AddLbq.js');
    JSBuilder.buildTests(AddLbqTest, 'addLbqTest.js');

    ////////////////////////////////////////
    /// FLM PERFORMANCE ASSURANCE POLICY ///
    ////////////////////////////////////////

    //Build Degradation Status state
    var DegradationStatusSources = [
       'src/main/js/pa/degrade/DetermineDegradationStatus.js',
       'src/main/js/pa/degrade/DetermineDegradationStatusMain.js'
    ];

    var DegradationStatusSourcesTest = [
       'src/main/js/pa/degrade/DetermineDegradationStatus.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusThresholdComparisonTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusSettingsTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusRelevanceThresholdTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusRelevanceThresholdTypeTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusKpiEnabledTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusNullHandlingTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusRangeTestCaseWithExpectedResult.js',
       'src/test/js/pa/degrade/DetermineDegradationStatusTest.js'
    ];

    JSBuilder.buildSource(DegradationStatusSources, 'DetermineDegradationStatus.js');
    JSBuilder.buildTests(DegradationStatusSourcesTest, 'DetermineDegradationTest.js');
    done();
});