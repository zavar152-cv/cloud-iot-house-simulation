FROM nginx
EXPOSE 80 443

COPY ./${CONFIG} /etc/nginx/
WORKDIR /etc/nginx
CMD ["nginx", "-g", "daemon off;"]