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

package com.openkm.bean.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.dao.KeyValueDAO;
import com.openkm.dao.bean.KeyValue;

public class Select extends FormElement {
    private static Logger log = LoggerFactory.getLogger(Select.class);

    private static final long serialVersionUID = 1L;

    public static final String TYPE_SIMPLE = "simple";

    public static final String TYPE_MULTIPLE = "multiple";

    private List<Validator> validators = new ArrayList<Validator>();

    private List<Option> options = new ArrayList<Option>();

    private String type = TYPE_SIMPLE;

    private String value = "";

    private String data = "";

    private String optionsData = "";

    private String table = "";

    private String optionsQuery = "";

    private boolean readonly = false;

    public Select() {
        super.width = "150px";
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(final List<Option> options) {
        handleDbOptions(options);
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidators(final List<Validator> validators) {
        this.validators = validators;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }

    public String getOptionsData() {
        return optionsData;
    }

    public void setOptionsData(final String optionsData) {
        this.optionsData = optionsData;
    }

    public String getTable() {
        return table;
    }

    public void setTable(final String table) {
        this.table = table;
    }

    public String getOptionsQuery() {
        return optionsQuery;
    }

    public void setOptionsQuery(final String optionsQuery) {
        this.optionsQuery = optionsQuery;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(final boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("label=").append(label);
        sb.append(", name=").append(name);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", readonly=").append(readonly);
        sb.append(", type=").append(type);
        sb.append(", value=").append(value);
        sb.append(", data=").append(data);
        sb.append(", optionsData=").append(optionsData);
        sb.append(", options=").append(options);
        sb.append(", validators=").append(validators);
        sb.append(", table=").append(table);
        sb.append(", optionsQuery=").append(optionsQuery);
        sb.append("}");
        return sb.toString();
    }

    /**
     * If Select reads options from DB, it gets options from DB and set into internal list {@see Select#options}.
     * It is assumed that each option in list has different Value. New options are matched by Value with old
     * options (by temporal hash) and selected if old option is also selected.
     * 
     * If {@see Select#filterQuery} is not specified, it replace internal list of options with parameter options.
     * 
     * @param options list of options
     */
    private List<Option> handleDbOptions(final List<Option> options) {
        // read options from DB?
        if (optionsQuery == null || optionsQuery.isEmpty()) {
            // no -> set options from parameter
            this.options = options;
        } else {
            // read options from DB:
            final List<Option> dbOptions = getOptionsFromDb();

            // creates hashed options (key is value) from internal option list
            final HashMap<String, Option> hashedOptions = new HashMap<String, Option>();

            if (this.options != null) {
                for (final Option option : this.options) {
                    hashedOptions.put(option.getValue(), option);
                }
            }

            // iterates DB options and set option if value is matched
            for (final Option dbOption : dbOptions) {
                if (dbOption == null) {
                    continue;
                }

                final Option option = hashedOptions.get(dbOption.getValue());
                dbOption.setSelected(option != null ? option.isSelected()
                        : false);
            }

            this.options = dbOptions;
        }

        return this.options;
    }

    /**
     * Return list of Select's Options from meta table according to query.
     * 
     * @return list of options from meta table, empty list on error.
     */
    private List<Option> getOptionsFromDb() {
        final List<Option> dbOptions = new ArrayList<Option>();

        try {
            log.debug("Getting options from DB (table={}, query={})",
                    new Object[] { table, optionsQuery });
            final List<KeyValue> keyValues = KeyValueDAO.getKeyValues(table,
                    optionsQuery);

            for (final KeyValue keyValue : keyValues) {
                final Option option = new Option();
                option.setValue(keyValue.getKey());
                option.setLabel(keyValue.getValue());
                dbOptions.add(option);
            }

            log.debug("Got {} options from DB", dbOptions.size());
        } catch (final Throwable t) {
            log.error("Unable to get key values for Select", t);
        }

        return dbOptions;
    }
}
