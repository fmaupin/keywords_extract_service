/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Read Content Micro Service.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fmaupin.keywords.model.bd.KeywordsDb;

/**
 * KeywordsTransformerTest
 *
 * Tests pour helper KeywordsTransformer.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 22/12/25
 */
class KeywordsTransformerTest {

    @Test
    void testShouldIgnoreNullKeywords() {
        Map<String, List<String>> entities = Map.of(
                "PERSON", Collections.singletonList(null));

        List<KeywordsDb.CategorizedKeyword> result = KeywordsTransformer.normalizeKeywords(entities);

        assertTrue(result.isEmpty());
    }

    @Test
    void testShouldIgnoreBlankKeywords() {
        Map<String, List<String>> entities = Map.of(
                "LOCATION", List.of("   "));

        List<KeywordsDb.CategorizedKeyword> result = KeywordsTransformer.normalizeKeywords(entities);

        assertTrue(result.isEmpty());
    }

    @Test
    void testShouldNormalizeAccentsAndCapitalize() {
        Map<String, List<String>> entities = Map.of(
                "LOCATION", List.of("  élÉphant  "));

        List<KeywordsDb.CategorizedKeyword> result = KeywordsTransformer.normalizeKeywords(entities);

        assertEquals(1, result.size());

        KeywordsDb.CategorizedKeyword kw = result.get(0);
        assertEquals("LOCATION", kw.getCategory());
        assertEquals("Elephant", kw.getKeyword());
    }

    @Test
    void testShouldDeduplicateAndSortKeywords() {
        Map<String, List<String>> entities = Map.of(
                "LOCATION", List.of("Paris", "paris", "Páris"));

        List<KeywordsDb.CategorizedKeyword> result = KeywordsTransformer.normalizeKeywords(entities);

        assertEquals(1, result.size());
        assertEquals("Paris", result.get(0).getKeyword());
    }

}
