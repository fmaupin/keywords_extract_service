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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fmaupin.keywords.enumeration.StatusEnum;
import com.fmaupin.keywords.helper.LogCaptor;
import com.fmaupin.keywords.model.message.Chunk;
import com.fmaupin.keywords.model.message.InputMessage;
import com.fmaupin.keywords.model.message.ResultProcessMessage;
import com.fmaupin.keywords.service.logic.LogicService;

/**
 * ResultServiceTest
 *
 * Tests pour service ResultService.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 19/12/25
 */
@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private LogicService logicService;

    @InjectMocks
    private ResultService resultService;

    @RegisterExtension
    LogCaptor logCaptor = new LogCaptor(ResultService.class);

    @Test
    void testShouldCompleteSuccessfullyWhenLogicServiceSucceeds() {
        Chunk chunk = buildChunk();

        InputMessage input = buildInputMessage(chunk);

        when(logicService.run(input)).thenReturn(input);

        ResultProcessMessage result = resultService.process(input).join();

        assertEquals(StatusEnum.COMPLETE, result.getStatus());
        assertEquals(chunk, result.getResult());
        assertNotNull(result.getProcessDate());

        verify(logicService).run(input);

        assertThat(logCaptor.getLogs())
                .anyMatch(log -> log.contains("Message processed successfully"));
    }

    @Test
    void testShoulFailWhenLogicServiceThrowsException() {
        Chunk chunk = buildChunk();

        InputMessage input = buildInputMessage(chunk);

        RuntimeException failure = new RuntimeException("failure");

        when(logicService.run(input)).thenThrow(failure);

        CompletionException ex = assertThrows(
                CompletionException.class,
                () -> this.processInputMessage(input));

        assertEquals(failure, ex.getCause().getCause());

        assertThat(logCaptor.getLogs())
                .anyMatch(log -> log.contains("Error processing message"));
    }

    @Test
    void testShouldShutdownExecutorService() {
        resultService.shutdown();

        assertThat(logCaptor.getLogs())
                .anyMatch(log -> log.contains("ExecutorService shutdown"));
    }

    private Chunk buildChunk() {
        return Chunk.builder()
                .documentId(UUID.randomUUID())
                .blockNumber(1)
                .build();
    }

    private InputMessage buildInputMessage(Chunk chunk) {
        return InputMessage.builder()
                .chunk(chunk)
                .build();
    }

    private ResultProcessMessage processInputMessage(InputMessage input) {
        return resultService.process(input).join();
    }

}
