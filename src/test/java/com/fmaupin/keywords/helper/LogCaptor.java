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

package com.fmaupin.keywords.helper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Generated;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/* LogCaptor
*
* Helper pour capturer les logs d'une classe pendant les tests
* et empêcher leur affichage dans la console ou dans un fichier.
*
* @author Fabrice MAUPIN
* @version 0.0.1-SNAPSHOT
* @since 17/12/25
*/
@Generated
public class LogCaptor implements BeforeEachCallback, ExtensionContext.Store.CloseableResource {

    private final Class<?> clazz;
    private CapturingAppender appender;
    private List<String> capturedLogs = new ArrayList<>();
    private Logger logger;
    private List<String> detachedAppenders = new ArrayList<>();

    public LogCaptor(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        logger = (Logger) LoggerFactory.getLogger(clazz);
        logger.setAdditive(false); // empêche la propagation vers le root

        // Désactiver les appenders existants (Console, File, etc.)
        logger.iteratorForAppenders().forEachRemaining(a -> {
            detachedAppenders.add(a.getName());
            logger.detachAppender(a);
        });

        // Ajouter un appender factice qui capture les logs
        appender = new CapturingAppender();
        appender.start();
        logger.addAppender(appender);
    }

    /**
     * Retourne les logs capturés pendant le test
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(capturedLogs);
    }

    /**
     * Restaure le logger à l'état initial
     */
    @Override
    public void close() {
        if (logger != null && appender != null) {
            logger.detachAppender(appender);
        }
        // Ré-attacher les appenders originaux si besoin
        // Ici on ne les rattache pas pour garder les tests isolés
    }

    private class CapturingAppender extends AppenderBase<ILoggingEvent> {
        @Override
        protected void append(ILoggingEvent eventObject) {
            capturedLogs.add(eventObject.getFormattedMessage());
        }
    }
}
