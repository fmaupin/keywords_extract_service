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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fmaupin.keywords.model.message.InputMessage;

import lombok.Generated;
import lombok.extern.slf4j.Slf4j;

/**
 * LogicDisplayResultService
 * 
 * Service pour afficher les résultats de l’extraction des entités.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 21/12/25
 */
@Service
@Generated
@Slf4j
public class LogicDisplayResultService {

    public void displayResult(InputMessage message, String lang, Map<String, List<String>> entities) {
        log.info("*******************");

        log.info("Entities extracted for {} - {} :",
                message.getChunk().getDocumentId(),
                message.getChunk().getBlockNumber());

        log.info("Language detected : {}", lang);

        entities.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(),
                        entry.getValue().stream()
                                .filter(e -> e != null && !e.isEmpty())
                                .toList()))
                .filter(entry -> !entry.getValue().isEmpty())
                .forEach(entry -> log.info("entity {}: {}", entry.getKey(), entry.getValue()));

        log.info("*******************");
    }
}
