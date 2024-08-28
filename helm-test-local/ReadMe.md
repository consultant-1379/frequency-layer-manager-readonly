# Helm Test Readme

##Notes for further updates

Script is currently support to test on an CCD environment, it may be failed on local laptop because of the resource problems.

    INSTALL_COMMAND_INT="install --devel "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}" --set imageCredentials.pullSecret=eric-son-frequency-layer-manager-int-secret,eric-son-frequency-layer-manager.tags.env-dev=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.zookeeper.enabled=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.kafka.enabled=true --name ${SERVICE_INTEGRATION_NAME} --namespace ${NAMESPACE} --wait --timeout 900"


values.min.yaml file will be modified to reduce the resources.
Helm command on the installFlmIntegrationService method on run_helm_test.sh file will be modified as below;

     INSTALL_COMMAND_INT="install --devel "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}" -f values.min.yaml --set imageCredentials.pullSecret=eric-son-frequency-layer-manager-int-secret,eric-son-frequency-layer-manager.tags.env-dev=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.zookeeper.enabled=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.kafka.enabled=true --name ${SERVICE_INTEGRATION_NAME} --namespace ${NAMESPACE} --wait --timeout 900"


## Run helm integration tests (on CCD environment)

1. The following JAVA_HOME variable should be set according to your Java 11 version's path in run_helm_test.sh at line 3

2. Run the following command under helm-test-local folder.
    ./run_helm_test.sh


## Results
1. Output of the eric-son-frequency-layer-manager-integration pod will be logged under helmTestResults.log file.
2. After run check values.yaml files under /charts/eric-son-frequency-layer-manager and /charts/eric-son-frequency-layer-manager-integration folders.
   If they are checked out revert these changes.
3. After run check the test chart under /charts/eric-son-frequency-layer-manager-integration/charts.
   If it is still there remove this chart from this folder.
4. Manually delete the images from following proj-ec-son-dev artifactory
     - armdocker.rnd.ericsson.se/proj-ec-son-dev/eric-son-frequency-layer-manager
     - armdocker.rnd.ericsson.se/proj-ec-son-dev/eric-son-frequency-layer-manager-integration
5. After tests deployment is not deleted automatically so it should be removed from the server.