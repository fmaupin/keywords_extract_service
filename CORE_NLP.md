# Rôle de CoreNLP

Ce que fait CoreNLP :
* Analyse syntaxique et morphologique du texte (POS tagging, dépendances grammaticales…).
* Reconnaissance d'entités nommées (NER) avec les modèles statistiques pré-entraînés :
    `PERSON`, `ORGANIZATION`, `LOCATION`, `CITY`, `COUNTRY`, `STATE_OR_PROVINCE`, etc.
* Détection des tokens et des phrases.

Limites :
* Les modèles anglais et français confondent souvent :
`ORGANIZATION` avec des lieux ou personnes célèbres (ex : “Real Madrid”, “Tokyo”, “NASA” → parfois incorrects). 
`CITY` avec des monuments (“Tour Eiffel”), des entreprises (“Amazon”), ou des événements.
`PERSON` avec des entités collectives (“Union Européenne”).
`LOCATION` omet souvent des **zones régionales** (ex : “Caraïbes”, “Asie du Sud-Est”, “Afrique de l’Ouest”) faute d’échantillons suffisants dans les modèles statistiques.

* Les entités extraites contiennent parfois :
des stopwords ou déterminants (“le Louvre”, “à Paris”, “de Google”),
des verbes, articles ou adjectifs parasites,
des tokens tronqués ou collés (“Université de”, “Services à Seattle”),
des entités incomplètes ou mal typées.

En résumé : CoreNLP donne une base brute correcte, mais il faut un post-traitement heuristique pour nettoyer et fiabiliser les entités.

# Rôle du post-traitement (CoreNLPHelper)

Le CoreNLPHelper intervient après la détection CoreNLP, en appliquant un ensemble de règles linguistiques et heuristiques adaptées au français et à l’anglais.

* Chargement dynamique des règles linguistiques

Toutes les règles sont chargées depuis des fichiers : `/entity_rules/fr/...` et `/entity_rules/en/...`

Ce qui permet d’adapter les comportements par langue :

- Stopwords (`stopwords_fr.txt`, `stopwords_en.txt`)
- Patterns invalides (`person_invalid_patterns.txt`, `organization_invalid_patterns.txt`)
- Indices sémantiques (`org_hints.txt`)
- Verbes courants (`common_verbs.txt`)
- Mots de géographie à exclure (`geography_stopwords.txt`)
- Mots en tête ou queue à ignorer (`leading_trailing_words.txt`)
- Règles de reclassification (`ner_post_mapping.txt`)
- **Régions géographiques reconnues** (`regions_fr.txt`, `regions_en.txt`)

Ainsi, aucune valeur n’est hardcodée dans le code, tout est **data-driven et multilingue**.

* Construction d’entités (`EntityBuilder`)

Le `EntityBuilder` regroupe les tokens successifs portant le même label NER ou des NNP (noms propres).
Mais avant d’ajouter une entité, il applique un ensemble de filtres :

1. Nettoyage du token

Normalisation des apostrophes, tirets, espaces insécables.
Suppression des caractères invisibles.

2. Merging intelligent

Regroupe les tokens consécutifs de même NER (France + Télévisions → France Télévisions).
Regroupe les NNP successifs (même sans NER explicite) → utile pour “Barack Obama”.

3. Filtrage par stopwords & leading/trailing

Ignore les entités commençant ou finissant par :
un stopword (“de”, “le”, “the”, “à”, etc.),
un mot passe-partout ou prépositionnel (“en”, “sur”, “pour”, etc.),
un verbe courant.

Évite des entités comme “à Paris”, “de Google”, “visiter Rome”.

4. Longueur maximale par type d’entité
Type NER	        Taille max	    Raison
____________________________________________________________________________________
`PERSON`	        ≤ 3 tokens	    Exclut “Union Européenne” ou “Royaume de France”
`CITY`	            ≤ 3 tokens	    Empêche “Services à Seattle”
`COUNTRY / STATE`	≤ 5 tokens	    Accepte “République de Corée”, exclut chaînes trop longues
`ORGANIZATION`	    ≤ 6 tokens	    Permet “Ministère de l’Éducation nationale”, évite phrases
`AUTRES`	        ≤ 10 tokens	    Sécurité générale

