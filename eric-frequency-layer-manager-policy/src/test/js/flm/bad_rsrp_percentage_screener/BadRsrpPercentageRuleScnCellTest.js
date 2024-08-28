/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

 Log.init("BadRsrpPercentageRuleScreener", "101" );
 var sectorId = "testSectorId";
 describe("Testing screening of target cells based on Target/Source Bad Rsrp rule", function() {
     possibleSourceCellsAndTargetCellsTest.forEach(function (test) {
         it(test.description,  function() {
             var sourceCellAndScreenedTargetCells = BadRsrpPercentageRuleScnCell.sourceCellAndScreenedTargetCells(test.data, sectorId);
             assert.deepStrictEqual(test.result, sourceCellAndScreenedTargetCells);
         });
     });
 });