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

package com.fmaupin.keywords.model.message;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Chunk
 *
 * MODEL -> contenu message -> `chunk`
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Getter
@ToString
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Chunk implements Comparable<Chunk> {

    private UUID documentId;

    private String block;

    private int blockNumber;

    private int blockTotal;

    private String pathFile;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    protected LocalDateTime date;

    @Override
    @Generated
    public int compareTo(Chunk other) {
        int cmp = this.documentId.compareTo(other.documentId);
        return (cmp != 0) ? cmp : Integer.compare(this.blockNumber, other.blockNumber);
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Chunk))
            return false;
        Chunk other = (Chunk) o;

        return Objects.equals(this.documentId, other.documentId)
                && this.blockNumber == other.blockNumber;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(documentId, blockNumber);
    }
}
