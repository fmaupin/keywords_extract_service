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

package com.fmaupin.keywords.service.logic;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.fmaupin.keywords.model.message.InputMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour implémentation métier du micro-service
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Service
@Slf4j
public class LogicService implements Logic {

    private Random random = new Random();

    @Override
    public InputMessage run(InputMessage message) {
        long lowerLimit = 1000L;
        long upperLimit = 10000L;

        long processingTime = random.nextLong(lowerLimit, upperLimit);

        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Thread {} - processing message {} -> processing time {}", Thread.currentThread().getName(),
                message.getChunk().getId(),
                processingTime);

        return message;
    }

}
