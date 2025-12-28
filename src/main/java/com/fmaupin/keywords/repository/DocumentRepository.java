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

package com.fmaupin.keywords.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.fmaupin.keywords.model.bd.DocumentsDb;

/**
 * DocumentRepository
 *
 * REPOSITORY pour la gestion des documents en base de données
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 23/11/25
 */
public interface DocumentRepository extends JpaRepository<DocumentsDb, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO documents (document_id, total_chunks, processed_chunks, document_status)
            VALUES (:documentId, :totalChunks, 1, 'PROCESSING')
            ON CONFLICT (document_id)
            DO UPDATE SET
                processed_chunks = documents.processed_chunks + 1,
                document_status = CASE
                    WHEN documents.processed_chunks + 1 >= documents.total_chunks
                    THEN 'COMPLETED'
                    ELSE documents.document_status
                END
            """, nativeQuery = true)
    int upsertAndIncrement(@Param("documentId") UUID documentId,
            @Param("totalChunks") int totalChunks);

}
