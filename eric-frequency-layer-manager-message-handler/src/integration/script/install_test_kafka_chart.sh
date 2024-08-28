#
#  * *------------------------------------------------------------------------------
#  * ******************************************************************************
#  *  COPYRIGHT Ericsson 2020
#  *
#  *  The copyright to the computer program(s) herein is the property of
#  *  Ericsson Inc. The programs may be used and/or copied only with written
#  *  permission from Ericsson Inc. or in accordance with the terms and
#  *  conditions stipulated in the agreement/contract under which the
#  *  program(s) have been supplied.
#  * ******************************************************************************
#  * ------------------------------------------------------------------------------
#

#kubectl delete namespace flm-test-kafka-consumers
#kubectl create namespace flm-test-kafka-consumers
helm upgrade --install flm-test-kafka-consumer test-kafka-chart -n flm-test-kafka-consumers --wait --timeout 180s
#kubectl -n flm-test-kafka-consumers exec eric-data-message-bus-kf-0 -- kafka-topics --create --bootstrap-server eric-data-message-bus-kf:9092 --partitions 10 --replication-factor 1 --topic flmPolicyOutputEventTopic
