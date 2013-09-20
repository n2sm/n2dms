package com.openkm.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.Config;
import com.openkm.core.RepositoryException;
import com.openkm.validator.note.NoteValidator;
import com.openkm.validator.password.PasswordValidator;

public class ValidatorFactory {
    private static Logger log = LoggerFactory.getLogger(ValidatorFactory.class);

    private static PasswordValidator passwordValidator = null;

    private static NoteValidator noteValidator = null;

    /**
     * Password validator
     */
    public static PasswordValidator getPasswordValidator()
            throws RepositoryException {
        if (passwordValidator == null) {
            try {
                log.info("PasswordValidator: {}", Config.VALIDATOR_PASSWORD);
                final Object object = Class.forName(Config.VALIDATOR_PASSWORD)
                        .newInstance();
                passwordValidator = (PasswordValidator) object;
            } catch (final ClassNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            } catch (final InstantiationException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            } catch (final IllegalAccessException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            }
        }

        return passwordValidator;
    }

    public static NoteValidator getNoteValidator() throws RepositoryException {
        if (noteValidator == null) {
            try {
                log.info("NoteValidator: {}", Config.VALIDATOR_NOTE);
                final Object object = Class.forName(Config.VALIDATOR_NOTE)
                        .newInstance();
                noteValidator = (NoteValidator) object;
            } catch (final ClassNotFoundException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            } catch (final InstantiationException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            } catch (final IllegalAccessException e) {
                log.error(e.getMessage(), e);
                throw new RepositoryException(e.getMessage(), e);
            }
        }

        return noteValidator;
    }
}
