model create name=Ericsson_FLM_LBDAR_Model version=0.0.1 uuid=63d3eba7-c917-4ac0-b8b7-932f7d3e209e description="Ericsson FLM LBDAR Model"

##
## SCHEMAS
##
schema create name=FLM_ProceedWithOptimization version=0.0.1 uuid=7814724d-82b3-41eb-844c-ae0cde9b7f51 description="FLM_ProceedWithOptimization flag" flavour=Java schema=java.lang.Boolean

schema create name=FLM_sectorId version=0.0.1 uuid=2ed9640b-12ea-4796-9ab9-4bda32987b1a description="Sector ID" flavour=Java schema=java.lang.Long

schema create name=FLM_maxUserToMove version=0.0.1 uuid=94cf484a-e9fc-4467-a2e3-a533e489ad7a description="Max Source Users Move" flavour=Java schema=java.lang.String

schema create name=FLM_executionId version=0.0.1 uuid=2ed7840b-12ea-5096-9aa7-4bda32987b1a description="Execution ID" flavour=Java schema=java.lang.String

schema create name=FLM_optimizationCells version=0.0.1 uuid=2ed8540b-19ea-4796-9ab1-4bda48796a1a description="A schema defining an array of cells to be optimized." flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/OptimizationCellsSchema.json"
LE

schema create name=FLM_sourceTargetCells version=0.0.1 uuid=8ef9698f-5c5a-4f17-96a2-c7d74307ad87 description="A schema defining an array of source cells and target cells to be optimized." flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/SourceTargetCellSchema.json"
LE

schema create name=FLM_topRankedSourceTargetCells version=0.0.1 uuid=6b92b814-8d56-11eb-8dcd-0242ac130003 description="A schema defining record of top ranked source cell and corresponding target cells to be optimized." flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/TopRankedSourceCell.json"
LE

schema create name=FLM_proposedLoadBalancingQuanta version=0.0.1 uuid=2ed6947b-12ea-1158-9ab1-4bda59558a2a description="A candidate source cell and its associated target cells" flavour=Avro schema=LS
#MACROFILE:"eric-frequency-layer-manager-policy/src/main/resources/eric-frequency-layer-manager/policy/schemas/ProposedLoadBalancingQuantaSchema.json"
LE

##
## EVENTS
##

event create name=FlmPolicyInputEvent version=0.0.1 uuid=852f0b04-4697-8258-bf03-53b130203ac5 description="Generic event used for input and output of all states in this policy." nameSpace=com.ericsson.oss.services.sonom.events source="source" target="target"
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=topRankedSourceTargetCells schemaName=FLM_topRankedSourceTargetCells schemaVersion=0.0.1 optional=true
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
event parameter create name=FlmPolicyInputEvent version=0.0.1 parName=proposedLoadBalancingQuanta schemaName=FLM_proposedLoadBalancingQuanta schemaVersion=0.0.1 optional=true

event create name=FlmPolicyOutputEvent version=0.0.1 uuid=a282acc9-11a4-4bcb-a151-aa5722326c9e description="Event used for the final output of all states in this policy." nameSpace=com.ericsson.oss.services.sonom.events source="source" target="target"
event parameter create name=FlmPolicyOutputEvent version=0.0.1 parName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
event parameter create name=FlmPolicyOutputEvent version=0.0.1 parName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
event parameter create name=FlmPolicyOutputEvent version=0.0.1 parName=proposedLoadBalancingQuanta schemaName=FLM_proposedLoadBalancingQuanta schemaVersion=0.0.1 optional=true
event parameter create name=FlmPolicyOutputEvent version=0.0.1 parName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1

##
## TASKS
##

task create name=FLM_InputEventScreenerTask version=0.0.1 uuid=c8f5ee04-af7a-4560-91d0-261a81086fdd description="Input Event Screener Task"
task inputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_InputEventScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_InputEventScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/InputEventIntegrityScreener.js"
LE

task create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 uuid=c8f5ee04-af7a-4560-91d0-261a81086fde description="Input Event Screener Task"
task inputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_AppCoverageReliabilityScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/AppCoverageReliabilityScreener.js"
LE


task create name=FLM_OptimizationScreenerTask version=0.0.1 uuid=c8f5ee04-af7a-4560-91d0-261a81086fdc description="Optimization Screener Task"
task inputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_OptimizationScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_OptimizationScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/GFDeltaScnSctr.js"
LE

