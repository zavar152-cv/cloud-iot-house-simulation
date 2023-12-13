FROM node:latest

RUN apt-get -y update

WORKDIR /front
COPY . .
RUN npm i uuid && npm install && npm run build

EXPOSE 3000

CMD [ "npm", "run", "preview" ]
