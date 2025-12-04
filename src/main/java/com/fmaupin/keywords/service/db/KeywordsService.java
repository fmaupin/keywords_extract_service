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

package com.fmaupin.keywords.service.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fmaupin.keywords.exception.KeywordsProcessingException;
import com.fmaupin.keywords.model.bd.DocumentsDb;
import com.fmaupin.keywords.model.bd.KeywordsDb;
import com.fmaupin.keywords.model.message.Chunk;
import com.fmaupin.keywords.repository.KeywordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gestion des mots clés en base de données
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 23/11/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordsService {

    private final KeywordRepository keywordRepository;

    private final DocumentService documentService;

    private final FailedDocumentService failedDocumentService;

    @Transactional
    public void saveChunkKeywords(Chunk chunk, List<KeywordsDb.CategorizedKeyword> keywords) {

        UUID documentId = chunk.getDocumentId();
        int chunkNumber = chunk.getBlockNumber();
        int totalChunks = chunk.getBlockTotal();

        try {
            // Verrouiller si le chunk existe déjà
            Optional<KeywordsDb> existing = keywordRepository.findByDocumentIdAndChunkIdForUpdate(documentId,
                    chunkNumber);

            if (existing.isPresent()) {
                log.warn("Chunk {} already processed for document {}", chunkNumber, documentId);
                return;
            }

            // Mise à jour du document
            DocumentsDb document = documentService.handleChunkProcessed(documentId, totalChunks);

            // Ajouter keywords (document déjà présent)
            KeywordsDb kw = KeywordsDb.builder()
                    .document(document)
                    .chunkNumber(chunkNumber)
                    .keywords(keywords)
                    .build();

            keywordRepository.save(kw);

            log.info("Keywords saved for documentId {}", documentId);
        } catch (Exception e) {
            // Marquer le document en FAILED dans une transaction indépendante
            failedDocumentService.markDocumentAsFailed(documentId);

            throw new KeywordsProcessingException(
                    "Error saving keywords for document " + documentId +
                            " chunk " + chunkNumber,
                    e);
        }
    }
}
