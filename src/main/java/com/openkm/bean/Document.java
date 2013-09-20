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

package com.openkm.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author pavila
 * 
 */
public class Document implements Serializable {
    private static final long serialVersionUID = 4453338766237619444L;

    public static final String TYPE = "okm:document";

    public static final String CONTENT = "okm:content";

    public static final String CONTENT_TYPE = "okm:resource";

    public static final String SIZE = "okm:size";

    public static final String LANGUAGE = "okm:language";

    public static final String AUTHOR = "okm:author";

    public static final String VERSION_COMMENT = "okm:versionComment";

    public static final String NAME = "okm:name";

    public static final String TEXT = "okm:text";

    public static final String TITLE = "okm:title";

    public static final String DESCRIPTION = "okm:description";

    private String path;

    private String title = "";

    private String description = "";

    private String language = "";

    private String author;

    private Calendar created;

    private Calendar lastModified;

    private String mimeType;

    private boolean locked;

    private boolean checkedOut;

    private Version actualVersion;

    private int permissions;

    private LockInfo lockInfo;

    private String uuid;

    private boolean subscribed;

    private boolean convertibleToPdf;

    private boolean convertibleToSwf;

    private Set<String> subscriptors = new HashSet<String>();

    private Set<String> keywords = new HashSet<String>();

    private Set<Folder> categories = new HashSet<Folder>();

    private List<Note> notes = new ArrayList<Note>();

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(final LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(final Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(final Calendar created) {
        this.created = created;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(final Set<String> keywords) {
        this.keywords = keywords;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(final boolean checkedOut) {
        this.checkedOut = checkedOut;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public Version getActualVersion() {
        return actualVersion;
    }

    public void setActualVersion(final Version actualVersion) {
        this.actualVersion = actualVersion;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(final int permissions) {
        this.permissions = permissions;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public Set<String> getSubscriptors() {
        return subscriptors;
    }

    public void setSubscriptors(final Set<String> subscriptors) {
        this.subscriptors = subscriptors;
    }

    public Set<Folder> getCategories() {
        return categories;
    }

    public void setCategories(final Set<Folder> categories) {
        this.categories = categories;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public boolean isConvertibleToPdf() {
        return convertibleToPdf;
    }

    public void setConvertibleToPdf(final boolean convertibleToPdf) {
        this.convertibleToPdf = convertibleToPdf;
    }

    public boolean isConvertibleToSwf() {
        return convertibleToSwf;
    }

    public void setConvertibleToSwf(final boolean convertibleToSwf) {
        this.convertibleToSwf = convertibleToSwf;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(final List<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("path=").append(path);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", mimeType=").append(mimeType);
        sb.append(", author=").append(author);
        sb.append(", permissions=").append(permissions);
        sb.append(", created=").append(
                created == null ? null : created.getTime());
        sb.append(", lastModified=").append(
                lastModified == null ? null : lastModified.getTime());
        sb.append(", keywords=").append(keywords);
        sb.append(", categories=").append(categories);
        sb.append(", locked=").append(locked);
        sb.append(", lockInfo=").append(lockInfo);
        sb.append(", actualVersion=").append(actualVersion);
        sb.append(", subscribed=").append(subscribed);
        sb.append(", subscriptors=").append(subscriptors);
        sb.append(", uuid=").append(uuid);
        sb.append(", convertibleToPdf=").append(convertibleToPdf);
        sb.append(", convertibleToSwf=").append(convertibleToSwf);
        sb.append(", notes=").append(notes);
        sb.append("}");
        return sb.toString();
    }
}
