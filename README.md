## projet extract-service

Extraction des mots clés à partir de `chunks` issus d'une queue broker

Les mots clés sont stockés dans une base de données pour traitement de consolidation ultérieur

### Pré-requis

Installation du [JDK Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).

### Exécution, test, installation & génération app

```
mvn spring-boot:run -Dspring-boot.run.arguments="--my.password.broker=<my password broker>" -Dspring-boot.run.profiles=dev

mvn jacoco:prepare-agent test -Dmy.password.broker=<my password broker> install jacoco:report

mvn clean install -Dmy.password.broker=<my password broker> -DskipTests=true*

java -jar extract-service.jar --spring.profiles.active=dev --my.password.broker=<my password broker>
```

La valeur de "my password broker" va dépendre du password généré par le projet [keywords_broker](https://github.com/fmaupin/keywords_broker). 

### Test consommation chunks

Les `chunks` sont générés par le projet [keywords_read_content_service](https://github.com/fmaupin/keywords_read_content_service) et déposés sur la queue `qchunks`

### Auteur

Ce projet a été créé par Fabrice MAUPIN.

### License

GNU General Public License v3.0

See [LICENSE](https://github.com/fmaupin/ms_poc_1/blob/master/LICENSE) to see the full text.



