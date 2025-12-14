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

package com.fmaupin.keywords.configuration;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YamlInfoContributor
 *
 * classe permettant de récupérer les propriétés info.* dans le fichier Yaml de
 * l'application.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 14/12/25
 */
@Component
public class GenericInfoContributor implements InfoContributor {

    private final ConfigurableEnvironment env;

    public GenericInfoContributor(ConfigurableEnvironment env) {
        this.env = env;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> infoMap = new LinkedHashMap<>();

        for (PropertySource<?> propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?> eps) {
                for (String key : eps.getPropertyNames()) {
                    if (key.startsWith("info.")) {
                        String[] path = key.split("\\.");
                        Map<String, Object> current = infoMap;

                        // parcourir la hiérarchie info.*
                        for (int i = 1; i < path.length - 1; i++) {
                            current = (Map<String, Object>) current.computeIfAbsent(path[i],
                                    k -> new LinkedHashMap<>());
                        }

                        // valeur finale
                        current.put(path[path.length - 1], eps.getProperty(key));
                    }
                }
            }
        }

        builder.withDetails(infoMap);
    }
}
