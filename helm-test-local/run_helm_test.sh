#!/bin/bash

HELM_TEST_LOCAL=`pwd`
PROJECT_ROOT="${HELM_TEST_LOCAL}/.."

HELM_VERSION_CMD=$(helm version)
HELM_VERSION=$(echo $HELM_VERSION_CMD |awk -F'Version:"v' '{print $2}' |awk -F'.' '{print $1}')

TAG=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w ${1:-6} | head -n 1)
USER_ID=$( echo "$(whoami)" | tr '[:upper:]' '[:lower:]' | awk -F'@' '{print $1}'| awk -F'+' '{print $2}')
# Above command does not give correct user id on old laptops
if [ -z "$USER_ID" ]; then
	USER_ID=$( echo "$(whoami)" | tr '[:upper:]' '[:lower:]' | awk -F'@' '{print $1}')
fi

SERVICE_NAME="eric-son-frequency-layer-manager"
SERVICE_DEPLOYMENT_NAME="${USER_ID}-${SERVICE_NAME}"
SERVICE_TAG="flm-${TAG}-${USER_ID}"

SERVICE_INTEGRATION_NAME="${SERVICE_NAME}-integration"
SERVICE_INTEGRATION_DEPLOYMENT_NAME="${USER_ID}-${SERVICE_NAME}-integration"
echo ${SERVICE_INTEGRATION_DEPLOYMENT_NAME}

SERVICE_INTEGRATION_TAG="flm-int-${TAG}-${USER_ID}"
SERVICE_INTEGRATION_HOSTNAME="${SERVICE_INTEGRATION_TAG}.hostname.domain.com"

NAMESPACE="${SERVICE_INTEGRATION_DEPLOYMENT_NAME}"

BRO_CONFIGURATION=" --set eric-ctrl-bro.global.security.tls.enabled=false,\
eric-ctrl-bro.bro.enableAgentDiscovery=true,\
eric-ctrl-bro.bro.enableNotifications=true,\
eric-ctrl-bro.kafka.hostname=eric-data-message-bus-kf"

POSTGRES_SERVICE_NAME="eric-data-document-database-pg"
POSTGRES_SERVICE_CHART_REPO_URL="https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-adp-gs-all-helm/"

KPI_SERVICE_NAME="eric-pm-kpi-calculator"
KPI_SERVICE_CHART_REPO_URL="https://arm.epk.ericsson.se/artifactory/proj-ec-son-drop-helm/"

CM_SERVICE_NAME="eric-cm-topology-model-sn"
CM_SERVICE_CHART_REPO_URL="https://arm.epk.ericsson.se/artifactory/proj-ec-son-drop-helm/"

BRO_SERVICE_NAME="eric-ctrl-bro"
BRO_CHART_REPO_URL="https://arm.sero.gic.ericsson.se/artifactory/proj-adp-gs-all-helm"

if [[ $HELM_VERSION == 3 ]]; then
        TIMEOUT=5500s
    else
        TIMEOUT=5500
fi

NC='\033[0m' # No Color
BROWN='\033[0;33m'


function log() {
    echo -e "\n${BROWN} --- ${1} --- ${NC}\n"
}

function checkExitCode() {
    if [ $? -ne 0 ]; then
        log "ERROR: $1 "
        rollbackValuesChanges
        exit 255
    fi
}

function initRepo() {
    log "Initializing repositories"
    helm repo add ${POSTGRES_SERVICE_NAME}-repo ${POSTGRES_SERVICE_CHART_REPO_URL}
    helm repo add ${KPI_SERVICE_NAME}-repo ${KPI_SERVICE_CHART_REPO_URL}
    helm repo add ${CM_SERVICE_NAME}-repo ${CM_SERVICE_CHART_REPO_URL}
    helm repo add ${BRO_SERVICE_NAME}-repo ${BRO_CHART_REPO_URL}

    checkExitCode "Failed to update ${SERVICE_NAME} and ${SERVICE_INTEGRATION_NAME} dependencies!"
}

