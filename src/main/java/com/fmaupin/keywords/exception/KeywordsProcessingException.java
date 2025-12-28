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

package com.fmaupin.keywords.exception;

/**
 * ChunkProcessingException
 *
 * Exception pour erreurs générées lors des mises à jour dans la base de données
 * de la table `keywords`.
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 24/11/25
 */
public class KeywordsProcessingException extends RuntimeException {
    public KeywordsProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
