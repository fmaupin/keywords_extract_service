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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fmaupin.keywords.enumeration.DocumentStatusEnum;
import com.fmaupin.keywords.helper.LogCaptor;
import com.fmaupin.keywords.model.bd.DocumentsDb;
import com.fmaupin.keywords.repository.DocumentRepository;
import com.fmaupin.keywords.service.db.FailedDocumentService;

/**
 * FailedDocumentServiceTest
 *
 * Tests pour service FailedDocumentService.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 19/12/25
 */
@ExtendWith(MockitoExtension.class)
class FailedDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private FailedDocumentService failedDocumentService;

    @RegisterExtension
    LogCaptor logCaptor = new LogCaptor(FailedDocumentService.class);

    private UUID documentId = UUID.randomUUID();

    @Test
    void testShouldMarkDocumentAsFailedWhenDocumentExists() {
        DocumentsDb document = builDocument();

        when(documentRepository.findById(
                documentId)).thenReturn(Optional.of(document));

        failedDocumentService.markDocumentAsFailed(documentId);

        assertEquals(DocumentStatusEnum.FAILED, document.getDocumentStatus());

        verify(documentRepository).findById(documentId);
        verify(documentRepository).save(document);
    }

    @Test
    void testDoNothingWhenDocumentDoNotExist() {
        when(documentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        failedDocumentService.markDocumentAsFailed(documentId);

        verify(documentRepository).findById(documentId);
        verify(documentRepository, never()).save(any());
    }

    private DocumentsDb builDocument() {
        return DocumentsDb.builder()
                .documentId(documentId)
                .totalChunks(5)
                .processedChunks(1)
                .build();
    }

}