task create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b48f90b332a3 description="Identify Source Cells Task"
task inputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task logic create name=FLM_IdentifySourceAndTargetCellsTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/SourceAndTargetCellIdentifier.js"
LE

task create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b48f90b443c7  description="Low Connected Users screener Task"
task inputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_LowConnectedUsersScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/LowConnectedUsersScreener.js"
LE

task create name=FLM_TransienceScreenerTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b48f90b443c6 description="Transience screener Task"
task inputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_TransienceScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_TransienceScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/TransienceScreener.js"
LE

task create name=FLM_ContiguityScreenerTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b48f90b332b5 description="Contiguity screener Task"
task inputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_ContiguityScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_ContiguityScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/ContiguityRuleScnCell.js"
LE

task create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b48f90b332b6 description="Performance Availability Screener Task"
task inputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_PerformanceAvailabilityScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/PerformanceAvailabilityRuleScnCell.js"
LE

task create name=FLM_CoverageBalanceCellsTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-b38f90b442a3 description="Coverage Balance Screener Task"
task inputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_CoverageBalanceCellsTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_CoverageBalanceCellsTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/CoverageBalanceRule.js"
LE

task create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 uuid=0a00cb89-f91d-4d57-8336-a41f90a932e7 description="Percentage ENDC Users screener Task"
task inputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_PercentageENDCUsersScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/ENDCUserRuleScnCell.js"
LE

task create name=FLM_ESSScreenerTask version=0.0.1 uuid=762823d8-ca0e-47a4-aba6-1558ac1eb018 description="ESS screener Task"
task inputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_ESSScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_ESSScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/ESSRuleScnCell.js"
LE

task create name=FLM_UlPuschSinrScreenerTask version=0.0.1 uuid=8910b8de-02b5-11ed-b939-0242ac120002 description="UL Pusch Sinr Screener Task"
task inputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_UlPuschSinrScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_UlPuschSinrScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/UlPuschSinrRuleScnCell.js"
LE

task create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 uuid=3e6489c0-04fc-11ed-b939-0242ac120002 description="Bad RSRP Percentage Screener Task"
task inputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_BadRsrpPercentageScreenerTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/BadRsrpPercentageRuleScnCell.js"
LE

task create name=FLM_MaxSourceUsersMoveTask version=0.0.1 uuid=a4daa5df-c5d8-4ce8-8c7c-a592b9024d7f description="Max Source Users Move calculation Task"
task inputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_MaxSourceUsersMoveTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task logic create name=FLM_MaxSourceUsersMoveTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/MaxSourceUsersMove.js"
LE

task create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 uuid=d3cd54ca-a60b-45c6-91a1-8b2b97293d30 description="Determine Step Size And Optimization Task"
task inputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=sourceCells schemaName=FLM_sourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 fieldName=topRankedSourceTargetCells schemaName=FLM_topRankedSourceTargetCells schemaVersion=0.0.1 optional=true
task logic create name=FLM_DetermineStepSizeAndOptimizationSpeedTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/DetermineStepSizeAndOptimizationSpeed.js"
LE

task create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 uuid=01c5741a-80d3-11eb-9439-0242ac130002 description="Build Large/Small Step Size to a Numeric Step Size and Distribute Users to Targets Task"
task inputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=topRankedSourceTargetCells schemaName=FLM_topRankedSourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=topRankedSourceTargetCells schemaName=FLM_topRankedSourceTargetCells schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task logic create name=FLM_NumericStepSizeAndDistributeUsersTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/NumericStepSizeAndDistributeUsers.js"
LE

task create name=FLM_AddLbqTask version=0.0.1 uuid=ebefc9b5-2f0a-4ee0-b793-5189b0f843a1 description="Generates the proposed LBQ"
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=ProceedWithOptimization schemaName=FLM_ProceedWithOptimization schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=topRankedSourceTargetCells schemaName=FLM_topRankedSourceTargetCells schemaVersion=0.0.1 optional=true
task inputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=maxUserToMove schemaName=FLM_maxUserToMove schemaVersion=0.0.1 optional=true
task outputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=optimizationCells schemaName=FLM_optimizationCells schemaVersion=0.0.1
task outputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=sectorId schemaName=FLM_sectorId schemaVersion=0.0.1
task outputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=executionId schemaName=FLM_executionId schemaVersion=0.0.1
task outputfield create name=FLM_AddLbqTask version=0.0.1 fieldName=proposedLoadBalancingQuanta schemaName=FLM_proposedLoadBalancingQuanta schemaVersion=0.0.1 optional=true
task logic create name=FLM_AddLbqTask version=0.0.1 logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/AddLbq.js"
LE