function cleanUpHelmEnvironment() {
    log "Cleaning up Helm environment"
    if [[ $HELM_VERSION == 3 ]]; then
            helm uninstall ${SERVICE_INTEGRATION_DEPLOYMENT_NAME} -n $NAMESPACE
            helm uninstall ${SERVICE_DEPLOYMENT_NAME} -n $NAMESPACE
        else
            helm delete --purge ${SERVICE_INTEGRATION_DEPLOYMENT_NAME}
            helm delete --purge ${SERVICE_DEPLOYMENT_NAME}
    fi

    log "Deleting namespace ${NAMESPACE}"
    kubectl delete namespace ${NAMESPACE}
}

function buildFlmDockerImage() {
    log "Building project"

    cd ${PROJECT_ROOT}

    echo $JAVA_HOME
    log "mvn clean install -Dskip-unit-tests=true "
    mvn clean install -Dskip-unit-tests=true

    checkExitCode "Failed to build maven project!"

    log "Building ${SERVICE_NAME}:${SERVICE_TAG}"
    docker build . -t armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_NAME}:${SERVICE_TAG}
    checkExitCode "Failed to build armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_NAME}:${SERVICE_TAG}"

    if [[ "$remote" -eq 1 ]]; then
        docker push armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_NAME}:${SERVICE_TAG}
    fi
}

function packageFlmChartToIntegration() {
    log "Packaging ${SERVICE_NAME}"

    cd "${PROJECT_ROOT}/charts/${SERVICE_NAME}"
    helm dependency update 2>&1 | grep -v "skipping loading invalid"

    cd ${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}
    helm dependency update 2>&1 | grep -v "skipping loading invalid"
    rm -rf charts/eric-son-frequency-layer-manager*

    updateHelmChartsValue "${PROJECT_ROOT}/charts/${SERVICE_NAME}" "ingress.hostname" "${SERVICE_INTEGRATION_HOSTNAME}"
    updateHelmChartsValue "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}" "ingress.hostname" "${SERVICE_INTEGRATION_HOSTNAME}"

    cd ${PROJECT_ROOT}
    sed -i "s/hostname: # overrides global hostname/hostname: ${SERVICE_INTEGRATION_HOSTNAME}/g" charts/eric-son-frequency-layer-manager/values.yaml

    helm package charts/eric-son-frequency-layer-manager -d charts/eric-son-frequency-layer-manager-integration/charts --version 1.2.3
}

function buildFlmIntegrationDockerImage() {
    log "Building ${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}"

    cd "$PROJECT_ROOT/testsuite/integration/jee/"
    docker build . -t armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}
    checkExitCode "Failed to build armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}"

    if [[ "$remote" -eq 1 ]]; then
        docker push armdocker.rnd.ericsson.se/proj-ec-son-dev/${SERVICE_INTEGRATION_NAME}:${SERVICE_INTEGRATION_TAG}
    fi

    cd "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}"
    sed -i "s+repository: armdocker.rnd.ericsson.se/proj-ec-son-ci-internal+repository: armdocker.rnd.ericsson.se/proj-ec-son-dev+g" values.yaml
    sed -i "s/tag: VERSION/tag: ${SERVICE_INTEGRATION_TAG}/g" values.yaml
}

