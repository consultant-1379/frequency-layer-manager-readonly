env.SERVICE_NAME = "eric-son-frequency-layer-manager"
env.HELM_SET = "imageCredentials.pullSecret=${SERVICE_NAME}-secret,tags.env-dev=true,eric-cm-topology-model-sn.zookeeper.enabled=true,kafka.configurationOverrides.auto.create.topics.enable=false,eric-cm-topology-model-sn.kafka.enabled=true"
env.HELM_SET_INT = "imageCredentials.pullSecret=${SERVICE_NAME}-int-secret,eric-son-frequency-layer-manager.tags.env-dev=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.zookeeper.enabled=true,eric-son-frequency-layer-manager.eric-cm-topology-model-sn.kafka.enabled=true,eric-son-frequency-layer-manager.kafka.createTopics=true"
env.HELM_SET_LINT = "eric-son-flm-data.imageCredentials.repoPath=proj-document-database-pg/data,eric-son-flm-data.images.postgres.name=eric-data-document-database-pg,eric-son-flm-data.images.postgres.tag=4.0.0+35"
env.HELM_CHART_REPO = "https://arm.epk.ericsson.se/artifactory/proj-ec-son-drop-helm"
env.HELM_CHART_PACKAGED = "${WORKSPACE}/.bob/${SERVICE_NAME}-internal/*.tgz"
env.HELM_INSTALL_RELEASE_NAME = "${SERVICE_NAME}-install"
env.HELM_INSTALL_NAMESPACE = "${HELM_INSTALL_RELEASE_NAME}"
env.HELM_INSTALL_TIMEOUT = 1500
env.HELM3_INSTALL_TIMEOUT = "${HELM_INSTALL_TIMEOUT}s"
env.HELM_INSTALL_INT_RELEASE_NAME = "${SERVICE_NAME}-integration-test"
env.HELM_INSTALL_INT_NAMESPACE = "${HELM_INSTALL_INT_RELEASE_NAME}"
env.HELM_INT_CHART_DIRECTORY = "${WORKSPACE}/charts/${SERVICE_NAME}-integration"
env.GERRIT_REPO_NAME = "frequency-layer-manager"

//stage overwrite flag
env.HELM3 = "true"
env.STAGE_OVERWRITE_K8S_INSTALL = "true"
env.STAGE_OVERWRITE_POSTALWAYS = "true"