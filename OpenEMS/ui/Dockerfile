FROM nginx:stable-alpine


COPY ./target/ /usr/share/nginx/html/

COPY ./nginx.conf /etc/nginx/conf.d/default.conf 

# EXPOSE 80 8084 8085
# 8084 REST/JSON 
# 8085 websocket with Edge

CMD ["nginx", "-g", "daemon off;"] 