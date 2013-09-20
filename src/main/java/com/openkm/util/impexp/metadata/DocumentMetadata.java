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

package com.openkm.util.impexp.metadata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DocumentMetadata {
    // okm:document
    private String uuid;

    private String author;

    private String name;

    private String path;

    private Calendar created;

    private Calendar lastModified;

    private String language;

    private String title;

    private String description;

    private Set<String> keywords = new HashSet<String>();

    private Set<CategoryMetadata> categories = new HashSet<CategoryMetadata>();

    // mix:notification
    private Set<String> subscriptors = new HashSet<String>();

    // mix:scripting
    private String scripting;

    // mix:encryption
    private String cipherName;

    // okm:notes
    private List<NoteMetadata> notes = new ArrayList<NoteMetadata>();

    // mix:property_group
    private List<PropertyGroupMetadata> propertyGroups = new ArrayList<PropertyGroupMetadata>();

    // okm:resource
    private VersionMetadata version;

    // mix:accessControlled
    private Map<String, Integer> grantedUsers = new HashMap<String, Integer>();

    private Map<String, Integer> grantedRoles = new HashMap<String, Integer>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(final Calendar created) {
        this.created = created;
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

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(final Set<String> keywords) {
        this.keywords = keywords;
    }

    public Set<CategoryMetadata> getCategories() {
        return categories;
    }

    public void setCategories(final Set<CategoryMetadata> categories) {
        this.categories = categories;
    }

    public Set<String> getSubscriptors() {
        return subscriptors;
    }

    public void setSubscriptors(final Set<String> subscriptors) {
        this.subscriptors = subscriptors;
    }

    public String getScripting() {
        return scripting;
    }

    public void setScripting(final String scripting) {
        this.scripting = scripting;
    }

    public String getCipherName() {
        return cipherName;
    }

    public void setCipherName(final String cipherName) {
        this.cipherName = cipherName;
    }

    public List<NoteMetadata> getNotes() {
        return notes;
    }

    public void setNotes(final List<NoteMetadata> notes) {
        this.notes = notes;
    }

    public List<PropertyGroupMetadata> getPropertyGroups() {
        return propertyGroups;
    }

    public void setPropertyGroups(
            final List<PropertyGroupMetadata> propertyGroups) {
        this.propertyGroups = propertyGroups;
    }

    public VersionMetadata getVersion() {
        return version;
    }

    public void setVersion(final VersionMetadata version) {
        this.version = version;
    }

    public Map<String, Integer> getGrantedUsers() {
        return grantedUsers;
    }

    public void setGrantedUsers(final Map<String, Integer> grantedUsers) {
        this.grantedUsers = grantedUsers;
    }

    public Map<String, Integer> getGrantedRoles() {
        return grantedRoles;
    }

    public void setGrantedRoles(final Map<String, Integer> grantedRoles) {
        this.grantedRoles = grantedRoles;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("uuid=");
        sb.append(uuid);
        sb.append(", author=");
        sb.append(author);
        sb.append(", name=");
        sb.append(name);
        sb.append(", path=");
        sb.append(path);
        sb.append(", created=");
        sb.append(created == null ? null : created.getTime());
        sb.append(", lastModified=");
        sb.append(lastModified == null ? null : lastModified.getTime());
        sb.append(", language=");
        sb.append(language);
        sb.append(", title=");
        sb.append(title);
        sb.append(", description=");
        sb.append(description);
        sb.append(", keywords=");
        sb.append(keywords);
        sb.append(", categories=");
        sb.append(categories);
        sb.append(", subscriptors=");
        sb.append(subscriptors);
        sb.append(", scripting=");
        sb.append(scripting);
        sb.append(", cipherName=");
        sb.append(cipherName);
        sb.append(", notes=");
        sb.append(notes);
        sb.append(", propertyGroups=");
        sb.append(propertyGroups);
        sb.append(", version=");
        sb.append(version);
        sb.append(", grantedUsers=");
        sb.append(grantedUsers);
        sb.append(", grantedRoles=");
        sb.append(grantedRoles);
        sb.append("}");
        return sb.toString();
    }
}