function installFlmServiceAndFlmIntegration() {
    log "Installing Helm Charts"
    kubectl create ns ${NAMESPACE}

    cd ${PROJECT_ROOT}

    #LoadBalancers changed to ClusterIP to avoid external IP issue
    INSTALL_COMMAND_FLM="upgrade --install --devel ${SERVICE_DEPLOYMENT_NAME} "${PROJECT_ROOT}/charts/${SERVICE_NAME}" ${BRO_CONFIGURATION} --set eric-pm-kpi-calculator.retentionPeriod.days=8,imageCredentials.pullSecret=eric-son-frequency-layer-manager-secret,global.pullSecret=eric-son-frequency-layer-manager-secret,eric-son-frequency-layer-manager.parameters.numCallsCellHourlyReliabilityThresholdInHours=1,eric-son-frequency-layer-manager.parameters.syntheticCountersCellReliabilityThresholdInRops=1,tags.env-dev=true,eric-cm-topology-model-sn.zookeeper.enabled=true,kafka.configurationOverrides.auto.create.topics.enable=false,eric-cm-topology-model-sn.kafka.enabled=true,eric-aut-policy-engine-ax.zookeeper.enabled=false,eric-aut-policy-engine-ax.kafka.enabled=false,eric-pm-kpi-calculator.eric-pm-kpi-data.persistentVolumeClaim.enabled=true,eric-cm-topology-model-sn.eric-cm-son-topology-data.persistentVolumeClaim.enabled=true,eric-pm-kpi-calculator.eric-tcp-traffic-forward.service.type=ClusterIP,eric-tcp-traffic-forward.service.type=ClusterIP,imageCredentials.eric-son-frequency-layer-manager.repoPath=proj-ec-son-dev,images.eric-son-frequency-layer-manager.tag=${SERVICE_TAG} --namespace ${NAMESPACE}  --wait --timeout ${TIMEOUT}"
    echo "helm " ${INSTALL_COMMAND_FLM}
    helm ${INSTALL_COMMAND_FLM}
    checkExitCode "Failed to install $SERVICE_NAME"

    INSTALL_COMMAND_INT="upgrade --install --devel ${SERVICE_INTEGRATION_DEPLOYMENT_NAME} "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}" --set imageCredentials.pullSecret=eric-son-frequency-layer-manager-int-secret,global.pullSecret=eric-son-frequency-layer-manager-int-secret,eric-son-frequency-layer-manager.tags.env-dev=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.zookeeper.enabled=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.kafka.enabled=true,eric-son-frequency-layer-manager.eric-aut-policy-engine-ax.zookeeper.enabled=false,eric-son-frequency-layer-manager.eric-aut-policy-engine-ax.kafka.enabled=false  --namespace ${NAMESPACE} -f "${PROJECT_ROOT}/values.min.yaml"  --wait --timeout ${TIMEOUT}"
    echo "helm " ${INSTALL_COMMAND_INT}
    helm ${INSTALL_COMMAND_INT}
    checkExitCode "Failed to install $SERVICE_INTEGRATION_NAME"

    #LoadBalancers changed to ClusterIP to avoid external IP issue
    INSTALL_COMMAND_FLM="upgrade --install --devel ${SERVICE_DEPLOYMENT_NAME} "${PROJECT_ROOT}/charts/${SERVICE_NAME}" ${BRO_CONFIGURATION} --set eric-pm-kpi-calculator.retentionPeriod.days=8,imageCredentials.pullSecret=eric-son-frequency-layer-manager-secret,global.pullSecret=eric-son-frequency-layer-manager-secret,imageCredentials.eric-son-frequency-layer-manager.repoPath=proj-ec-son-dev,images.eric-son-frequency-layer-manager.tag=${SERVICE_TAG},eric-son-frequency-layer-manager.parameters.numCallsCellHourlyReliabilityThresholdInHours=1,eric-son-frequency-layer-manager.parameters.syntheticCountersCellReliabilityThresholdInRops=1,tags.env-dev=true,eric-cm-topology-model-sn.zookeeper.enabled=true,kafka.configurationOverrides.auto.create.topics.enable=false,eric-cm-topology-model-sn.kafka.enabled=true,eric-aut-policy-engine-ax.zookeeper.enabled=false,eric-aut-policy-engine-ax.kafka.enabled=false,eric-pm-kpi-calculator.eric-pm-kpi-data.persistentVolumeClaim.enabled=true,eric-cm-topology-model-sn.eric-cm-son-topology-data.persistentVolumeClaim.enabled=true,eric-pm-kpi-calculator.eric-tcp-traffic-forward.service.type=ClusterIP,eric-tcp-traffic-forward.service.type=ClusterIP --namespace ${NAMESPACE} -f "${PROJECT_ROOT}/values-integration-test.yaml"  --wait --timeout ${TIMEOUT}"
    echo "helm " ${INSTALL_COMMAND_FLM}
    helm ${INSTALL_COMMAND_FLM}
    checkExitCode "Failed to install $SERVICE_NAME"
}

