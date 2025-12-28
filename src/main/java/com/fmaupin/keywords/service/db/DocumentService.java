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

package com.fmaupin.keywords.service.db;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fmaupin.keywords.exception.DocumentUpdateException;
import com.fmaupin.keywords.model.bd.DocumentsDb;
import com.fmaupin.keywords.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gestion des documents en base de données
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 23/11/25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentsDb handleChunkProcessed(UUID documentId, int totalChunks) {
        try {
            documentRepository.upsertAndIncrement(documentId, totalChunks);

            DocumentsDb document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalStateException("Document not found after upsert"));

            log.info("Chunk processed for documentId {}: {}/{}",
                    documentId,
                    document.getProcessedChunks(),
                    document.getTotalChunks());

            return document;

        } catch (Exception e) {
            throw new DocumentUpdateException(
                    "Error during atomic upsert for document " + documentId, e);
        }
    }

}
