/*
 * Copyright (C) 2025 Fabrice MAUPIN
 *
 * This file is part of Read Content Micro Service.
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

package com.fmaupin.keywords.enumeration;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Common
 *
 * Boite à outils pour les énumérations
 *
 *
 * @author Fabrice MAUPIN
 * @version 0.0.1-SNAPSHOT
 * @since 30/10/25
 */
public interface Common {

	/**
	 * récupération valeur de la clé dans énumération
	 * 
	 * @param name : clé
	 * @param e    : énumération
	 * @return null -> clé n'existe pas ou valeur
	 */
	static <E extends Enum<E>> E getValue(String name, Class<E> e) {
		Map<String, E> lookup = EnumSet.allOf(e).stream().collect(Collectors.toMap(Enum::name, Function.identity()));

		return lookup.get(name);
	}
}
