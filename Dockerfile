FROM anapsix/alpine-java
MAINTAINER Michael Rideout 

ADD ./target/uberjar/sub-learning-web.jar sub-learning-web.jar
COPY ./entrypoint.sh /

ENV PORT 4040

EXPOSE 4040

RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
