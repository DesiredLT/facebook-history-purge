FROM node:22-alpine
WORKDIR /app
COPY --chown=node:node ai-service ./ai-service
COPY --chown=node:node vaeloria-android/app/src/main/assets/ai ./vaeloria-android/app/src/main/assets/ai
USER node
ENV HOST=0.0.0.0 PORT=8080
EXPOSE 8080
CMD ["node", "ai-service/server.mjs"]
