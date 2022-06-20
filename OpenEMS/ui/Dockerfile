# Dockerize the frontend Angular application for crimescripts.nl
# Support: https://dzone.com/articles/how-to-dockerize-angular-app

# First build the angular application in a seperate docker
FROM node:16-alpine as build-step

RUN mkdir /app

WORKDIR /app

COPY package.json /app

RUN npm install -g @angular/cli

RUN npm install

COPY . /app

RUN ng build -c "openems,openems-edge-prod,prod"

RUN ls -l /app/target/

# Copy the result to the nginx docker
FROM nginx:1.17.1-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build-step /app/target /usr/share/nginx/html

RUN ls -l /usr/share/nginx/html

