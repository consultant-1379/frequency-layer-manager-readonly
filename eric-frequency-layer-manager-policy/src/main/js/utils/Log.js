/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020 - 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

/*
* Utilities class providing logging functions for policy tasks
*/
/* istanbul ignore next */
var Log = (function () {
    var _logEnabled = (typeof executor.logger.logEnabled === 'undefined') ? true : executor.logger.logEnabled;
    var _instance;
    var _executionId;

    var _UNKNOWN_INSTANCE_DEFAULT = "UNKNOWN_INSTANCE";
    var _NO_EXECUTION_ID_DEFAULT = "NO_EXECUTION_ID";
    var _UNEXPECTED_EXCEPTION_OCCURRED = "Unexpected exception occurred - ";

    // init method should be called before any logging functionality has started
    var init = function(instanceName, executionIdToSet) {
          this.setInstanceName(instanceName);
          this.setExecutionId(executionIdToSet);
    };

    var getInstanceName = function () {
        return _instance;
    };

    var setInstanceName = function (instanceName) {
        try{
            // .length will throw an error if instanceName is null or undefined.
            _instance = (instanceName.length > 0) ? instanceName : UNKNOWN_INSTANCE_DEFAULT;
        } catch (err) {
            w("Failed to set Instance Name. Using '" + UNKNOWN_INSTANCE_DEFAULT + "' for logging");
            instanceName = UNKNOWN_INSTANCE_DEFAULT;
        }
    };

    var getExecutionId = function () {
        return _executionId;
    };

    var setExecutionId = function(executionIdToSet){
        try{
            // .length will throw an error if executionIdToSet is null or undefined.
            executionIdToSet=""+executionIdToSet.toString();
            _executionId = (executionIdToSet.length > 0) ? executionIdToSet : _NO_EXECUTION_ID_DEFAULT;
        } catch (err) {
             w("Failed to set Execution ID. Using '" + _NO_EXECUTION_ID_DEFAULT +"' for logging");
             executionIdToSet = _NO_EXECUTION_ID_DEFAULT;
        }
    };

    var i = function (message) {
        if (_logEnabled == false) return;

        try {
            executor.logger.info(_getLoggingMessage(message));
        }
        catch (err) {

        }
    };
    var e = function (message) {
        try {
            executor.logger.error(_getLoggingMessage(message));
        }
        catch (err) {

        }
    };
    var d = function (message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.debug(_getLoggingMessageWithInstance(message));
        }
        catch (err) {

        }
    };
    var t = function (message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.trace(_getLoggingMessage(message));
        }
        catch (err) {

        }
    };
    var w = function (message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.warn(_getLoggingMessage(message));
        }
        catch (err) {

        }
    };

    var cellInfo = function (sectorId, ossId, cellId, message) {
              if (_logEnabled == false) return;
              try {
                  executor.logger.info(infoForCell(sectorId, ossId, cellId, message));
              }
              catch (err) {
              }
          };

    var sectorInfo = function (sectorId, message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.info(infoForSector(sectorId, message));
        }
        catch (err) {
        }
    };

    var sectorWarn = function (sectorId, message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.warn(infoForSector(sectorId, message));
        }
        catch (err) {
        }
    };

    var cellInclusion = function (sectorId, ossId, cellId, message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.info(inclusionInfoForCell(sectorId, ossId, cellId, message));
        }
        catch (err) {
        }
    };

    var cellExclusion = function (sectorId, ossId, cellId, message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.info(exclusionInfoForCell(sectorId, ossId, cellId , message));
        }
        catch (err) {
        }
    };

    var cellUnexpectedExceptionExclusion = function (sectorId, ossId, cellId, message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.info(exclusionInfoForCell(sectorId, ossId, cellId , _UNEXPECTED_EXCEPTION_OCCURRED + message));
        }
        catch (err) {
        }
    };

    var sectorExclusion = function (sectorId , message) {
        if (_logEnabled == false) return;
        try {
            executor.logger.info(exclusionInfoForSector(sectorId , message));
        }
        catch (err) {
        }
    };

    var _getLoggingMessageWithInstance = function (message) {
        return "Execution_ID: " + _executionId + " [" + _instance + "] " + message;
    };

    var _getLoggingMessage = function (message) {
        return "Execution_ID: " + _executionId + " " + message;
    };

    var infoForCell = function(sectorId, ossId, cellId , message){
        return "Execution_ID: " + _executionId + ", Oss_ID: " + ossId + ", Sector_ID: " + sectorId + ", Cell_ID: " + cellId + ", " + message;
    };
    
    var infoForSector = function(sectorId, message){
        return "Execution_ID: " + _executionId + ", Sector_ID: " + sectorId + ", " + message;
    };

    var inclusionInfoForCell = function(sectorId, ossId, cellId , message){
        return "Execution_ID: " + _executionId + ", Oss_ID: " + ossId + ", Sector_ID: " + sectorId + ", Cell_ID: " + cellId + ", Inclusion_Reason: " + message;
    };

    // NOTE: Message used for reporting matching pattern and agreed with CA.
    var exclusionInfoForCell = function(sectorId, ossId, cellId , message){
        return "Execution_ID: " + _executionId + ", Oss_ID: " + ossId + ", Sector_ID: " + sectorId + ", Cell_ID: " + cellId + ", Exclusion_Reason: " + message;
    };

    // NOTE: Message used for reporting matching pattern and agreed with CA.
    var exclusionInfoForSector = function(sectorId, message){
        return "Execution_ID: " + _executionId + ", Sector_ID: " + sectorId + ", Exclusion_Reason: " + message;
    };

    var unmonitoredKpiWarn = function(sectorId, kpiName, paWindow, timestamp, cellId, ossId){
        if (_logEnabled == false) return;
        try {
            executor.logger.warn(infoForNullKpiValue(sectorId, kpiName, paWindow, timestamp, cellId, ossId));
        }
        catch (err) {
        }
        };

    var infoForNullKpiValue = function(sectorId, kpiName, paWindow, timestamp, cellId, ossId){
        if((typeof cellId == 'undefined') && ( typeof ossId == 'undefined') ){
            return "Execution_ID: " + _executionId + ", Sector_ID: " + sectorId + ", KPI: " + kpiName + " is null for PA window " + paWindow + ". No KPI monitoring possible for the timestamp: " + timestamp + " on the sector for this KPI. Please check sector for possible degradation.";
        }
        return "Execution_ID: " + _executionId + ", Sector_ID: " + sectorId + ", Cell_ID: "+ cellId + ", Oss_ID: " + ossId +
         " KPI: " + kpiName + " is null for PA window " + paWindow + ". No KPI monitoring possible for the timestamp: " + timestamp + " on the cell for this KPI. Please check cell for possible degradation.";
     };

    return {
        init: init,
        getInstanceName: getInstanceName,
        setInstanceName: setInstanceName,
        getExecutionId: getExecutionId,
        setExecutionId: setExecutionId,
        sectorExclusion: sectorExclusion,
        cellExclusion: cellExclusion,
        cellUnexpectedExceptionExclusion: cellUnexpectedExceptionExclusion,
        cellInclusion: cellInclusion,
        cellInfo: cellInfo,
        sectorInfo: sectorInfo,
        sectorWarn: sectorWarn,
        unmonitoredKpiWarn: unmonitoredKpiWarn,
        e: e,
        i: i,
        w: w,
        t: t,
        d: d
    };
})();
