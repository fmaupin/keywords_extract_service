package com.fmaupin.keywords.model;

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

import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * CompletedDocument
 *
 * MODEL -> contenu message queue `qcompleted`
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 29/12/25
 */
@Getter
@ToString
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CompletedDocument implements Comparable<CompletedDocument> {

    private UUID documentId;

    @Override
    @Generated
    public int compareTo(CompletedDocument other) {
        return this.documentId.compareTo(other.documentId);
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CompletedDocument))
            return false;
        CompletedDocument other = (CompletedDocument) o;

        return Objects.equals(this.documentId, other.documentId);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(documentId);
    }
}
