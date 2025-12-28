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

package com.fmaupin.keywords.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fmaupin.keywords.exception.CoreNLPServerException;
import com.fmaupin.keywords.helper.LogCaptor;
import com.fmaupin.keywords.model.message.Chunk;
import com.fmaupin.keywords.model.message.InputMessage;
import com.fmaupin.keywords.service.db.KeywordsService;
import com.fmaupin.keywords.service.logic.LogicDisplayResultService;
import com.fmaupin.keywords.service.logic.LogicService;

/**
 * LogicServiceTest
 *
 * Tests pour service LogicService.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 19/12/25
 */
@ExtendWith(MockitoExtension.class)
class LogicServiceTest {

        @Mock
        private RestTemplate restTemplate;

        @Mock
        private KeywordsService keywordsService;

        @Mock
        private LogicDisplayResultService displayResultService;

        @InjectMocks
        private LogicService logicService;

        @RegisterExtension
        LogCaptor logCaptor = new LogCaptor(LogicService.class);

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(logicService, "coreNLPUrlBase", "http://fake-nlp");
                ReflectionTestUtils.setField(logicService, "languageDefault", "fr");
        }

        @Test
        void testShoulProcessMessageSuccessfully() {
                Chunk chunk = Chunk.builder()
                                .documentId(UUID.randomUUID())
                                .blockNumber(1)
                                .block("This is a sufficiently long English text to detect language properly.")
                                .build();

                InputMessage input = InputMessage.of(chunk);

                String fakeJson = "{ \"sentences\": [] }";

                when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                                .thenReturn(fakeJson);

                InputMessage result = logicService.run(input);

                assertSame(input, result);

                verify(restTemplate).postForObject(anyString(), any(), eq(String.class));
                verify(keywordsService).saveChunkKeywords(eq(chunk), anyList());
                verify(displayResultService).displayResult(any(), any(), any());

                assertThat(logCaptor.getLogs())
                                .anyMatch(log -> log.contains("processing message"));
        }

        @Test
        void testShouldDefaultLanguageWhenTextIsEmpty() {
                Chunk chunk = Chunk.builder()
                                .documentId(UUID.randomUUID())
                                .blockNumber(2)
                                .block("   ")
                                .build();

                InputMessage input = InputMessage.of(chunk);

                when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                                .thenReturn("{ }");

                logicService.run(input);

                assertThat(logCaptor.getLogs())
                                .anyMatch(log -> log.contains("Text is empty or null"));

                verify(keywordsService).saveChunkKeywords(any(), any());
        }

        @Test
        void testShouldDefaultLanguageWhenTextIsTooShort() {
                Chunk chunk = Chunk.builder()
                                .documentId(UUID.randomUUID())
                                .blockNumber(3)
                                .block("Too short text")
                                .build();

                InputMessage input = InputMessage.of(chunk);

                when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                                .thenReturn("{ }");

                logicService.run(input);

                assertThat(logCaptor.getLogs())
                                .anyMatch(log -> log.contains("Text is too short"));
        }

        @Test
        void testShouldReturnMessageWhenCorenlpExceptionOccurs() {
                Chunk chunk = Chunk.builder()
                                .documentId(UUID.randomUUID())
                                .blockNumber(4)
                                .block("This text is long enough to trigger processing.")
                                .build();

                InputMessage input = InputMessage.of(chunk);

                when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                                .thenThrow(new CoreNLPServerException("NLP down"));

                InputMessage result = logicService.run(input);

                assertSame(input, result);

                verify(keywordsService, never()).saveChunkKeywords(any(), any());

                assertThat(logCaptor.getLogs())
                                .anyMatch(log -> log.contains("Error during keywords extraction or saving"));
        }

}
