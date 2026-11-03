#
# Build stage
#
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL
ARG VITE_IDP
ARG VITE_KC_URL
ARG VITE_KC_REALM
ARG VITE_KC_CLIENTID
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_IDP=$VITE_IDP
ENV VITE_KC_URL=$VITE_KC_URL
ENV VITE_KC_REALM=$VITE_KC_REALM
ENV VITE_KC_CLIENTID=$VITE_KC_CLIENTID
RUN npm run build

#
# Serve stage
#
FROM node:22-alpine
RUN npm install -g serve
WORKDIR /app
COPY --from=build /app/dist ./dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
