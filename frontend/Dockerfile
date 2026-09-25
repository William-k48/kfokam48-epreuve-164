# ============================================================
# Frontend React + Vite — image de production
# Multi-stage : build avec Node, service avec Nginx
# Port exposé : 4200
# ============================================================

# --- Étape 1 : build ---
FROM node:20-alpine AS build
WORKDIR /app

# Copie des fichiers de dépendances d'abord (cache Docker)
COPY package*.json ./
RUN npm ci

# Copie du reste du code
COPY . .

# Build de production
RUN npm run build

# --- Étape 2 : runtime ---
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Configuration Nginx personnalisée (port 4200 + SPA fallback)
COPY --from=build /app/dist .
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 4200
CMD ["nginx", "-g", "daemon off;"]