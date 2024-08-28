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

/*
 * Task logic for screening out cells with missing mandatory kpis, settings or cm attributes.
 */
var EmptyOrMissingScreener = (function () {

    var filterCells = function (sectorId, optimizationCellsArray) {

        var validOptimizationCellsArray = [];
        for (var index in optimizationCellsArray) {
            var optimizationCell = optimizationCellsArray[index];
            if (validateRequiredFields(sectorId, OptimizationField.REQUIRED_KPIS, optimizationCell.kpis, FieldName.KPIS_FIELD, optimizationCell) &&
                    validateRequiredFields(sectorId, OptimizationField.REQUIRED_SETTINGS, optimizationCell.settings, FieldName.SETTINGS_FIELD,
                    optimizationCell) &&
                    validateRequiredFields(sectorId, OptimizationField.REQUIRED_CM_ATTRIBUTES, optimizationCell.cmAttributes, FieldName
                    .CM_ATTRIBUTES_FIELD,
                    optimizationCell)) {
                validOptimizationCellsArray.push(optimizationCell);
            }
        }
        return validOptimizationCellsArray;
    };

    var validateRequiredFields = function (sectorId, listRequiredProperty, settings, fieldType, optimizationCell ) {
        for (var index in listRequiredProperty) {
            var propertyName = listRequiredProperty[index];
            var value = settings[ propertyName ];
            if (value == null) {
                if (fieldType == FieldName.KPIS_FIELD) {
                    Log.cellExclusion(sectorId, optimizationCell.ossId, optimizationCell.fdn ,"Cell excluded due to missing " + fieldType + " '" + propertyName + "'.");
                } else {
                    Log.cellUnexpectedExceptionExclusion(sectorId, optimizationCell.ossId, optimizationCell.fdn ,"Cell excluded due to missing " + fieldType + " '" + propertyName + "'.");
                }
                return false;
            } else if (value == "" || value == "null") {
                if (fieldType == FieldName.KPIS_FIELD) {
                    Log.cellExclusion(sectorId, optimizationCell.ossId, optimizationCell.fdn ,"Cell excluded due to empty " + fieldType + " '" + propertyName + "' value.");
                } else {
                    Log.cellUnexpectedExceptionExclusion(sectorId, optimizationCell.ossId, optimizationCell.fdn ,"Cell excluded due to empty " + fieldType + " '" + propertyName + "' value.");
                }
                return false;
            }
        }
        return true;
    };

    return {
         filterCells: filterCells
    };
 })();