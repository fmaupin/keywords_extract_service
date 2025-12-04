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

package com.fmaupin.keywords.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fmaupin.keywords.model.message.Chunk;
import com.fmaupin.keywords.model.message.InputMessage;
import com.fmaupin.keywords.service.ResultService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * MessageListener
 *
 * Écoute la queue et traite les messages entrants.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Component
@Slf4j
public class MessageListener {

    private final ResultService resultService;

    public MessageListener(ResultService resultService) {
        this.resultService = resultService;
    }

    @RabbitListener(queues = "${keywords-poc.rabbitmq.in.consumerQueueName}", ackMode = "MANUAL")
    public void onMessage(Chunk chunk, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        InputMessage inputMessage = InputMessage.of(chunk);

        log.info("Received chunk: {} - {}", inputMessage.getChunk().getDocumentId(),
                inputMessage.getChunk().getBlockNumber());

        try {
            resultService.process(inputMessage).join(); // traitement métier

            // traitement OK → ACK
            channel.basicAck(tag, false);

            log.info("Chunk processed successfully : {} - {}", inputMessage.getChunk().getDocumentId(),
                    inputMessage.getChunk().getBlockNumber());

        } catch (Exception e) {
            log.error("Error processing chunk: {} - {}", inputMessage.getChunk().getDocumentId(),
                    inputMessage.getChunk().getBlockNumber(), e);

            // traitement échoué → envoie du message vers la DLQ
            channel.basicNack(tag, false, false);
        }
    }
}
