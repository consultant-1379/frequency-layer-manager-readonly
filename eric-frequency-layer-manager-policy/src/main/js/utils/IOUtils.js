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
* Utilities class to allow policy task logic to process input and output events that are passed between states in the policy
*/
/* istanbul ignore next */
var IOUtils = (function () {

    var getSubjectId = function () {
        try {
            return executor.subject.id;
        }
        catch (err) {
            Log.w(err);
            Log.w("Failed to fetch subject id returning null");
        }
        return null;
    };

    var getInfields = function () {
        try {
            return executor.inFields;
        }
        catch (err) {
            Log.w(err);
            Log.w("Failed to get inFields returning null");
        }
        return null;
    };

    var getInput = function (key) {
        try {
            if(typeof executor.inFields != "undefined")
                return executor.inFields.get(key);

            return executor.fields.get(key);
        }
        catch (err) {
            Log.w(err);
            Log.w("Failed to fetch " + key + " returning null");
        }
        return null;
    };

    var setOutput = function (key, value) {
        try {
            Log.d("Output set - " + key + " as " + value);
            executor.outFields.put(key, value);
        }
        catch (err) {
            Log.w(err);
            Log.w("Failed to Set output key " + key + " as " + value);
        }
    };

    var getOutput = function (key) {
        try {
            return executor.outFields.get(key);
        }
        catch (err) {
            Log.w(err);
            Log.w("Failed to fetch " + key + " returning null");
        }
        return null;
    };

    var setNextState = function (stateName){
        Log.d("Attempting to set next state to : " + stateName);
        try{
            if(typeof strType != "undefined"){
                executor.selectedStateOutputName = new strType(stateName);
            }
            else{
                executor.selectedStateOutputName = stateName;
            }
            Log.d("Next state set to : " + stateName);
        }
        catch(err){
            Log.w(err);
            Log.w("Failed to set next state to " + stateName);
        }
    };

    var getSectorId = function () {
        return "" + getInput("sectorId").toString();
    };

    return {
        getSubjectId: getSubjectId,
        getInfields: getInfields,
        getInput: getInput,
        setOutput: setOutput,
        getOutput: getOutput,
        setNextState: setNextState,
        getSectorId: getSectorId
    };

})();
