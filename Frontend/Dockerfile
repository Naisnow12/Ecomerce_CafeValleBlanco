# Etapa 1: Compilación de Angular SSR con Node 22 y pnpm
FROM node:22-alpine AS build
WORKDIR /app

# Habilitar pnpm (versión alineada con packageManager en package.json)
RUN corepack enable && corepack prepare pnpm@11.24.0 --activate

# Copiar manifiestos e instalar dependencias
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile --dangerously-allow-all-builds

# Copiar código fuente y generar bundle SSR
COPY . .
RUN pnpm run build

# Etapa 2: Imagen ligera de ejecución (solo runtime Node + dist)
FROM node:22-alpine AS runtime
WORKDIR /app

ENV NODE_ENV=production
ENV PORT=4000

COPY --from=build /app/dist ./dist

EXPOSE 4000

CMD ["node", "dist/Frontend/server/server.mjs"]
