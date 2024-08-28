model create name=Ericsson_FLM_Performance_Assurance_Model version=0.0.1 uuid=b81088ac-12f4-4261-a78a-e4e7ad7ac20a description="Ericsson FLM Performance Assurance Model"

##
## SCHEMAS
##
schema create name=FLM_PA_executionId version=0.0.1 uuid=44d694d1-d93e-490a-8f92-ec3349191f71 description="Generic Execution ID schema used for both FLM execution Id and PA Execution Id" flavour=Java schema=java.lang.String

schema create name=FLM_PA_window version=0.0.1 uuid=98c5d881-e69a-515b-f420-559ee9487f65 description="PA window schema" flavour=Java schema=java.lang.Integer

schema create name=FLM_PA_sector version=0.0.1 uuid=9de5989a-41de-9687-8dd7-4aba91578b2b description="A schema defining an sector to to check for performance degradation." flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/performanceAssurance/PaSectorSchema.json"
LE

schema create name=FLM_PA_degradationStatus version=0.0.1 uuid=4cf9845e-32ee-2268-1ce8-9ece99558c3c description="A schema defining the result of the policies decision on degradation" flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/performanceAssurance/PaDegradationStatusSchema.json"
LE

##
## EVENTS
##

event create name=FlmPaPolicyInputEvent version=0.0.1 uuid=0bd339f0-5460-4df2-8b80-3dc74313a59d description="Generic event used for input and output of all states in this policy." nameSpace=com.ericsson.oss.services.sonom.events source="source" target="target"
event parameter create name=FlmPaPolicyInputEvent version=0.0.1 parName=flmExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
event parameter create name=FlmPaPolicyInputEvent version=0.0.1 parName=paExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
event parameter create name=FlmPaPolicyInputEvent version=0.0.1 parName=paWindow schemaName=FLM_PA_window schemaVersion=0.0.1
event parameter create name=FlmPaPolicyInputEvent version=0.0.1 parName=sector schemaName=FLM_PA_sector schemaVersion=0.0.1

event create name=FlmPaPolicyOutputEvent version=0.0.1 uuid=43a21da2-3f8c-4a3b-8694-668a1b6d1226 description="Event used for the final output of all states in this policy." nameSpace=com.ericsson.oss.services.sonom.events source="source" target="target"
event parameter create name=FlmPaPolicyOutputEvent version=0.0.1 parName=paExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
event parameter create name=FlmPaPolicyOutputEvent version=0.0.1 parName=flmExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
event parameter create name=FlmPaPolicyOutputEvent version=0.0.1 parName=paWindow schemaName=FLM_PA_window schemaVersion=0.0.1
event parameter create name=FlmPaPolicyOutputEvent version=0.0.1 parName=sector schemaName=FLM_PA_sector schemaVersion=0.0.1
event parameter create name=FlmPaPolicyOutputEvent version=0.0.1 parName=degradationStatus schemaName=FLM_PA_degradationStatus schemaVersion=0.0.1

##
## TASKS
##

task create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 uuid=108e40cc-38c7-468d-ab33-6dda5877dcf6 description="Task to determine if a sector has degraded"
task inputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=flmExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
task inputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=paExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
task inputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=paWindow schemaName=FLM_PA_window schemaVersion=0.0.1
task inputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=sector schemaName=FLM_PA_sector schemaVersion=0.0.1
task outputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=flmExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
task outputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=paExecutionId schemaName=FLM_PA_executionId schemaVersion=0.0.1
task outputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=paWindow schemaName=FLM_PA_window schemaVersion=0.0.1
task outputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=sector schemaName=FLM_PA_sector schemaVersion=0.0.1
task outputfield create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 fieldName=degradationStatus schemaName=FLM_PA_degradationStatus schemaVersion=0.0.1
task logic create name=FLM_PA_DetermineDegradationStatusTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/DetermineDegradationStatus.js"
LE

##
## POLICIES
##

policy create name=FLM_PA_Policy version=0.0.1 uuid=340a0658-f180-461d-996b-d2952579440d description="Performance Assurance policy" template=FREEFORM firstState=FLM_PA_DetermineDegradationStatusState

## FLM_PA_DetermineDegradationStatusState
policy state create name=FLM_PA_Policy version=0.0.1 stateName=FLM_PA_DetermineDegradationStatusState triggerName=FlmPaPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_PA_DetermineDegradationStatusTask defaultTaskVersion=0.0.1
policy state output create name=FLM_PA_Policy version=0.0.1 stateName=FLM_PA_DetermineDegradationStatusState outputName=FlmPaPolicyOutputEvent eventName=FlmPaPolicyOutputEvent eventVersion=0.0.1 nextState=NULL
policy state taskref create name=FLM_PA_Policy version=0.0.1 stateName=FLM_PA_DetermineDegradationStatusState taskLocalName=FLM_PA_DetermineDegradationStatusTask taskName=FLM_PA_DetermineDegradationStatusTask taskVersion=0.0.1 outputType=DIRECT outputName=FlmPaPolicyOutputEvent