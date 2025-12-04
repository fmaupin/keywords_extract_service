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

package com.fmaupin.keywords.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fmaupin.keywords.model.bd.KeywordsDb;

import jakarta.persistence.LockModeType;

/**
 * KeywordRepository
 *
 * REPOSITORY pour la gestion des mots clés en base de données
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 23/11/25
 */
public interface KeywordRepository extends JpaRepository<KeywordsDb, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT k FROM KeywordsDb k
                WHERE k.document.documentId = :documentId
                AND k.chunkNumber = :chunkNumber
            """)
    Optional<KeywordsDb> findByDocumentIdAndChunkIdForUpdate(
            @Param("documentId") UUID documentId,
            @Param("chunkNumber") int chunkNumber);

}
