ARG ECSON_WILDFLY_BASE_VERSION=1.0.312
FROM armdocker.rnd.ericsson.se/proj-eson-3pp/ecson-wildfly-base:${ECSON_WILDFLY_BASE_VERSION}
COPY ./eric-frequency-layer-manager-ear/target/*.ear /ericsson/3pp/wildfly/standalone/deployments/

RUN echo "142674:x:142674:142674:An Identity for eric-frequency-layer-manager:/flmuser:/bin/false" >> /etc/passwd \
    && echo "142674:!::0:::::" >> /etc/shadow

RUN chown -R 142674:0 /ericsson/3pp/ \
    && chmod -R g=u /ericsson/3pp/

USER 142674

EXPOSE 8080 9999 9990