##
## POLICIES
##

policy create name=FLM_Policy version=0.0.1 uuid=bc7c7e82-1d2f-49cc-a0e7-dc9514bcda5c description="FLM policy" template=FREEFORM firstState=FLM_InputEventScreenerState

## FLM_AddLbqState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_AddLbqState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_AddLbqTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_AddLbqState outputName=FlmPolicyOutputEvent eventName=FlmPolicyOutputEvent eventVersion=0.0.1 nextState=NULL
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_AddLbqState taskLocalName=FLM_AddLbqTask taskName=FLM_AddLbqTask taskVersion=0.0.1 outputType=DIRECT outputName=FlmPolicyOutputEvent

## FLM_NumericStepSizeAndDistributeUsersState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_NumericStepSizeAndDistributeUsersState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_NumericStepSizeAndDistributeUsersTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_NumericStepSizeAndDistributeUsersState outputName=ProceedNumericStepSizeAndDistributeUsers eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_NumericStepSizeAndDistributeUsersState taskLocalName=FLM_NumericStepSizeAndDistributeUsers taskName=FLM_NumericStepSizeAndDistributeUsersTask taskVersion=0.0.1 outputType=DIRECT outputName=ProceedNumericStepSizeAndDistributeUsers

## FLM_DetermineStepSizeAndOptimizationSpeedState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_DetermineStepSizeAndOptimizationSpeedState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_DetermineStepSizeAndOptimizationSpeedTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_DetermineStepSizeAndOptimizationSpeedState outputName=ProceedOrSkipDeterminingStepSizeAndOptimizationSpeed eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_NumericStepSizeAndDistributeUsersState
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_DetermineStepSizeAndOptimizationSpeedState taskLocalName=FLM_DetermineStepSizeAndOptimizationSpeed taskName=FLM_DetermineStepSizeAndOptimizationSpeedTask taskVersion=0.0.1 outputType=DIRECT outputName=ProceedOrSkipDeterminingStepSizeAndOptimizationSpeed

## FLM_MaxSourceUsersMoveState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_MaxSourceUsersMoveState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_MaxSourceUsersMoveTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_MaxSourceUsersMoveState outputName=ProceedToNextState eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_DetermineStepSizeAndOptimizationSpeedState
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_MaxSourceUsersMoveState taskLocalName=FLM_MaxSourceUsersMove taskName=FLM_MaxSourceUsersMoveTask taskVersion=0.0.1 outputType=DIRECT outputName=ProceedToNextState

