def k8sTest() {
    script {
        utils.waitForDeploymentReady(HELM_INSTALL_NAMESPACE, HELM_INSTALL_TIMEOUT)
        echo 'Update Helm integration chart dependencies:'
        if (env.HELM3 == "true"){
            sh '${HELM3_CMD} dependency update ${HELM_INT_CHART_DIRECTORY}'
        } else {
            sh '${HELM_CMD} dependency update ${HELM_INT_CHART_DIRECTORY}'
        }
 
        echo 'Replacing the pulled frequency-layer-manager chart with the latest one packaged in pipeline:'
        sh 'sudo chmod -fR 777 "${HELM_INT_CHART_DIRECTORY}/charts/" &&\
            find ${HELM_INT_CHART_DIRECTORY}/charts/ -name \'eric-son-frequency-layer-manager*\' -delete '
        sh 'cp -r ${HELM_CHART_PACKAGED} ${HELM_INT_CHART_DIRECTORY}/charts'
 
        echo 'Install the Helm integration chart:'
        sh "${KUBECTL_CMD} create ns ${HELM_INSTALL_INT_NAMESPACE}"
        sh "${KUBECTL_CMD} create secret docker-registry ${SERVICE_NAME}-secret \
            --docker-server=armdocker.rnd.ericsson.se \
            --docker-username=${CREDENTIALS_SEKA_ARTIFACTORY_USR} \
            --docker-password=${CREDENTIALS_SEKA_ARTIFACTORY_PSW} \
            -n ${HELM_INSTALL_INT_NAMESPACE} || true"
 
        if (env.HELM3 == "true"){
            sh '${HELM3_CMD} install \
            ${HELM_INSTALL_INT_RELEASE_NAME} ${HELM_INT_CHART_DIRECTORY} \
            --set testsuite.image.tag="$(cat .bob/var.version)" \
            --set ${HELM_SET_INT} \
            --set eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret,eric-son-frequency-layer-manager.images.eric-son-frequency-layer-manager.tag="$(cat .bob/var.version)",eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret \
            --namespace ${HELM_INSTALL_INT_NAMESPACE} \
            --debug \
            --dry-run > helm-int-install-dry-run.log || true'
        } else {
            sh '${HELM_CMD} install \
            --name ${HELM_INSTALL_INT_RELEASE_NAME} ${HELM_INT_CHART_DIRECTORY} \
            --set testsuite.image.tag="$(cat .bob/var.version)" \
            --set ${HELM_SET_INT} \
            --set eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret,eric-son-frequency-layer-manager.images.eric-son-frequency-layer-manager.tag="$(cat .bob/var.version)",eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret \
            --namespace ${HELM_INSTALL_INT_NAMESPACE} \
            --debug \
            --dry-run > helm-int-install-dry-run.log || true'
        }
 
        if (env.HELM3 == "true"){
            sh '${HELM3_CMD} upgrade --install \
            ${HELM_INSTALL_INT_RELEASE_NAME} ${HELM_INT_CHART_DIRECTORY} \
            --set testsuite.image.tag="$(cat .bob/var.version)" \
            --set ${HELM_SET_INT} \
            --set eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret,eric-son-frequency-layer-manager.images.eric-son-frequency-layer-manager.tag="$(cat .bob/var.version)",eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret \
            --namespace ${HELM_INSTALL_INT_NAMESPACE} \
            --timeout ${HELM3_INSTALL_TIMEOUT} \
            --wait || true'
        } else {
            sh '${HELM_CMD} install \
            --name ${HELM_INSTALL_INT_RELEASE_NAME} ${HELM_INT_CHART_DIRECTORY} \
            --set testsuite.image.tag="$(cat .bob/var.version)" \
            --set ${HELM_SET_INT} \
            --set eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret,eric-son-frequency-layer-manager.images.eric-son-frequency-layer-manager.tag="$(cat .bob/var.version)",eric-son-frequency-layer-manager.imageCredentials.pullSecret=${SERVICE_NAME}-secret \
            --namespace ${HELM_INSTALL_INT_NAMESPACE} \
            --timeout ${HELM_INSTALL_TIMEOUT} \
            --wait || true'
        }
        utils.waitForDeploymentReady(HELM_INSTALL_INT_NAMESPACE, HELM_INSTALL_TIMEOUT)
 
        echo 'Run the basic helm test, and run the Helm integration test:'
        if (env.HELM3 == "true"){
            sh '${HELM3_CMD} test ${HELM_INSTALL_INT_RELEASE_NAME} --namespace ${HELM_INSTALL_INT_NAMESPACE} --debug --timeout ${HELM3_INSTALL_TIMEOUT}'
        } else {
            sh '${HELM_CMD} test ${HELM_INSTALL_INT_RELEASE_NAME} --debug --timeout ${HELM_INSTALL_TIMEOUT}'
        }
    }
}
return this