FROM node:latest

WORKDIR /front
COPY . .
RUN npm i uuid && npm install

EXPOSE 3000

CMD [ "npm", "run", "dev" ]
