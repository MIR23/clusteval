/*******************************************************************************
 * Copyright (c) 2013 Christian Wiwie.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Christian Wiwie - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.utils;

import static ch.qos.logback.core.pattern.color.ANSIConstants.DEFAULT_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.RED_FG;
import static ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author Christian Wiwie
 * 
 */
public class MyHighlightingCompositeConverter
		extends
			HighlightingCompositeConverter {

	/**
	 * 
	 */
	public MyHighlightingCompositeConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter#
	 * getForegroundColorCode(ch.qos.logback.classic.spi.ILoggingEvent)
	 */
	@Override
	protected String getForegroundColorCode(ILoggingEvent event) {
		Level level = event.getLevel();
		switch (level.toInt()) {
			case Level.ERROR_INT :
				return RED_FG;
			case Level.WARN_INT :
				return YELLOW_FG;
			default :
				return DEFAULT_FG;
		}
	}
}
