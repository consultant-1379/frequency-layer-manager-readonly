def k8sInstall() {
    script {
            echo 'Initial install of the service helm chart:'
            sh "${KUBECTL_CMD} create ns ${HELM_INSTALL_NAMESPACE} || true"
            sh "${KUBECTL_CMD} create secret docker-registry ${SERVICE_NAME}-secret \
                --docker-server=armdocker.rnd.ericsson.se \
                --docker-username=${CREDENTIALS_SEKA_ARTIFACTORY_USR} \
                --docker-password=${CREDENTIALS_SEKA_ARTIFACTORY_PSW} \
                -n ${HELM_INSTALL_NAMESPACE} || true"
            if (env.HELM3 == "true"){
                sh '${HELM3_CMD} upgrade \
                --install ${HELM_INSTALL_RELEASE_NAME} ${HELM_CHART_PACKAGED} \
                --set ${HELM_SET} \
                --namespace ${HELM_INSTALL_NAMESPACE} \
                --timeout ${HELM3_INSTALL_TIMEOUT} \
                --devel \
                --wait'
            } else {
                sh '${HELM_CMD} upgrade \
                --install ${HELM_INSTALL_RELEASE_NAME} ${HELM_CHART_PACKAGED} \
                --set ${HELM_SET} \
                --namespace ${HELM_INSTALL_NAMESPACE} \
                --timeout ${HELM_INSTALL_TIMEOUT} \
                --devel \
                --wait'
            }
    }
}
return this