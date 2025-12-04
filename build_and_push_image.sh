#!/bin/bash

# Définir variables
export GITHUB_USERNAME=$1
export GITHUB_TOKEN=$2

export TAG='latest'

DOCKERHUB_USERNAME=$3 
DOCKERHUB_TOKEN=$4

MODE=$5

# Se connecter à Docker Hub (pour récupérer image de référence utilisée par jib)
echo $DOCKERHUB_TOKEN | docker login -u $DOCKERHUB_USERNAME --password-stdin
if [ $? -ne 0 ]; then
  echo "Erreur : connexion Docker Hub a échoué"
  exit 1
fi

# Se connecter à GitHub Container Registry (pour pousser image buildée)
echo $GITHUB_TOKEN | docker login ghcr.io -u $GITHUB_USERNAME --password-stdin
if [ $? -ne 0 ]; then
  echo "Erreur : connexion GitHub Container Registry a échoué"
  exit 1
fi

# Extraire digest SHA256 image de référence
ERROR=$(docker inspect eclipse-temurin:17-jre --format='{{index .RepoDigests 0}}' 2>&1 >/dev/null)

# Vérifier si digest a été extrait correctement ?
if [[ "$ERROR" =~ "No such object" ]]; then
  docker pull eclipse-temurin:17-jre
fi

DIGEST=$(docker inspect eclipse-temurin:17-jre --format='{{index .RepoDigests 0}}' 2>/dev/null | cut -d'@' -f2)

# build & push image
mvn compile jib:build \
  -Djib.cleanCache=true \
  -Djib.httpTimeout=60000 \
  -Djib.from.image=eclipse-temurin@$DIGEST \
  -Dspring.profiles.active=$MODE
if [ $? -ne 0 ]; then
  echo "Erreur : build image a échoué"
  exit 1
fi

# Se déconnecter de Docker Hub & GitHub Container Registry
docker logout
docker logout ghcr.io

# Réinitialiser variables
unset GITHUB_USERNAME
unset GITHUB_TOKEN

unset TAG

unset DOCKERHUB_USERNAME
unset DOCKERHUB_TOKEN

unset DIGEST
unset ERROR
