#/bin/bash

sudo docker build -t openems/ui .
sudo docker container stop openems-ui
sudo docker container rm openems-ui
sudo docker run -d -it -p 0.0.0.0:80:80 --name openems-ui openems/ui

