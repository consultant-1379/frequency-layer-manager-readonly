#Running test with local Kafka
In order to run ExecutionConsumerHandlerKafkaTest app, you will need to do the followings:
* Install the test-kafka-chart on your local Docker Desktop
  * run the `install_test_kafka_chart.sh` script from where the script is
* Create the following item in `C:/Windows/System32/drivers/etc/hosts` file

`127.0.0.1 eric-data-message-bus-kf-0.eric-data-message-bus-kf.flm-test-kafka-consumers`
* Create a port forward to eric-data-message-bus ADP compoenent in local terminal
  * `kubectl port-forward -n flm-test-kafka-consumers eric-data-message-bus-kf-0 9092:9092`
* Set the bootstrap server in code to 
`eric-data-message-bus-kf-0.eric-data-message-bus-kf.flm-test-kafka-consumers:9092`
