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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.fmaupin.keywords.model.bd.KeywordsDb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * KeywordsTransformer
 *
 * Transformation et normalisation des mots clés extraits.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 25/11/25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KeywordsTransformer {

    public static List<KeywordsDb.CategorizedKeyword> normalizeKeywords(Map<String, List<String>> entities) {

        return entities.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(String::trim)
                        .map(KeywordsTransformer::normalizeString)
                        .filter(keyword -> keyword.length() > 2)
                        .distinct()
                        .map(keyword -> new KeywordsDb.CategorizedKeyword(entry.getKey(), keyword)))
                .sorted(Comparator.comparing(KeywordsDb.CategorizedKeyword::getCategory)
                        .thenComparing(KeywordsDb.CategorizedKeyword::getKeyword))
                .toList();
    }

    private static String normalizeString(String input) {
        if (input == null)
            return "";

        // suppression des accents / normalisation Unicode
        String noAccents = java.text.Normalizer
                .normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // minuscule
        noAccents = noAccents.toLowerCase().trim();

        // capitalisation
        if (noAccents.isEmpty())
            return "";

        return Character.toUpperCase(noAccents.charAt(0)) + noAccents.substring(1);
    }
}
