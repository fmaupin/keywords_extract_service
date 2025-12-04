/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Extract Micro Service.
 *
 * Read Content Micro Service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.fmaupin.keywords.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fmaupin.keywords.exception.CoreNLPServerException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;

/**
 * CoreNLPHelper
 *
 * Boite à outils pour serveur coreNLP.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 03/11/25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class CoreNLPHelper {

    private static final String ORGANIZATION_ENTITY = "ORGANIZATION";

    private static final String PERSON_ENTITY = "PERSON";

    private static final String LOCATION_ENTITY = "LOCATION";

    private static Set<String> stopwordsFR;
    private static Set<String> stopwordsEN;

    private static Set<Pattern> orgInvalidPatternsFR;
    private static Set<Pattern> orgInvalidPatternsEN;

    private static Set<Pattern> personInvalidPatternsFR;
    private static Set<Pattern> personInvalidPatternsEN;

    private static Set<String> leadingTrailingWordsFR;
    private static Set<String> leadingTrailingWordsEN;

    private static Set<String> orgHintsFR;
    private static Set<String> orgHintsEN;

    private static Set<String> geoStopwordsFR;
    private static Set<String> geoStopwordsEN;

    private static Set<String> commonVerbsFR;
    private static Set<String> commonVerbsEN;

    private static Map<Pattern, String> nerPostMappingFR;
    private static Map<Pattern, String> nerPostMappingEN;

    static {
        try {
            try (InputStream fr = CoreNLPHelper.class.getResourceAsStream("/entity_rules/fr/stopwords.txt");
                    InputStream en = CoreNLPHelper.class.getResourceAsStream("/entity_rules/en/stopwords.txt")) {

                if (fr != null) {
                    stopwordsFR = new BufferedReader(new InputStreamReader(fr, StandardCharsets.UTF_8))
                            .lines().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
                } else {
                    stopwordsFR = Collections.emptySet();
                }

                if (en != null) {
                    stopwordsEN = new BufferedReader(new InputStreamReader(en, StandardCharsets.UTF_8))
                            .lines().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
                } else {
                    stopwordsEN = Collections.emptySet();
                }
            }

            orgInvalidPatternsFR = ResourceLoader.loadPatterns("/entity_rules/fr/organization_invalid_patterns.txt");
            orgInvalidPatternsEN = ResourceLoader.loadPatterns("/entity_rules/en/organization_invalid_patterns.txt");

            personInvalidPatternsFR = ResourceLoader.loadPatterns("/entity_rules/fr/person_invalid_patterns.txt");
            personInvalidPatternsEN = ResourceLoader.loadPatterns("/entity_rules/en/person_invalid_patterns.txt");

            leadingTrailingWordsFR = ResourceLoader.loadLines("/entity_rules/fr/leading_trailing_words.txt");
            leadingTrailingWordsEN = ResourceLoader.loadLines("/entity_rules/en/leading_trailing_words.txt");

            orgHintsFR = ResourceLoader.loadLines("/entity_rules/fr/org_hints.txt");
            orgHintsEN = ResourceLoader.loadLines("/entity_rules/en/org_hints.txt");

            geoStopwordsFR = ResourceLoader.loadLines("/entity_rules/fr/geography_stopwords.txt");
            geoStopwordsEN = ResourceLoader.loadLines("/entity_rules/en/geography_stopwords.txt");

            commonVerbsFR = ResourceLoader.loadLines("/entity_rules/fr/common_verbs.txt");
            commonVerbsEN = ResourceLoader.loadLines("/entity_rules/en/common_verbs.txt");

            nerPostMappingFR = ResourceLoader.loadMappings("/entity_rules/fr/ner_post_mapping.txt");
            nerPostMappingEN = ResourceLoader.loadMappings("/entity_rules/en/ner_post_mapping.txt");
        } catch (IOException e) {
            throw new CoreNLPServerException("Error loading CoreNLP helper resources", e);
        }
    }

    /**
     * Extraction des entités nommées depuis la réponse JSON de CoreNLP.
     * 
     * @param jsonResponse : réponse JSON de CoreNLP
     * @param langDetected : langue détectée du texte
     * 
     * @return Map des entités nommées extraites
     * @throws CoreNLPServerException
     */
    public static Map<String, List<String>> extractEntities(String jsonResponse, String langDetected)
            throws CoreNLPServerException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        try {
            JsonNode root = mapper.readTree(jsonResponse);
            Map<String, List<String>> entities = new HashMap<>();

            // 1. Parcours des phrases (extraction brute)
            for (JsonNode sentence : root.path("sentences")) {
                new EntityBuilder(langDetected).processSentence(sentence, entities);
            }

            // 2. Post-correction des labels NER
            entities = applyPostNERMapping(entities, langDetected);

            // 3. econstitution du texte pour enrichissement géographique
            String text = "";

            if (root.hasNonNull("text") && !root.path("text").asText().isEmpty()) {
                text = root.path("text").asText();
            } else {
                // 🔹 Reconstitue le texte à partir des tokens
                StringBuilder sb = new StringBuilder();

                for (JsonNode sentence : root.path("sentences")) {
                    for (JsonNode token : sentence.path("tokens")) {
                        sb.append(token.path("word").asText()).append(" ");
                    }
                }

                text = sb.toString().trim();
            }

            // 4. Enrichissement des entités géographiques
            List<String> locList = entities.getOrDefault(LOCATION_ENTITY, new ArrayList<>());

            if (!locList.isEmpty()) {
                Set<String> enrichedLocs = GeoRegionPostProcessor.enrichWithRegions(text, new HashSet<>(locList),
                        langDetected);
                entities.put(LOCATION_ENTITY, new ArrayList<>(enrichedLocs));
            }

            // 5. Déduplication : suppression des entités LOCATION des autres catégories
            deduplicateGeographicEntities(entities);

            return entities;
        } catch (JsonProcessingException e) {
            throw new CoreNLPServerException("Error parsing CoreNLP JSON response", e);
        }
    }

    @SuppressWarnings("java:S3776")
    private static Map<String, List<String>> applyPostNERMapping(Map<String, List<String>> entities, String lang) {
        Map<Pattern, String> rules = "fr".equalsIgnoreCase(lang) ? nerPostMappingFR : nerPostMappingEN;
        Map<String, List<String>> corrected = new HashMap<>();

        for (Map.Entry<String, List<String>> e : entities.entrySet()) {
            for (String entity : e.getValue()) {
                String newLabel = e.getKey();

                for (Map.Entry<Pattern, String> rule : rules.entrySet()) {
                    if (rule.getKey().matcher(entity).matches()) {
                        if (log.isDebugEnabled())
                            log.debug("NER correction: '{}' [{}] → [{}] via {}", entity, e.getKey(), rule.getValue(),
                                    rule.getKey());

                        newLabel = rule.getValue();
                        break;
                    }
                }

                corrected.computeIfAbsent(newLabel, k -> new ArrayList<>()).add(entity);
            }
        }

        return corrected;
    }

    /**
     * Supprime des autres labels les entités maintenant détectées comme LOCATION.
     */
    private static void deduplicateGeographicEntities(Map<String, List<String>> entities) {
        List<String> locations = entities.getOrDefault(LOCATION_ENTITY, List.of());

        if (locations.isEmpty())
            return;

        List<String> lowerPriorityLabels = List.of(PERSON_ENTITY, ORGANIZATION_ENTITY);

        for (String label : lowerPriorityLabels) {
            List<String> filtered = entities.getOrDefault(label, new ArrayList<>()).stream()
                    .filter(e -> !locations.contains(e))
                    .toList();

            entities.put(label, filtered);
        }
    }

    /**
     * EntityBuilder
     *
     * Construction d'entitées à partir d'une phrase.
     *
     * @author Fabrice MAUPIN
     * @version 0.0.1-SNAPSHOT
     * @since 05/11/25
     */
    private static class EntityBuilder {
        private final StringBuilder buffer = new StringBuilder();

        private String currentNER = "";
        private String lastNER = "";
        private boolean lastWasNNP = false;
        private final String langDetected;

        private final Set<String> stopwords;
        private final Set<String> geoStopwords;
        private final Set<String> leadingTrailing;
        private final Set<String> verbs;
        private final Set<String> orgHints;

        EntityBuilder(String langDetected) {
            this.langDetected = langDetected;

            // Stopwords généraux
            this.stopwords = "fr".equalsIgnoreCase(langDetected) ? stopwordsFR : stopwordsEN;

            // Stopwords pour les entités géographiques
            this.geoStopwords = "fr".equalsIgnoreCase(langDetected) ? geoStopwordsFR
                    : geoStopwordsEN;

            // Leading/trailing words
            this.leadingTrailing = "fr".equalsIgnoreCase(langDetected) ? leadingTrailingWordsFR
                    : leadingTrailingWordsEN;

            // Verbes communs
            this.verbs = "fr".equalsIgnoreCase(langDetected) ? commonVerbsFR : commonVerbsEN;

            // Hints pour ORGANIZATION
            this.orgHints = "fr".equalsIgnoreCase(langDetected) ? orgHintsFR : orgHintsEN;
        }

        /**
         * Traite une phrase JSON.
         * 
         * @param sentence : phrase JSON
         * @param entities : Map des entités nommées extraites
         */
        public void processSentence(JsonNode sentence, Map<String, List<String>> entities) {
            for (JsonNode token : sentence.path("tokens")) {
                String word = cleanWord(token.path("word").asText());
                String pos = token.path("pos").asText();
                String ner = token.path("ner").asText();

                if (isEntityOrProperNoun(ner, pos)) {
                    handleEntityToken(word, pos, ner, entities);
                } else {
                    flushEntity(entities);
                }
            }

            flushEntity(entities);
        }

        private boolean isEntityOrProperNoun(String ner, String pos) {
            return !"O".equals(ner) || pos.startsWith("NNP");
        }

        private void handleEntityToken(String word, String pos, String ner, Map<String, List<String>> entities) {
            if (shouldMerge(ner, pos)) {
                appendWord(word);
            } else {
                flushEntity(entities);
                buffer.append(word);
            }

            currentNER = !"O".equals(ner) ? ner : currentNER;
            lastNER = ner;
            lastWasNNP = pos.startsWith("NNP");
        }

        private boolean shouldMerge(String ner, String pos) {
            return ner.equals(lastNER) || (lastWasNNP && pos.startsWith("NNP"));
        }

        private void appendWord(String word) {
            if (!buffer.isEmpty())
                buffer.append(" ");

            buffer.append(word);
        }

        private static String cleanWord(String word) {
            if (word == null)
                return "";

            // Apostrophes typographiques → apostrophe simple
            word = word.replace("’", "'").replace("‘", "'");

            // Tirets spéciaux → tiret simple
            word = word.replace("\u2013", "-").replace("\u2014", "-");

            // Espaces insécables → espace normal
            word = word.replace("\u00A0", " ");

            // Supprime les caractères de contrôle invisibles sauf tab/retour ligne
            word = word.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

            return word.trim();
        }

        private void flushEntity(Map<String, List<String>> entities) {
            if (!buffer.isEmpty() && !currentNER.isEmpty()) {
                String entityWord = buffer.toString().trim();

                if (!entityWord.isEmpty() && isValidEntity(entityWord)) {
                    entities.computeIfAbsent(currentNER, k -> new ArrayList<>()).add(entityWord);
                }
            }

            buffer.setLength(0);
            lastNER = "";
            currentNER = "";
            lastWasNNP = false;
        }

        /**
         * Heuristiques par type NER
         */
        private boolean isValidEntity(String entity) {
            if (entity.isEmpty())
                return false;

            String[] tokens = entity.split("\\s+");
            if (tokens.length == 0)
                return false;

            // filtrage stopwords généraux + leading/trailing
            String first = tokens[0].toLowerCase();
            String last = tokens[tokens.length - 1].toLowerCase();

            if (stopwords.contains(tokens[0].toLowerCase())
                    || leadingTrailing.contains(first)
                    || leadingTrailing.contains(last)) {
                return false;
            }

            if (verbs.contains(first))
                return false;

            // Limite longueur entité selon type
            if (!checkCurrentNER(tokens)) {
                return false;
            }

            // Filtrage par patterns invalides
            Set<Pattern> patterns = getPatternsForCurrentNER();
            for (Pattern p : patterns) {
                if (p.matcher(entity).matches())
                    return false;
            }

            // Vérification hints pour ORGANIZATION
            return checkOrganization(entity);
        }

        private boolean checkOrganization(String entity) {
            return !(ORGANIZATION_ENTITY.equals(currentNER) &&
                    orgHints.stream().noneMatch(entity::contains) &&
                    (!Character.isUpperCase(entity.charAt(0)) || entity.split(" ").length == 1));
        }

        private boolean checkCurrentNER(String[] tokens) {
            switch (currentNER) {
                case PERSON_ENTITY:
                    return tokens.length <= 3;
                case "CITY":
                    return tokens.length <= 3 && !geoStopwords.contains(tokens[0].toLowerCase());
                case "COUNTRY", "STATE_OR_PROVINCE":
                    if (tokens.length > 1 && geoStopwords.contains(tokens[0].toLowerCase()))
                        return false;

                    return tokens.length <= 5;
                case ORGANIZATION_ENTITY:
                    return tokens.length <= 6;
                default:
                    return tokens.length <= 10;
            }
        }

        private Set<Pattern> getPatternsForCurrentNER() {
            return switch (currentNER) {
                case PERSON_ENTITY ->
                    "fr".equalsIgnoreCase(langDetected) ? personInvalidPatternsFR : personInvalidPatternsEN;
                case ORGANIZATION_ENTITY ->
                    "fr".equalsIgnoreCase(langDetected) ? orgInvalidPatternsFR : orgInvalidPatternsEN;
                default -> Collections.emptySet();
            };
        }

    }

}
