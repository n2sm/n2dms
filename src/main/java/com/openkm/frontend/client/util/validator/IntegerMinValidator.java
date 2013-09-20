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

package com.openkm.frontend.client.util.validator;

import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBoxBase;

import eu.maydu.gwt.validation.client.ValidationAction;
import eu.maydu.gwt.validation.client.ValidationResult;
import eu.maydu.gwt.validation.client.Validator;
import eu.maydu.gwt.validation.client.i18n.StandardValidationMessages;
import eu.maydu.gwt.validation.client.i18n.ValidationMessages;

/**
 * IntegerMinValidator
 * 
 * @author jllort
 *
 */
public class IntegerMinValidator extends Validator<IntegerMinValidator> {

    private TextBoxBase text;

    private SuggestBox suggest;

    private int min;

    public IntegerMinValidator(final TextBoxBase text) {
        this(text, null);
    }

    public IntegerMinValidator(final TextBoxBase text, final String customMsgKey) {
        this(text, false);
    }

    public IntegerMinValidator(final TextBoxBase text, final int min) {
        this.text = text;
        this.min = min;
    }

    public IntegerMinValidator(final TextBoxBase text, final int min,
            final String customMsgKey) {
        this.text = text;
        this.min = min;
        setCustomMsgKey(customMsgKey);
    }

    public IntegerMinValidator(final TextBoxBase text,
            final boolean preventsPropagationOfValidationChain) {
        this(text, preventsPropagationOfValidationChain, null);
    }

    public IntegerMinValidator(final TextBoxBase text,
            final boolean preventsPropagationOfValidationChain,
            final String customMsgKey) {
        super();
        setPreventsPropagationOfValidationChain(preventsPropagationOfValidationChain);
        if (text == null) {
            throw new RuntimeException("text must not be null");
        }
        this.text = text;
        setCustomMsgKey(customMsgKey);
    }

    public IntegerMinValidator(final SuggestBox suggest) {
        this(suggest, null);
    }

    public IntegerMinValidator(final SuggestBox suggest,
            final String customMsgKey) {
        this(suggest, false);
        setCustomMsgKey(customMsgKey);
    }

    public IntegerMinValidator(final SuggestBox suggest, final int min) {
        this(suggest, min, false);
    }

    public IntegerMinValidator(final SuggestBox suggest,
            final boolean preventsPropagationOfValidationChain) {
        this(suggest, preventsPropagationOfValidationChain, null);
    }

    public IntegerMinValidator(final SuggestBox suggest,
            final boolean preventsPropagationOfValidationChain,
            final String customMsgKey) {
        super();
        setPreventsPropagationOfValidationChain(preventsPropagationOfValidationChain);
        if (suggest == null) {
            throw new RuntimeException("suggest must not be null");
        }
        this.suggest = suggest;
        setCustomMsgKey(customMsgKey);
    }

    public IntegerMinValidator(final SuggestBox suggest, final int min,
            final boolean preventsPropagationOfValidationChain) {
        this(suggest, min, preventsPropagationOfValidationChain, null);
    }

    public IntegerMinValidator(final SuggestBox suggest, final int min,
            final boolean preventsPropagationOfValidationChain,
            final String customMsgKey) {
        super();
        setPreventsPropagationOfValidationChain(preventsPropagationOfValidationChain);
        if (suggest == null) {
            throw new RuntimeException("suggest must not be null");
        }
        this.suggest = suggest;
        this.min = min;
        setCustomMsgKey(customMsgKey);
    }

    @Override
    public ValidationResult validate(final ValidationMessages allMessages) {
        final StandardValidationMessages messages = allMessages
                .getStandardMessages();
        String str;
        if (text != null) {
            str = text.getText();
        } else {
            str = suggest.getText();
        }
        if (!isRequired() && str.equals("")) {
            return null;
        }
        str = str.trim();
        if (str.equals("")) {
            return new ValidationResult(getErrorMessage(allMessages,
                    messages.notAnInteger()));
        }

        //Integer in range
        try {
            final long value = Long.parseLong(str);
            if (value < min) {
                return new ValidationResult(getErrorMessage(allMessages,
                        messages.validator_min()));
            }
        } catch (final NumberFormatException ex) {
            return new ValidationResult(getErrorMessage(allMessages,
                    messages.notAnInteger()));
        }

        return null;
    }

    @Override
    public void invokeActions(final ValidationResult result) {
        if (text != null) {
            for (final ValidationAction<TextBoxBase> action : getFailureActions()) {
                action.invoke(result, text);
            }
        } else {
            for (final ValidationAction<SuggestBox> action : getFailureActions()) {
                action.invoke(result, suggest);
            }
        }
    }
}
