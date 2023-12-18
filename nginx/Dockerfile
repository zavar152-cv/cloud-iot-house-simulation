FROM nginx
EXPOSE 80 443

COPY . /etc/nginx/
WORKDIR /etc/nginx
CMD ["nginx", "-g", "daemon off;"]