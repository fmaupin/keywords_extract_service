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

package com.fmaupin.keywords.service.logic;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fmaupin.keywords.exception.CoreNLPServerException;
import com.fmaupin.keywords.helper.CoreNLPHelper;
import com.fmaupin.keywords.helper.KeywordsTransformer;
import com.fmaupin.keywords.model.bd.KeywordsDb;
import com.fmaupin.keywords.model.message.InputMessage;
import com.fmaupin.keywords.service.db.KeywordsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour extraction des mots clés
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogicService implements Logic {

    @Value("${coreNLP.url-base}")
    private String coreNLPUrlBase;

    @Value("${spring.main.language-default}")
    private String languageDefault;

    private final RestTemplate restTemplate;

    private final KeywordsService keywordsService;

    private final LogicDisplayResultService displayResultService;

    @Override
    public InputMessage run(InputMessage message) {
        try {
            Instant start = Instant.now();

            String text = message.getChunk().getBlock();

            // Détection automatique de la langue
            String lang = detectLanguage(text);

            // Génération dynamique de l'URL CoreNLP avec tokenize.language
            String coreNLPUrl = buildCoreNLPUrl(lang);

            // Appel au serveur CoreNLP pour extraire les entités (UTF-8 & synchrone)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));

            HttpEntity<String> request = new HttpEntity<>(text, headers);

            String jsonResponse = restTemplate.postForObject(coreNLPUrl,
                    request, String.class);

            Map<String, List<String>> entities = CoreNLPHelper.extractEntities(jsonResponse, lang);

            // Affichage des entités extraites (logs)
            displayResultService.displayResult(message, lang, entities);

            // Stocker les mots clés en base de données
            List<KeywordsDb.CategorizedKeyword> keywords = KeywordsTransformer.normalizeKeywords(entities);

            keywordsService.saveChunkKeywords(message.getChunk(), keywords);

            Instant end = Instant.now();

            log.info("Thread {} - processing message [{} - {}] -> processing time {} ms",
                    Thread.currentThread().getName(),
                    message.getChunk().getDocumentId(),
                    message.getChunk().getBlockNumber(),
                    ChronoUnit.MILLIS.between(start, end));

            return message;
        } catch (CoreNLPServerException e) {
            log.error("Error during keywords extraction or saving", e);
            return message;
        }
    }

    /**
     * Détecte automatiquement la langue du texte
     */
    private String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Text is empty or null, defaulting language to locale");
            return languageDefault;
        }

        // Nettoyage du texte : suppression des espaces multiples et caractères
        // invisibles
        text = text.replaceAll("\\s+", " ").trim();

        // Trop court pour détecter => langage par défaut
        if (text.length() < 30) {
            log.warn("Text is too short, defaulting language to locale");
            return languageDefault;
        }

        try {
            LanguageDetector detector = new OptimaizeLangDetector().loadModels();
            LanguageResult result = detector.detect(text);

            if (result.isReasonablyCertain()) {
                return result.getLanguage();
            } else {
                log.warn("Language not reasonably certain, defaulting to locale");
                return languageDefault;
            }
        } catch (Exception e) {
            log.error("Language detection failed, defaulting to locale", e);
            return languageDefault;
        }
    }

    /**
     * Construit dynamiquement l'URL CoreNLP avec la langue détectée
     */
    private String buildCoreNLPUrl(String lang) {
        String propertiesJson = String.format(
                "{\"annotators\":\"tokenize,ssplit,pos,lemma,ner\",\"outputFormat\":\"json\",\"tokenize.language\":\"%s\"}",
                lang);

        return coreNLPUrlBase + "?properties=" + URLEncoder.encode(propertiesJson, StandardCharsets.UTF_8);
    }

}
