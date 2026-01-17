# Stage 1: build React
FROM node:20-alpine AS build

WORKDIR /app

COPY ocr-frontend/package*.json ./
RUN npm install sockjs-client @stomp/stompjs
RUN npm ci
COPY ocr-frontend/. .
RUN npm run build

# Stage 2: serve via NGINX
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
