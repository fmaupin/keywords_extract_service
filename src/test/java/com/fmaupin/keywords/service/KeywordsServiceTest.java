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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.fmaupin.keywords.exception.KeywordsProcessingException;
import com.fmaupin.keywords.helper.LogCaptor;
import com.fmaupin.keywords.model.bd.DocumentsDb;
import com.fmaupin.keywords.model.bd.KeywordsDb;
import com.fmaupin.keywords.model.message.Chunk;
import com.fmaupin.keywords.repository.KeywordRepository;
import com.fmaupin.keywords.service.db.DocumentService;
import com.fmaupin.keywords.service.db.FailedDocumentService;
import com.fmaupin.keywords.service.db.KeywordsService;

/**
 * KeywordsServiceTest
 *
 * Tests pour service KeywordsService.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 17/12/25
 */
@ExtendWith(MockitoExtension.class)
class KeywordsServiceTest {

        @Mock
        private KeywordRepository keywordRepository;

        @Mock
        private DocumentService documentService;

        @Mock
        private FailedDocumentService failedDocumentService;

        @Mock
        private RabbitTemplate rabbitTemplate;

        @InjectMocks
        private KeywordsService keywordsService;

        private Chunk chunk;
        private List<KeywordsDb.CategorizedKeyword> keywords;

        @RegisterExtension
        LogCaptor logCaptor = new LogCaptor(KeywordsService.class);

        @BeforeEach
        void setUp() {
                chunk = buildChunk();

                keywords = buildKeywords();

                ReflectionTestUtils.setField(keywordsService, "exchange", "test-exchange");
                ReflectionTestUtils.setField(keywordsService, "routingkey", "test-routing");
        }

        @Test
        void testShouldSaveKeywordsWhenChunkNotProcessed() {
                DocumentsDb document = new DocumentsDb();

                when(keywordRepository.findByDocumentIdAndChunkIdForUpdate(
                                chunk.getDocumentId(),
                                chunk.getBlockNumber())).thenReturn(Optional.empty());

                when(documentService.handleChunkProcessed(
                                chunk.getDocumentId(),
                                chunk.getBlockTotal())).thenReturn(document);

                keywordsService.saveChunkKeywords(chunk, keywords);

                verify(keywordRepository).save(argThat(kw -> kw.getDocument() == document &&
                                kw.getChunkNumber() == chunk.getBlockNumber() &&
                                kw.getKeywords().equals(keywords)));

                verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("test-routing"),
                                any(UUID.class));

                verify(failedDocumentService, never()).markDocumentAsFailed(any());

                assertThat(logCaptor.getLogs())
                                .anyMatch(this::containsKeywordsSavedMessage);
        }

        @Test
        void testShouldNotSaveKeywordsWhenChunkAlreadyProcessed() {
                when(keywordRepository.findByDocumentIdAndChunkIdForUpdate(
                                chunk.getDocumentId(),
                                chunk.getBlockNumber())).thenReturn(Optional.of(new KeywordsDb()));

                keywordsService.saveChunkKeywords(chunk, buildKeywords());

                verify(keywordRepository, never()).save(any());
                verify(documentService, never()).handleChunkProcessed(any(), anyInt());
                verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(UUID.class));

                verify(failedDocumentService, never()).markDocumentAsFailed(any());

                assertThat(logCaptor.getLogs())
                                .anyMatch(this::containsAlreadyProcessedMessage);
        }

        @Test
        void testShouldMarkDocumentFailedWhenExceptionOccurs() {
                when(keywordRepository.findByDocumentIdAndChunkIdForUpdate(
                                any(), anyInt())).thenThrow(new RuntimeException("DB down"));

                KeywordsProcessingException ex = assertThrows(
                                KeywordsProcessingException.class,
                                () -> keywordsService.saveChunkKeywords(chunk, keywords));

                assertTrue(ex.getMessage().contains(chunk.getDocumentId().toString()));

                verify(failedDocumentService)
                                .markDocumentAsFailed(chunk.getDocumentId());
        }

        private boolean containsKeywordsSavedMessage(String log) {
                return log.contains("Keywords saved for documentId");
        }

        private boolean containsAlreadyProcessedMessage(String log) {
                return log.contains("already processed");
        }

        private Chunk buildChunk() {
                return Chunk.builder()
                                .documentId(UUID.randomUUID())
                                .block("mon premier bloc de texte")
                                .blockNumber(3)
                                .blockTotal(3)
                                .pathFile("/tmp/file.txt")
                                .date(LocalDateTime.now())
                                .build();
        }

        private List<KeywordsDb.CategorizedKeyword> buildKeywords() {
                return List.of(
                                new KeywordsDb.CategorizedKeyword("TECH", "java"),
                                new KeywordsDb.CategorizedKeyword("FRAMEWORK", "spring"));
        }
}
