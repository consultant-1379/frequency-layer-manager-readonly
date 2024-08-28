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

describe("Testing screening of target cells based on the performance and availability rule", function() {
    possibleSourceCellsAndTargetCellsTest.forEach(function (test) {
        it(test.description,  function() {
           if(test.TestCase === "TestAllConditions"){
             var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(test.data);
             assert.deepStrictEqual(test.result, sourceCellAndScreenedTargetCells);
            return;
           }
           validateForMissingKpi(test.data);
           validateForMissingSettings(test.data);
           validateNullKpi(test.data);
           validateNullSetting(test.data);
           if(test.TestCase === "TestLessThanConditions"){
                validateLessThenConditions(test.data,'[]');
           }
           else{
                validateGreaterThenConditions(test.data,'[]');
           }
        });

        var validateForMissingKpi = function (data) {
            var newData = JSON.parse(JSON.stringify(data));
            for (var sourceTargetList in newData) {
                 var targetList = newData[sourceTargetList].targetCells;
                 for (var targetCell in targetList) {
                    for (var kpi in targetList[targetCell].kpis) {
                    if(kpi !== "cell_availability"){
                        delete targetList[targetCell].kpis[kpi];
                      }
                 }
              }
             }
             var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(newData);
             assert.deepStrictEqual(newData, sourceCellAndScreenedTargetCells);
        };

        var validateForMissingSettings = function (data) {
            var newData = JSON.parse(JSON.stringify(data));
            for (var sourceTargetList in newData) {
             var source = newData[sourceTargetList].sourceCell;
             for (var setting in source.settings) {
                     delete source.settings[setting];
              }
             }
             var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(newData);
             assert.deepStrictEqual(newData, sourceCellAndScreenedTargetCells);
        };

        var validateNullKpi = function (data) {
             var newData = JSON.parse(JSON.stringify(data));
             for (var sourceTargetList in newData) {
                  var targetList = newData[sourceTargetList].targetCells;
                  for (var targetCell in targetList) {
                     for (var kpi in targetList[targetCell].kpis) {
                     if(kpi !== "cell_availability"){
                         targetList[targetCell].kpis[kpi] = null;
                       }
                  }
               }
              }
              var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(newData);
              assert.deepStrictEqual(newData, sourceCellAndScreenedTargetCells);
        };

        var validateNullSetting = function (data) {
            var newData = JSON.parse(JSON.stringify(data));
            for (var sourceTargetList in newData) {
             var source = newData[sourceTargetList].sourceCell;
             for (var setting in source.settings) {
                     source.settings[setting]=null;
              }
             }
             var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(newData);
             assert.deepStrictEqual(newData, sourceCellAndScreenedTargetCells);
        };

        var validateLessThenConditions = function (data, assertData) {
            var newData = JSON.parse(JSON.stringify(data));
            for (var sourceTargetList in newData) {
                var source = newData[sourceTargetList].sourceCell;
                for (var setting in source.settings) {
                         source.settings[setting]=10;
                  }
                var targetList = newData[sourceTargetList].targetCells;
                for (var targetCell in targetList) {
                   for (var kpi in targetList[targetCell].kpis) {
                        if(kpi !== "cell_availability"){
                            targetList[targetCell].kpis[kpi] = 5;
                          }
                 }
                }
            }
            var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(newData);
            assert.deepStrictEqual((assertData === '[]') ? [] : newData, sourceCellAndScreenedTargetCells);
            if(assertData === '[]')
            validateGreaterThenConditions(data,data);
        };

        var validateGreaterThenConditions = function (changedData, assertData) {
            for (var sourceTargetList in changedData) {
                var source = changedData[sourceTargetList].sourceCell;
                for (var setting in source.settings) {
                         source.settings[setting]=5;
                  }
                var targetList = changedData[sourceTargetList].targetCells;
                for (var targetCell in targetList) {
                   for (var kpi in targetList[targetCell].kpis) {
                        if(kpi !== "cell_availability"){
                            targetList[targetCell].kpis[kpi] = 10;
                          }
                 }
                }
            }
            var sourceCellAndScreenedTargetCells = performanceAvailabilityRuleScnCell.sourceCellAndScreenedTargetCells(changedData);
            assert.deepStrictEqual((assertData === '[]') ? [] : changedData, sourceCellAndScreenedTargetCells);
            if(assertData === '[]')
            validateLessThenConditions(changedData,changedData);
        };

    });
});

