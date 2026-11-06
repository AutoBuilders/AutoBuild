# Stage 1: Build
FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

ARG VITE_IDP=keycloak
ARG VITE_API_BASE_URL=http://localhost:8080/api/v1
ARG VITE_KC_URL=http://localhost:8180
ARG VITE_KC_REALM=SpringBootRealm
ARG VITE_KC_CLIENTID=pipeline-app

ENV VITE_IDP=$VITE_IDP
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KC_URL=$VITE_KC_URL
ENV VITE_KC_REALM=$VITE_KC_REALM
ENV VITE_KC_CLIENTID=$VITE_KC_CLIENTID

RUN npm run build

# Stage 2: Serve
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