* Vérification contextuelle selon le type d’entité
`PERSON`
Élimine les patterns connus d’erreurs (person_invalid_patterns.txt) comme :
Titres sans nom (“Président”, “Directeur général”)
Expressions collectives (“Union Européenne”)

`ORGANIZATION`
Exclut les entités contenant un verbe ou article inutile.
Vérifie la présence d’un hint organisationnel (ex : groupe, université, ministère, inc, corp, etc.).
Si aucun hint n’est trouvé, l’entité est rejetée.
Applique les organization_invalid_patterns.txt pour exclure faux positifs connus.

`CITY / COUNTRY / STATE`
Vérifie que le premier token n’est pas un stopword géographique (ex : “le”, “à”, “du”).
Nettoie les monuments et entités culturelles confondues avec des lieux (Tour Eiffel, Colisée).

* Post-traitement géographique (`GeoRegionPostProcessor`)

Corriger les oublis de CoreNLP sur les **régions et zones géographiques**.  

Exemples :  
- “Caraïbes”, “Amérique latine”, “Asie du Sud-Est”, “Afrique de l’Ouest”, “Moyen-Orient”.

Fonctionnement :
1. Le texte complet est reconstitué à partir des tokens CoreNLP.  
2. Le module scanne le texte à l’aide de listes multilingues :
   - `/entity_rules/fr/regions_fr.txt`
   - `/entity_rules/en/regions_en.txt`
3. Si une région connue est trouvée **et absente des entités CoreNLP**, elle est ajoutée dans la catégorie `LOCATION`.  
4. Les doublons et collisions avec `CITY`, `COUNTRY`, `STATE` sont automatiquement gérés.

* Reclassification post-NER (ner_post_mapping.txt)

Certaines entités reconnues par CoreNLP sont reclassées après coup selon des regex :

Exemple de règle	      Avant	        Après	        Effet
______________________________________________________________________________
(?i).*Union.*Europ.*	  PERSON	    ORGANIZATION	Corrige “Union Européenne”
(?i).*Tour Eiffel.*	      CITY	        LOCATION	    Corrige “Tour Eiffel”
`(?i).Amazon.	          .Google.`	    CITY	        ORGANIZATION

Ce module est crucial pour corriger les erreurs sémantiques typiques du modèle.

# Résumé synthétique

Étape	                                                Objectif	                                              Réalisé par
_________________________________________________________________________________________________________________________________
Tokenisation, POS, NER	                                Extraction brute d’entités	                              ✅ CoreNLP
Nettoyage linguistique	                                Normalisation des tokens	                              ✅ CoreNLPHelper
Regroupement des tokens                                 cohérents	Reconstruction d’entités complètes	          ✅ CoreNLPHelper
Filtrage linguistique (stopwords, verbs, longueur)	    Élimine les entités bruitées	                          ✅ CoreNLPHelper
Vérification par pattern (invalid_patterns, hints)	    Évite les faux positifs	                                  ✅ CoreNLPHelper
Reclassification post-NER	                            Corrige les erreurs de typage	                          ✅ CoreNLPHelper
Détection des régions manquantes                        Ajoute “Caraïbes”, “Asie du Sud-Est”, etc.                ✅ GeoRegionPostProcessor

