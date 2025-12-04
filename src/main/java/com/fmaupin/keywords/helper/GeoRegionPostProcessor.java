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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * GeoRegionPostProcessor
 *
 * Post-processeur des régions géographiques dans texte traité par CoreNLP
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 10/11/25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoRegionPostProcessor {

    private static final Map<String, Set<Pattern>> REGION_PATTERNS = new HashMap<>();

    static {
        try {
            REGION_PATTERNS.put("fr", ResourceLoader.loadPatterns("/entity_rules/fr/regions.txt"));
            REGION_PATTERNS.put("en", ResourceLoader.loadPatterns("/entity_rules/en/regions.txt"));
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading GeoRegionPostProcessor resources", e);
        }
    }

    /**
     * Ajoute les entités géographiques régionales manquantes au set existant.
     *
     * @param text              le texte complet
     * @param existingLocations entités LOCATION existantes
     * @param langDetected      "fr" ou "en"
     * @return ensemble enrichi d’entités LOCATION
     */
    public static Set<String> enrichWithRegions(String text, Set<String> existingLocations, String langDetected) {
        String lang = (langDetected != null && langDetected.toLowerCase().startsWith("fr")) ? "fr" : "en";
        Set<Pattern> patterns = REGION_PATTERNS.getOrDefault(lang, Collections.emptySet());

        Set<String> enriched = new LinkedHashSet<>(existingLocations);

        for (Pattern pattern : patterns) {
            var matcher = pattern.matcher(text);

            while (matcher.find()) {
                String region = matcher.group().trim();
                if (!enriched.contains(region)) {
                    enriched.add(region);
                }
            }
        }

        return enriched;
    }
}
