FROM jenkins/jenkins:lts-jdk17

USER root

# Install Docker CLI, Docker Compose, Maven, Node.js (LTS), Angular CLI, and Chrome
RUN curl -fsSLO https://download.docker.com/linux/static/stable/x86_64/docker-26.1.3.tgz && \
    tar xzvf docker-26.1.3.tgz --strip 1 -C /usr/local/bin docker/docker && \
    rm docker-26.1.3.tgz && \
    mkdir -p /usr/local/lib/docker/cli-plugins && \
    curl -SL https://github.com/docker/compose/releases/download/v2.27.0/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose && \
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose && \
    docker --version && docker compose version && \
    apt-get update && apt-get install -y maven && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g @angular/cli && \
    apt-get update && apt-get install -y wget && \
    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb && \
    rm -rf /var/lib/apt/lists/* && \
    mvn -version && node -v && npm -v

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

USER jenkins
