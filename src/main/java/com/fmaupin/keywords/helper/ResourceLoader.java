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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ResourceLoader
 *
 * Chargement des fichiers resources.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 10/11/25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceLoader {

    public static Set<Pattern> loadPatterns(String resourcePath) throws IOException {
        InputStream is = ResourceLoader.class.getResourceAsStream(resourcePath);

        if (is == null)
            return Collections.emptySet();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    // regex safe: matcher insensible à la casse et aux accents optionnels
                    .map(s -> Pattern.compile("\\b" + Pattern.quote(s) + "\\b",
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                    .collect(Collectors.toSet());
        }
    }

    public static Set<String> loadLines(String resourcePath) throws IOException {
        InputStream is = ResourceLoader.class.getResourceAsStream(resourcePath);

        if (is == null)
            return Collections.emptySet();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                    .collect(Collectors.toSet());
        }
    }

    public static Map<Pattern, String> loadMappings(String path) throws IOException {
        try (InputStream is = ResourceLoader.class.getResourceAsStream(path)) {
            if (is == null)
                return Collections.emptyMap();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                Map<Pattern, String> map = new LinkedHashMap<>();

                br.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=>");
                            if (parts.length == 2) {
                                map.put(Pattern.compile(parts[0].trim()), parts[1].trim());
                            }
                        });

                return map;
            }
        }
    }
}
