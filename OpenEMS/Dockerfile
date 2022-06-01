# select parent image
FROM openjdk:latest

COPY openemsEdgeNew.jar /usr/lib/openems/emsEdgeNew.jar
# copy the packaged jar file into our docker image

RUN mkdir /etc/openems.d
# This is going to be the place, where all the bundle configurations are held.

# EXPOSE 8080 8084 8085 502 8082
# 8080 Felix
# 8084 REST/JSON 
# 8085 UI websocket
# 502  modbus
# 8082 websocket


# set the startup command to execute the jar
CMD ["java", "-Dfelix.cm.dir=/etc/openems.d/", "-jar", "/usr/lib/openems/emsEdgeNew.jar"]