function runHelmTest(){
    log "Running ${SERVICE_INTEGRATION_DEPLOYMENT_NAME} helm test"
    if [[ $HELM_VERSION == 3 ]]; then
        TEST_COMMAND="test ${SERVICE_INTEGRATION_DEPLOYMENT_NAME} -n ${NAMESPACE} --timeout ${TIMEOUT}"
      else
        TEST_COMMAND="test ${SERVICE_INTEGRATION_DEPLOYMENT_NAME} --timeout ${TIMEOUT}"
    fi
    echo "helm " ${TEST_COMMAND}
    helm ${TEST_COMMAND}

    extractLogs
}

function rollbackValuesChanges(){
    cd "${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}"
    sed -i "s+repository: armdocker.rnd.ericsson.se/proj-ec-son-dev+repository: armdocker.rnd.ericsson.se/proj-ec-son-ci-internal+g" values.yaml
    sed -i "s/tag: ${SERVICE_INTEGRATION_TAG}/tag: VERSION/g" values.yaml
    unix2dos values.yaml

    cd ${PROJECT_ROOT}/charts/${SERVICE_INTEGRATION_NAME}/charts
    rm -rf eric-son-frequency-layer-manager*
}

function extractLogs(){
    log "Extracting logs"
    cd ${HELM_TEST_LOCAL}
    kubectl logs ${SERVICE_INTEGRATION_NAME} -c eric-son-frequency-layer-manager-integration-runner --namespace ${NAMESPACE} > helmTestResults.log
    log "Finished extracting logs"
}

function userInput() {
    local _prompt _default _response

    _prompt="Do you wish to run Integration Tests on ECCD/KaaS server (Yes/No)? [y/n] ?"

    while true; do
        read -r -p "$_prompt " _response
        case "$_response" in
          [Yy][Ee][Ss]|[Yy])
            echo "Yes"
            return 1
            ;;
          [Nn][Oo]|[Nn])
            echo "No"
            return 0
            ;;
          *) # Anything else (including a blank) is invalid.
            ;;
        esac
    done
}

function waitForDeploymentReady() {
    DEPLOYMENTS=`kubectl get deployments -n ${NAMESPACE} -o=jsonpath='{.items[*].metadata.name}'`
    for DEPLOYMENT_NAME in $DEPLOYMENTS ; do
        kubectl rollout status deployment/${DEPLOYMENT_NAME} -n ${NAMESPACE} --timeout 900s
    done
}

function updateHelmChartsValue() {
  cd $1/charts
  for fileName in *.tgz; do
    dirName=`tar -tzf ${fileName} | sed -e 's@/.*@@' | uniq`

    tar -xzf ${fileName}

    python ${PROJECT_ROOT}/../son-dev-utils/scripts/update_yaml_entry.py --file="${dirName}/values.yaml" --key="${2}" --value="${3}"

    tar -czf ${fileName} ${dirName}
    rm -rf ${dirName}
  done
}

#########
# main
#########
echo "If you are running this test in an environment where the deployment of Frequency Layer Manager already exists, then it is advised to either remove that deployment or change the port number of the flmDatabaseExporter.externalService.port in your configuration."
SECONDS=0
userInput
remote="$?"

initRepo
cleanUpHelmEnvironment

buildFlmDockerImage
packageFlmChartToIntegration
buildFlmIntegrationDockerImage

installFlmServiceAndFlmIntegration
rollbackValuesChanges
waitForDeploymentReady
runHelmTest
duration=$SECONDS
echo "Script took - $(($duration / 60)) minutes and $(($duration % 60)) seconds to run."
