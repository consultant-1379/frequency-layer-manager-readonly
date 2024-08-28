ARG CBO_VERSION="5.18.0-14"
FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBO_VERSION}
ARG CBOS_VERSION="5.18.0-14"
ARG CBO_REPO_URL=https://arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}

#Install openJDK 11
ENV JAVA_VERSION=11.0.17 \
    JAVA_HOME=/usr/

RUN zypper addrepo -C -G -f ${CBO_REPO_URL}?ssl_verify=no \
    COMMON_BASE_OS_SLES_REPO \
    && zypper install -l -y java-11-openjdk-headless curl \
    && zypper clean --all \
    && zypper rr COMMON_BASE_OS_SLES_REPO

ENV  FLM_HOSTNAME=wildfly \
     FLM_PORT=8080

RUN mkdir -p /opt/flm-service-testsuite
COPY ./target/testsuite-uber-jar.jar /opt/flm-service-testsuite
ENTRYPOINT [ "java","-jar","/opt/flm-service-testsuite/testsuite-uber-jar.jar" ]
