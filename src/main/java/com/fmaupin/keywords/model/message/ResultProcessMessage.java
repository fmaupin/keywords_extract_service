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

package com.fmaupin.keywords.model.message;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fmaupin.keywords.enumeration.StatusEnum;

import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ResultProcessMessage
 *
 * MODEL -> résultat traitement d'un message
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Getter
@Setter
@Builder
@ToString
public class ResultProcessMessage implements Comparable<ResultProcessMessage> {

    private InputMessage msg;

    @Builder.Default
    private StatusEnum status = StatusEnum.IN_PROGRESS;

    private Chunk result;

    private LocalDateTime processDate;

    @Override
    @Generated
    public int hashCode() {
        return new HashCodeBuilder().append(msg.getConsumeDate()).append(status).append(result).append(processDate)
                .toHashCode();
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ResultProcessMessage)) {
            return false;
        }

        ResultProcessMessage otherInputMessage = (ResultProcessMessage) o;

        return (compareTo(otherInputMessage) == 0);
    }

    @Override
    @Generated
    public int compareTo(ResultProcessMessage other) {
        // ATTENTION : tri sur date de consommation des messages -> conserver ordre
        // initial des messages
        return msg.getConsumeDate().compareTo(other.getMsg().getConsumeDate());
    }

}
