/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2013  Paco Avila & Josep Llort
 *
 *  No bytes were intentionally harmed during the development of this application.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.validator.note;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.Config;
import com.openkm.validator.ValidatorException;

/**
 * Complex note validator
 */
public class CompleteNoteValidator implements NoteValidator {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(CompleteNoteValidator.class);

    @Override
    public void Validate(String text) throws ValidatorException {
        validateLength(text);
    }

    /**
     * Validate length
     */
    private void validateLength(String text) throws ValidatorException {
        if (Config.VALIDATOR_NOTE_MIN_LENGTH > 0 && text.length() < Config.VALIDATOR_NOTE_MIN_LENGTH) {
            throw new ValidatorException(Config.VALIDATOR_NOTE_ERROR_MIN_LENGTH);
        }

        if (Config.VALIDATOR_NOTE_MAX_LENGTH > 0 && text.length() > Config.VALIDATOR_NOTE_MAX_LENGTH) {
            throw new ValidatorException(Config.VALIDATOR_NOTE_ERROR_MAX_LENGTH);
        }
    }

}
