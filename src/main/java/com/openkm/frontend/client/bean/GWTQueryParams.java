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

package com.openkm.frontend.client.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Query params
 * 
 * @author jllort
 */
public class GWTQueryParams implements IsSerializable, Cloneable {
    public static final int DOCUMENT = 1;

    public static final int FOLDER = 2;

    public static final int MAIL = 4;

    public static final String OPERATOR_AND = "and";

    public static final String OPERATOR_OR = "or";

    private long id;

    private String queryName;

    private String name;

    private String keywords;

    private String content;

    private String path;

    private String mimeType;

    private String author;

    private Date lastModifiedFrom;

    private Date lastModifiedTo;

    private boolean isDashboard = false;

    private long domain = 0;

    private String mailFrom = "";

    private String mailTo = "";

    private String mailSubject = "";

    private String categoryUuid = "";

    private String categoryPath = "";

    private String operator = OPERATOR_AND;

    private Map<String, GWTPropertyParams> properties = new HashMap<String, GWTPropertyParams>();

    private String grpName;

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public GWTQueryParams clone() {
        final GWTQueryParams newParans = new GWTQueryParams();
        newParans.setAuthor(getAuthor());
        newParans.setCategoryPath(getCategoryPath());
        newParans.setCategoryUuid(getCategoryUuid());
        newParans.setContent(getContent());
        newParans.setDashboard(isDashboard());
        newParans.setDomain(getDomain());
        newParans.setGrpName(getGrpName());
        newParans.setId(getId());
        newParans.setKeywords(getKeywords());
        newParans.setLastModifiedFrom(getLastModifiedFrom());
        newParans.setLastModifiedTo(getLastModifiedTo());
        newParans.setMailFrom(getMailFrom());
        newParans.setMailSubject(getMailSubject());
        newParans.setMailTo(getMailTo());
        newParans.setMimeType(getMimeType());
        newParans.setName(getName());
        newParans.setOperator(getOperator());
        newParans.setPath(getPath());
        final Map<String, GWTPropertyParams> newProperties = new HashMap<String, GWTPropertyParams>();

        for (final String key : properties.keySet()) {
            newProperties.put(key, properties.get(key));
        }

        newParans.setProperties(newProperties);
        newParans.setQueryName(getQueryName());
        return newParans;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(final String keywords) {
        this.keywords = keywords;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, GWTPropertyParams> getProperties() {
        return properties;
    }

    public void setProperties(
            final Map<String, GWTPropertyParams> finalProperties) {
        properties = finalProperties;
    }

    public String getGrpName() {
        return grpName;
    }

    public void setGrpName(final String grpName) {
        this.grpName = grpName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public Date getLastModifiedFrom() {
        return lastModifiedFrom;
    }

    public void setLastModifiedFrom(final Date lastModifiedFrom) {
        this.lastModifiedFrom = lastModifiedFrom;
    }

    public Date getLastModifiedTo() {
        return lastModifiedTo;
    }

    public void setLastModifiedTo(final Date lastModifiedTo) {
        this.lastModifiedTo = lastModifiedTo;
    }

    public boolean isDashboard() {
        return isDashboard;
    }

    public void setDashboard(final boolean isDashboard) {
        this.isDashboard = isDashboard;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public long getDomain() {
        return domain;
    }

    public void setDomain(final long domain) {
        this.domain = domain;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(final String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(final String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(final String mailSubject) {
        this.mailSubject = mailSubject;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public String getCategoryUuid() {
        return categoryUuid;
    }

    public void setCategoryUuid(final String uuid) {
        categoryUuid = uuid;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(final String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(final String queryName) {
        this.queryName = queryName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("name=");
        sb.append(name);
        sb.append(", keywords=");
        sb.append(keywords);
        sb.append(", content=");
        sb.append(content);
        sb.append(", path=");
        sb.append(path);
        sb.append(", mimeType=");
        sb.append(mimeType);
        sb.append(", author=");
        sb.append(author);
        sb.append(", isDashboard=" + isDashboard);
        sb.append(", lastModifiedFrom=");
        sb.append(lastModifiedFrom == null ? null : lastModifiedFrom.getTime());
        sb.append(", lastModifiedTo=");
        sb.append(lastModifiedTo == null ? null : lastModifiedTo.getTime());
        sb.append(", properties=");
        sb.append(properties);
        sb.append("]");
        return sb.toString();
    }
}
