def postalways() {
    script {
    utils.postAlways()
    utils.staticAnalysisReports()
    if (env.K8S == "true") {
        logging.get_logs_for_each_namespace(HELM_INSTALL_NAMESPACE, HELM_INSTALL_INT_NAMESPACE)
        if (env.HELM3 == "true"){
            utils.cleanupHelm3Releases(HELM_INSTALL_RELEASE_NAME, HELM_INSTALL_INT_NAMESPACE)
        } else {
            utils.cleanupHelmReleases(HELM_INSTALL_RELEASE_NAME, HELM_INSTALL_INT_NAMESPACE)
        }
        archiveArtifacts allowEmptyArchive: true, artifacts: 'logs_*.tgz'
    }
    }
}
return this