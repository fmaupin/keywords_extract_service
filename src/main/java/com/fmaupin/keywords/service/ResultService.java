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

package com.fmaupin.keywords.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;

import com.fmaupin.keywords.enumeration.StatusEnum;
import com.fmaupin.keywords.model.message.InputMessage;
import com.fmaupin.keywords.model.message.ResultProcessMessage;
import com.fmaupin.keywords.service.logic.LogicService;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * ResultService
 *
 * Service pour gestion des résultats issus traitement des messages
 * entrants
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Service
@Slf4j
public class ResultService {

    private final LogicService logicService;

    // gestionnaire d'exécution des tâches asynchrones
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    // suivi du traitement pour monitoring éventuel (liste thread-safe)
    private final List<ResultProcessMessage> resultList = new CopyOnWriteArrayList<>();

    public ResultService(LogicService logicService) {
        this.logicService = logicService;
    }

    /**
     * Traite un message RabbitMQ (asynchrone)
     */
    public void process(InputMessage input) {
        resultList.add(ResultProcessMessage.builder()
                .msg(input)
                .status(StatusEnum.PENDING)
                .build());

        CompletableFuture
                .supplyAsync(() -> logicService.run(input), executorService)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        handleError(input, ex);
                    } else {
                        markComplete(input, result);
                    }
                });
    }

    private void markComplete(InputMessage input, InputMessage result) {
        resultList.stream()
                .filter(r -> r.getMsg().equals(input))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatus(StatusEnum.COMPLETE);
                    r.setResult(result.getChunk());
                    r.setProcessDate(LocalDateTime.now());

                    log.info("Message processed successfully: {}", input);
                });
    }

    private void handleError(InputMessage input, Throwable ex) {
        resultList.stream()
                .filter(r -> r.getMsg().equals(input))
                .findFirst()
                .ifPresent(r -> r.setStatus(StatusEnum.FAILED));

        log.error("Error processing message {}: {}", input, ex.getMessage(), ex);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
        log.info("ExecutorService shutdown");
    }
}
