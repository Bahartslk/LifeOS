# syntax=docker/dockerfile:1

# ---- deps: install dependencies once, cached across builds ----
FROM node:20-alpine AS deps
WORKDIR /app
COPY backend/package.json backend/package-lock.json* ./
RUN npm ci

# ---- build: compile TypeScript and generate the Prisma client ----
FROM node:20-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY backend/ .
RUN npx prisma generate
RUN npm run build

# ---- runtime: minimal image with only what's needed to run ----
FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production

# Prisma's query engine is a native binary linked against system OpenSSL —
# Alpine doesn't ship it by default, and Node's own bundled OpenSSL doesn't
# count (it's statically linked into the Node binary, not a shared library
# other processes can dlopen).
RUN apk add --no-cache openssl

# node_modules comes from `build`, not `deps` — only `build` ran
# `prisma generate`, so only its node_modules/.prisma/client has the
# generated client. Copying from `deps` (pre-generate) leaves a stub
# @prisma/client that fails at startup with "did not initialize yet".
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/dist ./dist
COPY --from=build /app/prisma ./prisma
COPY backend/package.json ./package.json

EXPOSE 3000
CMD ["node", "dist/main.js"]