## FLM_ESSScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_ESSScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_ESSScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_ESSScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_MaxSourceUsersMoveState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_ESSScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_ESSScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_ESSScreenerState taskLocalName=FLM_ESSScreener taskName=FLM_ESSScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_PercentageENDCUsersScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_PercentageENDCUsersScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_PercentageENDCUsersScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_PercentageENDCUsersScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_ESSScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_PercentageENDCUsersScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_PercentageENDCUsersScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_PercentageENDCUsersScreenerState taskLocalName=FLM_PercentageENDCUsersScreener taskName=FLM_PercentageENDCUsersScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_CoverageBalanceScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_CoverageBalanceScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_CoverageBalanceCellsTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_CoverageBalanceScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_PercentageENDCUsersScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_CoverageBalanceScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_CoverageBalanceScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_CoverageBalanceScreenerState taskLocalName=FLM_CoverageBalanceScreener taskName=FLM_CoverageBalanceCellsTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_PerformanceAvailabilityScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_PerformanceAvailabilityScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_PerformanceAvailabilityScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_PerformanceAvailabilityScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_CoverageBalanceScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_PerformanceAvailabilityScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_PerformanceAvailabilityScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_PerformanceAvailabilityScreenerState taskLocalName=FLM_PerformanceAvailabilityScreener taskName=FLM_PerformanceAvailabilityScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_UlPuschSinrScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_UlPuschSinrScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_UlPuschSinrScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_UlPuschSinrScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_PerformanceAvailabilityScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_UlPuschSinrScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_UlPuschSinrScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_UlPuschSinrScreenerState taskLocalName=FLM_UlPuschSinrScreener taskName=FLM_UlPuschSinrScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_BadRsrpPercentageScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_BadRsrpPercentageScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_BadRsrpPercentageScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_BadRsrpPercentageScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_UlPuschSinrScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_BadRsrpPercentageScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_BadRsrpPercentageScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_BadRsrpPercentageScreenerState taskLocalName=FLM_BadRsrpPercentageScreener taskName=FLM_BadRsrpPercentageScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_ContiguityScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_ContiguityScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_ContiguityScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_ContiguityScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_BadRsrpPercentageScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_ContiguityScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_ContiguityScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_ContiguityScreenerState taskLocalName=FLM_ContiguityScreener taskName=FLM_ContiguityScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_TransienceScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_TransienceScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_TransienceScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_TransienceScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_ContiguityScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_TransienceScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_TransienceScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_TransienceScreenerState taskLocalName=FLM_TransienceScreener taskName=FLM_TransienceScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_LowConnectedUsersScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_LowConnectedUsersScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_LowConnectedUsersScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_LowConnectedUsersScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_TransienceScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_LowConnectedUsersScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_LowConnectedUsersScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_LowConnectedUsersScreenerState taskLocalName=FLM_LowConnectedUsersScreener taskName=FLM_LowConnectedUsersScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_IdentifySourceCellsState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_IdentifySourceAndTargetCellsState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_IdentifySourceAndTargetCellsTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_IdentifySourceAndTargetCellsState outputName=ProceedOrSkipSourceAndTargetIdentification eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_LowConnectedUsersScreenerState
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_IdentifySourceAndTargetCellsState taskLocalName=FLM_IdentifySourceAndTargetCells taskName=FLM_IdentifySourceAndTargetCellsTask taskVersion=0.0.1 outputType=DIRECT outputName=ProceedOrSkipSourceAndTargetIdentification

## FLM_OptimizationScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_OptimizationScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_OptimizationScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_OptimizationScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_IdentifySourceAndTargetCellsState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_OptimizationScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_OptimizationScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_OptimizationScreenerState taskLocalName=FLM_GoalFunScrSecr taskName=FLM_OptimizationScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_AppCoverageReliabilityScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_AppCoverageReliabilityScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_AppCoverageReliabilityScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_AppCoverageReliabilityScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_OptimizationScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_AppCoverageReliabilityScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_AppCoverageReliabilityScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_AppCoverageReliabilityScreenerState taskLocalName=FLM_AppCoverageReliability taskName=FLM_AppCoverageReliabilityScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization

## FLM_InputEventScreenerState
policy state create name=FLM_Policy version=0.0.1 stateName=FLM_InputEventScreenerState triggerName=FlmPolicyInputEvent triggerVersion=0.0.1 defaultTaskName=FLM_InputEventScreenerTask defaultTaskVersion=0.0.1
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_InputEventScreenerState outputName=ACTION_PROCEED_WITH_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AppCoverageReliabilityScreenerState
policy state output create name=FLM_Policy version=0.0.1 stateName=FLM_InputEventScreenerState outputName=ACTION_SKIP_OPTIMIZATION eventName=FlmPolicyInputEvent eventVersion=0.0.1 nextState=FLM_AddLbqState
policy state finalizerlogic create name=FLM_Policy version=0.0.1 stateName=FLM_InputEventScreenerState finalizerLogicName=ProceedWithOrSkipOptimization logicFlavour=JAVASCRIPT logic=LS
#MACROFILE:"eric-frequency-layer-manager-policy/target/js/main/StateSelectProceedWithOptimizationOrSkipOptimization.js"
LE
policy state taskref create name=FLM_Policy version=0.0.1 stateName=FLM_InputEventScreenerState taskLocalName=FLM_InputEventScreener taskName=FLM_InputEventScreenerTask taskVersion=0.0.1 outputType=LOGIC outputName=ProceedWithOrSkipOptimization
