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

package com.fmaupin.keywords.service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

    public ResultService(LogicService logicService) {
        this.logicService = logicService;
    }

    /**
     * Traite un message RabbitMQ (asynchrone)
     */
    public CompletableFuture<ResultProcessMessage> process(InputMessage input) {
        ResultProcessMessage tracker = ResultProcessMessage.builder()
                .msg(input)
                .status(StatusEnum.PENDING)
                .build();

        return CompletableFuture
                .supplyAsync(() -> logicService.run(input), executorService)
                .thenApply(result -> {
                    markComplete(tracker, result);
                    return tracker;
                })
                .exceptionally(ex -> {
                    handleError(tracker, ex);
                    throw new CompletionException(ex);
                });
    }

    private void markComplete(ResultProcessMessage tracker, InputMessage result) {
        tracker.setStatus(StatusEnum.COMPLETE);
        tracker.setResult(result.getChunk());
        tracker.setProcessDate(LocalDateTime.now());

        log.info("Message processed successfully: {} - {}",
                result.getChunk().getDocumentId(),
                result.getChunk().getBlockNumber());
    }

    private void handleError(ResultProcessMessage tracker, Throwable ex) {
        tracker.setStatus(StatusEnum.FAILED);
        InputMessage input = tracker.getMsg();

        log.error("Error processing message {} for {}: {}",
                ex.getMessage(),
                input.getChunk().getDocumentId(),
                input.getChunk().getBlockNumber(),
                ex);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
        log.info("ExecutorService shutdown");
    }
}
