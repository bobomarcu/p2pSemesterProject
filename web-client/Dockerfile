# -------------------------------------------------------------------
# STAGE 1: Build (Node.js)
# -------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

COPY . .

RUN npm run build

# -------------------------------------------------------------------
# STAGE 2: Serve (Nginx)
# -------------------------------------------------------------------
FROM nginx:alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN rm -rf /usr/share/nginx/html/*

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]