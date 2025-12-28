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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * InputMessage
 *
 * MODEL -> message consommé
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
@Getter
@Setter
@Builder
@ToString(callSuper = true, includeFieldNames = true)
@NoArgsConstructor
@AllArgsConstructor
public class InputMessage {

    private Chunk chunk;

    // horodatage de consommation du message
    private LocalDateTime consumeDate;

    public static InputMessage of(Chunk chunk) {
        return InputMessage.builder()
                .chunk(chunk)
                .consumeDate(LocalDateTime.now())
                .build();
    }

}
