#
# Build stage
#
FROM node:22-alpine AS build
RUN apk add --no-cache openjdk17-jre-headless
WORKDIR /app
COPY pipeline-ui/package*.json ./
RUN npm ci
COPY pipeline-ui/ .
COPY apis/openapi-spec/ /apis/openapi-spec/
RUN npm run generate:client-code
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
# Runtime stage
#
FROM nginx:alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/dist /usr/share/nginx/html
COPY pipeline-ui/nginx.conf /etc/nginx/conf.d/default.conf
RUN chown -R appuser:appgroup /usr/share/nginx/html \
    && chmod -R 755 /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
