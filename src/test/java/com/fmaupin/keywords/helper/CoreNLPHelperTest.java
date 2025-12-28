/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Extract Micro Service.
 *
 * Extract Micro Service is free software: you can redistribute it and/or modify
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fmaupin.keywords.exception.CoreNLPServerException;

/**
 * CoreNLPHelperTest
 *
 * Tests pour helper CoreNLPHelper.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 22/12/25
 */
class CoreNLPHelperTest {

  private static final String LANG_FR = "fr";

  private String jsonWithTextAndLocation() {
    return """
        {
          "text": "Paris est une belle ville",
          "sentences": [
            {
              "tokens": [
                { "word": "Paris", "pos": "NNP", "ner": "LOCATION" },
                { "word": "est", "pos": "VB", "ner": "O" }
              ]
            }
          ]
        }
        """;
  }

  private String jsonWithoutTextWithPersonAndLocation() {
    return """
        {
          "sentences": [
            {
              "tokens": [
                { "word": "Paris", "pos": "NNP", "ner": "PERSON" },
                { "word": "Paris", "pos": "NNP", "ner": "LOCATION" }
              ]
            }
          ]
        }
        """;
  }

  private String jsonInvalid() {
    return "{ invalid json ";
  }

  @Test
  void tesShouldExtractLocationWhenTextPresent() {
    Map<String, List<String>> result = CoreNLPHelper.extractEntities(jsonWithTextAndLocation(), LANG_FR);

    assertNotNull(result);
    assertTrue(result.containsKey("LOCATION"));
    assertEquals(List.of("Paris"), result.get("LOCATION"));
  }

  @Test
  void testShouldReconstructTextWhenTextFieldIsMissing() {
    Map<String, List<String>> result = CoreNLPHelper.extractEntities(jsonWithoutTextWithPersonAndLocation(),
        LANG_FR);

    assertNotNull(result);
    assertTrue(result.containsKey("LOCATION"));
  }

  @Test
  void testShouldRemoveLocationFromPersonEntities() {
    Map<String, List<String>> result = CoreNLPHelper.extractEntities(jsonWithoutTextWithPersonAndLocation(),
        LANG_FR);

    assertTrue(result.containsKey("LOCATION"));
    assertEquals(String.join(" ", List.of("Paris", "Paris")), String.join("", result.get("LOCATION")));

    assertTrue(result.containsKey("PERSON"));
    assertTrue(result.get("PERSON").isEmpty());
  }

  @Test
  void testShouldNotFailWhenNoLocationPresent() {
    String json = """
        {
          "sentences": [
            {
              "tokens": [
                { "word": "Jean", "pos": "NNP", "ner": "PERSON" }
              ]
            }
          ]
        }
        """;

    Map<String, List<String>> result = CoreNLPHelper.extractEntities(json, LANG_FR);

    assertTrue(result.containsKey("PERSON"));
    assertEquals(List.of("Jean"), result.get("PERSON"));
  }

  @Test
  void testShouldThrowExceptionOnInvalidJson() {
    CoreNLPServerException ex = assertThrows(
        CoreNLPServerException.class,
        this::callExtractEntitiesWithInvalidJson);

    assertTrue(ex.getMessage().contains("Error parsing CoreNLP JSON response"));
  }

  private void callExtractEntitiesWithInvalidJson() {
    CoreNLPHelper.extractEntities(jsonInvalid(), LANG_FR);
  }
}
