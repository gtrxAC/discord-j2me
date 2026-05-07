FROM node:24-alpine
WORKDIR /app
RUN apk add git

ENV IN_DOCKER=1
COPY . .
RUN ["npm", "i"]
CMD ["node", "run_proxies"]

EXPOSE 8080
EXPOSE 8081