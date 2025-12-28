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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fmaupin.keywords.exception.DocumentUpdateException;
import com.fmaupin.keywords.helper.LogCaptor;
import com.fmaupin.keywords.model.bd.DocumentsDb;
import com.fmaupin.keywords.repository.DocumentRepository;
import com.fmaupin.keywords.service.db.DocumentService;

/**
 * DocumentServiceTest
 *
 * Tests pour service DocumentService.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 17/12/25
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

        @Mock
        private DocumentRepository documentRepository;

        @InjectMocks
        private DocumentService documentService;

        @RegisterExtension
        LogCaptor logCaptor = new LogCaptor(DocumentService.class);

        @Captor
        ArgumentCaptor<UUID> uuidCaptor;

        @Captor
        ArgumentCaptor<Integer> totalChunksCaptor;

        private UUID documentId = UUID.randomUUID();

        @Test
        void testHandleChunkProcessedWhenDocumentFound() {
                DocumentsDb document = builDocument();

                when(documentRepository.upsertAndIncrement(
                                documentId, 5)).thenReturn(1);

                when(documentRepository.findById(
                                documentId)).thenReturn(Optional.of(document));

                DocumentsDb docResp = documentService.handleChunkProcessed(
                                documentId, 5);

                assertNotNull(docResp);
                assertEquals(docResp.getDocumentId(), document.getDocumentId());
                assertEquals(docResp.getDocumentStatus(), document.getDocumentStatus());
                assertEquals(docResp.getTotalChunks(), document.getTotalChunks());
                assertEquals(docResp.getProcessedChunks(), document.getProcessedChunks());

                verify(documentRepository).upsertAndIncrement(
                                uuidCaptor.capture(),
                                totalChunksCaptor.capture());

                assertEquals(document.getDocumentId(), uuidCaptor.getValue());
                assertEquals(document.getTotalChunks(), totalChunksCaptor.getValue());

                verify(documentRepository).findById(documentId);

                assertThat(logCaptor.getLogs())
                                .anyMatch(this::containsChunkProcessedSavedMessage);
        }

        @Test
        void testHandleChunkProcessedWhenDocumentNotFound() {
                when(documentRepository.upsertAndIncrement(
                                documentId, 5)).thenReturn(1);

                when(documentRepository.findById(
                                any()))
                                .thenThrow(new IllegalStateException("Document not found after upsert"));

                DocumentUpdateException ex = assertThrows(
                                DocumentUpdateException.class,
                                () -> documentService.handleChunkProcessed(documentId, 5));

                assertTrue(ex.getMessage().contains("Error during atomic upsert for document " + documentId));

        }

        @Test
        void testHandleChunkProcessedWhenUpsertFails() {
                when(documentRepository.upsertAndIncrement(documentId, 5))
                                .thenThrow(new RuntimeException("DB error"));

                DocumentUpdateException ex = assertThrows(
                                DocumentUpdateException.class,
                                () -> documentService.handleChunkProcessed(documentId, 5));

                assertTrue(ex.getMessage()
                                .contains("Error during atomic upsert for document " + documentId));

                verify(documentRepository).upsertAndIncrement(documentId, 5);
                verify(documentRepository, never()).findById(any());
        }

        private DocumentsDb builDocument() {
                return DocumentsDb.builder()
                                .documentId(documentId)
                                .totalChunks(5)
                                .processedChunks(1)
                                .build();
        }

        private boolean containsChunkProcessedSavedMessage(String log) {
                return log.contains("Chunk processed for documentId");
        }
}
