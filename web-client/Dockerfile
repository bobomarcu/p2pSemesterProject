# -------------------------------------------------------------------
# STAGE 1: Build (Node.js)
# -------------------------------------------------------------------
FROM node:20-alpine AS builder

WORKDIR /app

# Copiem fișierele de configurare npm
COPY package*.json ./

# Copiem codul sursă
COPY . .

# Generăm build-ul de producție (creează folderul 'build')
RUN npm run build

# -------------------------------------------------------------------
# STAGE 2: Serve (Nginx)
# -------------------------------------------------------------------
FROM nginx:alpine

# Copiem configurarea custom Nginx (vezi punctul 3 de mai jos)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Curățăm folderul default Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiem fișierele statice (HTML + CSS + JS) din builder
# NOTĂ: react-scripts generează în folderul 'build'
COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]