#!/bin/bash

echo "Install Docker"
apt-get remove -y docker docker-engine docker.io containerd runc
apt-get update
apt-get install -y \
    ca-certificates \
    curl \
    gnupg

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin


# echo "Install Java"
# wget https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.19%2B7/OpenJDK11U-jdk_x64_linux_hotspot_11.0.19_7.tar.gz
# tar -xf OpenJDK11U-jdk_x64_linux_hotspot_11.0.19_7.tar.gz
# mv jdk-11.0.19+7 /opt/jdk11
# rm OpenJDK11U-jdk_x64_linux_hotspot_11.0.19_7.tar.gz

# echo "Install Maven"
# wget https://dlcdn.apache.org/maven/maven-3/3.9.1/binaries/apache-maven-3.9.1-bin.tar.gz
# tar -xf apache-maven-3.9.1-bin.tar.gz
# mv apache-maven-3.9.1 /opt/apache-maven-3.9.1
# rm apache-maven-3.9.1-bin.tar.gz

# usermod -aG docker cc

# echo "Install git-lfs"
# pushd ../../
# apt install -y git-lfs
# git lfs install
# git lfs pull
# popd

# echo "
# export JDK_HOME=/opt/jdk11
# export JAVA_HOME=${JDK_HOME}
# export MAVEN_HOME=/opt/apache-maven-3.9.1
# export PATH=$PATH:${JAVA_HOME}/bin:$MAVEN_HOME/bin
# " >> ~/.bashrc
