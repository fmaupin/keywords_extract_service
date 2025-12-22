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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * GeoRegionPostProcessorTest
 *
 * Tests pour helper GeoRegionPostProcessor.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 22/12/25
 */
class GeoRegionPostProcessorTest {

    @Test
    void testShouldEnrichWithRegionWhenRegionFoundInText() {
        String text = "Je vis en Europe de l'Ouest depuis 10 ans";

        Set<String> existing = new HashSet<>();

        Set<String> result = GeoRegionPostProcessor.enrichWithRegions(
                text,
                existing,
                "fr");

        assertTrue(result.contains("Europe de l'Ouest"));
    }

    @Test
    void testShouldNotDuplicateExistingRegion() {
        String text = "Île-de-France est une région très peuplée";

        Set<String> existing = new LinkedHashSet<>();
        existing.add("Île-de-France");

        Set<String> result = GeoRegionPostProcessor.enrichWithRegions(
                text,
                existing,
                "fr");

        assertEquals(1, result.size());
        assertTrue(result.contains("Île-de-France"));
    }

    @Test
    void testShouldUseEnglishPatternsWhenLanguageIsNotFrench() {
        String text = "I live in Middle East";

        Set<String> result = GeoRegionPostProcessor.enrichWithRegions(
                text,
                Set.of(),
                "en");

        assertFalse(result.isEmpty());
    }

    @Test
    void testShouldReturnSameSetWhenNoRegionMatches() {
        String text = "This text has no region";

        Set<String> existing = new HashSet<>();
        existing.add("Paris");

        Set<String> result = GeoRegionPostProcessor.enrichWithRegions(
                text,
                existing,
                "fr");

        assertEquals(existing, result);
    }

}
