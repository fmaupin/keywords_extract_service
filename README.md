# Projet extract-service

Extraction des mots clés à partir de `chunks` issus d'une queue broker

Les mots clés sont stockés dans une base de données pour traitement de consolidation ultérieur

## Pré-requis

Installation du [JDK Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).

## Exécution, test, installation & génération app

```
mvn spring-boot:run -Dspring-boot.run.arguments="--my.password.broker=<my password broker>" -Dspring-boot.run.profiles=dev

mvn jacoco:prepare-agent test -Dmy.password.broker=<my password broker> install jacoco:report

mvn clean install -Dmy.password.broker=<my password broker> -DskipTests=true*

java -jar extract-service.jar --spring.profiles.active=dev --my.password.broker=<my password broker>
```

La valeur de "my password broker" va dépendre du password généré par le projet [keywords_broker](https://github.com/fmaupin/keywords_broker). 

## Test consommation chunks

Les `chunks` sont générés par le projet [keywords_read_content_service](https://github.com/fmaupin/keywords_read_content_service) et déposés sur la queue `qchunks`

## Utilisation de Stanford CoreNLP

Ce micro-service s’appuie sur un serveur Stanford CoreNLP pour extraire les entités nommées (personnes, lieux, organisations) et les noms propres.

Il détecte automatiquement la langue du texte et adapte le pipeline CoreNLP.

Le serveur CoreNLP doit être accessible à l’URL configurée pour que l’extraction fonctionne.

```
coreNLP:
  url-base: "http://localhost:9000/"
```

Cf. fichier `CORE_NLP.md` pour plus d'informations techniques.

**Prérequis**

* Serveur CoreNLP version 4.5.10 ou supérieure.

* Modèles anglais et français disponibles (stanford-corenlp-4.5.10-models.jar et stanford-corenlp-4.5.10-models-french.jar).

* Voir le fichier `README.md` du projet [CoreNLP Server](https://github.com/fmaupin/keywords_core_nlp) pour installation et lancement.


## Données de référence géographique

Les données se trouvent dans le fichier `src/resources/fr_cities.txt`

- Liste des communes françaises générée automatiquement à partir de :
  **Base officielle des codes postaux — INSEE / La Poste**  
  Source : [data.gouv.fr](https://www.data.gouv.fr/fr/datasets/base-officielle-des-codes-postaux/)  
  Licence : [Licence Ouverte 2.0 (Etalab)](https://www.etalab.gouv.fr/licence-ouverte-open-licence/)  
  © INSEE, République Française. Données réutilisées conformément à la Licence Ouverte 2.0.

Pour extraire les données : 

```
pip install requests

python <path>\script\generate_fr_cities.py
```

## Auteur

Ce projet a été créé par Fabrice MAUPIN.

## License

GNU General Public License v3.0

See [LICENSE](https://github.com/fmaupin/keywords_extract_service/blob/master/LICENSE) to see the full text.
