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

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fmaupin.keywords.enumeration.DocumentStatusEnum;
import com.fmaupin.keywords.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gestion des erreurs lors de la gestion des documents en
 * base de données
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 24/11/25
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FailedDocumentService {

    private final DocumentRepository documentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Garantie que l'état FAILED sera prise en
                                                           // compte même si la transaction principale est rollbackée
    public void markDocumentAsFailed(UUID documentId) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setDocumentStatus(DocumentStatusEnum.FAILED);
            documentRepository.save(doc);
        });
    }
}
