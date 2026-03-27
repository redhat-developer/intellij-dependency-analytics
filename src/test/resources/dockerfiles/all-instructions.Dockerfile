FROM ubuntu:latest
MAINTAINER Test User <test@example.com>
ARG APP_VERSION=1.0.0
ENV APP_HOME=/app
WORKDIR /app
COPY . .
ADD https://example.com/file.tar.gz /tmp/
RUN apt-get update && apt-get install -y curl
EXPOSE 8080
VOLUME ["/data"]
USER appuser
LABEL version="1.0" description="Test image"
HEALTHCHECK --interval=30s CMD curl -f http://localhost/
SHELL ["/bin/bash", "-c"]
STOPSIGNAL SIGTERM
ONBUILD RUN echo "triggered"
ENTRYPOINT ["/entrypoint.sh"]
CMD ["start"]
