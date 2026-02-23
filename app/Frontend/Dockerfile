# Stage 1: Build
FROM node:20-alpine AS builder

# Install pnpm
RUN npm install -g pnpm

# Set working directory
WORKDIR /app

# Copy package files
COPY package.json pnpm-lock.yaml* ./

# Install dependencies
RUN pnpm install

# Copy source code
COPY . .

# Build the application with environment variables
ARG VITE_API_TRANSACTIONS_URL
ARG VITE_API_REPORTS_URL
ARG VITE_API_AUTH_URL

ENV VITE_API_TRANSACTIONS_URL=$VITE_API_TRANSACTIONS_URL
ENV VITE_API_REPORTS_URL=$VITE_API_REPORTS_URL
ENV VITE_API_AUTH_URL=$VITE_API_AUTH_URL

RUN pnpm build

# Stage 2: Production
FROM nginx:stable-alpine AS production

# Install curl for health check
RUN apk add --no-cache curl

# Remove default nginx configuration
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Set permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    chown -R nginx:nginx /tmp

# Switch to non-root user
USER nginx

# Expose port 3000
EXPOSE 3000

# Start nginx
CMD ["nginx", "-g", "daemon off;"]