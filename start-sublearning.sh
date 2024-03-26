#!/bin/bash
java -Dport=9090 -Ddatabase-url=jdbc:postgresql://localhost/sublearning_prod?user=subprod\&password=wakai20 -jar /home/mick/src/sub-learning-web/target/uberjar/sub-learning-web.jar
