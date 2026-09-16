# Jenkins LTS + JDK 21 (khớp java.version của dự án) + Maven + Docker CLI
FROM jenkins/jenkins:lts-jdk21

USER root

# Maven để gọi `mvn` trực tiếp; docker.io cung cấp Docker CLI (client) để `docker build/push`.
# Daemon dùng của host qua socket mount (Docker-outside-of-Docker), không chạy daemon trong container.
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven docker.io \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
