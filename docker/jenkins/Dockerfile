# Jenkins LTS + JDK 21 (khớp java.version của dự án) + Maven
FROM jenkins/jenkins:lts-jdk21

USER root

# Cài Maven để Jenkinsfile gọi `mvn` trực tiếp được
RUN apt-get update \
    && apt-get install -y --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

USER jenkins