# Diagramme technique

          ┌──────────────────────────────┐
          │          Texte brut          │
          │ (FR / EN : phrase, message)  │
          └──────────────┬───────────────┘
                         │
                         ▼
       ┌───────────────────────────────────-───────┐
       │            CoreNLP (analyse NER)          │
       │-------------------------------------------│
       │ - Tokenisation                            │
       │ - POS Tagging (catégories grammaticales)  │
       │ - Named Entity Recognition (NER)          │
       │   → PERSON, ORGANIZATION, CITY, COUNTRY…  │
       └────────────────┬──────────────────────────┘
                        │
                        ▼
        ┌────────────────────────────────────────┐
        │          CoreNLPHelper (post-NER)      │
        │----------------------------------------│
        │ Chargement des règles selon langue :   │
        │   - stopwords_[fr|en].txt              │
        │   - organization_invalid_patterns.txt  │
        │   - person_invalid_patterns.txt        │
        │   - leading_trailing_words.txt         │
        │   - org_hints.txt                      │
        │   - geography_stopwords.txt            │
        │   - common_verbs.txt                   │
        │   - ner_post_mapping.txt               |
        |   - regions_[fr|en].txt                │
        └────────────────┬───────────────────────┘
                         │
                         ▼
       ┌─────────────────────────────────────────-───┐
       │      Étape 1 – Construction d’entités       │
       │---------------------------------------------│
       │ • Regroupe les tokens avec même NER ou NNP  │
       │ • Nettoie les apostrophes, tirets, etc.     │
       │ • Supprime les caractères invisibles        │
       └────────────────┬────────────────────────────┘
                        │
                        ▼
       ┌───────────────────────────────────────-─────┐
       │    Étape 2 – Nettoyage linguistique         │
       │---------------------------------------------│
       │ • Exclut les stopwords et mots passe-partout│
       │ • Supprime les verbes en tête d’entité      │
       │ • Filtre les leading/trailing words         │
       │ • Limite la longueur selon le type NER      │
       └────────────────┬────────────────────────────┘
                        │
                        ▼
       ┌─────────────────────────────────────────-───┐
       │     Étape 3 – Vérifications par type        │
       │---------------------------------------------│
       │ PERSON / ORGANIZATION / CITY / COUNTRY      │
       │ Nettoyage contextuel & sémantique           │
       └────────────────┬────────────────────────────┘
                        │
                        ▼
       ┌─────────────────────────────────────────-───┐
       │  Étape 4 – Reclassification post-NER        │
       │---------------------------------------------│
       │ • Application des regex du fichier mapping  │
       └────────────────┬────────────────────────────┘
                        │
                        ▼
       ┌────────────────────────────────────────┐
       │ Étape 5 – Enrichissement géographique  │
       │ Détection des régions (Caraïbes, etc.) │
       │ via GeoRegionPostProcessor             │
       └────────────────┬───────────────────────┘
                        │
                        ▼
       ┌────────────────────────────────────────┐
       │ Sortie finale (entités fiabilisées)    │
       │ PERSON: [Barack Obama, Angela Merkel]  │
       │ ORGANIZATION: [Google, UNESCO]         │
       │ CITY: [Paris, Madrid, Tokyo]           │
       │ COUNTRY: [France, Japon]               │
       │ LOCATION: [Tour Eiffel, Caraïbes]      │
       └────────────────────────────────────────┘

# Maintenance et évolutivité

## Ajout ou modification de règles linguistiques

Toutes les règles sont **définies dans des fichiers texte** sous `resources/entity_rules/`.

Chaque règle ou liste peut être modifiée sans recompiler le code :

/entity_rules/
├── fr/
│ ├── stopwords_fr.txt
│ ├── org_hints.txt
│ ├── regions_fr.txt
│ ├── ...
└── en/
├── stopwords_en.txt
├── org_hints.txt
├── regions_en.txt
├── ...

Recommandations :

1. **Toujours ajouter les nouveaux mots/régions en minuscules** (sauf exceptions propres aux noms propres).  

2. **Utiliser une ligne par entrée**, sans ponctuation.  

3. **Respecter la séparation par langue**

4. Pour ajouter une région manquante (ex : “Europe centrale”) :
   - Ajouter dans `regions_fr.txt`
   - Relancer le service → chargement automatique à la première exécution.

IMPORTANT : Les fichiers sont rechargés automatiquement lors du premier appel du `CoreNLPHelper`, et mis en cache statiquement jusqu’à la fin du cycle de vie de l’application.

## Extension à d’autres langues

Le système est **entièrement extensible** :  

Pour une nouvelle langue (ex : `es` pour l’espagnol), il suffit de créer :

/entity_rules/es/
stopwords_es.txt
org_hints.txt
regions_es.txt
...

et d’ajouter la logique de sélection `es` dans le `CoreNLPHelper`.

