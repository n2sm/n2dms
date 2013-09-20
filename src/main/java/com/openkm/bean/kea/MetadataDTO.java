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

package com.openkm.bean.kea;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * MetadataDTO
 * 
 * @author jllort
 */
public class MetadataDTO implements Serializable {
    private static final long serialVersionUID = 2530668808598426112L;

    private String fileName;

    private String tempFileName;

    private String mimeType = "";

    private String title = "";

    private String creator = "";

    private String generator = "";

    private String keyword = "";

    private int pageCount;

    private List<String> subjects;

    private Date contentCreated = null;

    private Date contentLastModified = null;

    /**
     * MetadataDTO
     */
    public MetadataDTO() {
        subjects = new ArrayList<String>();
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        if (creator != null) {
            this.creator = creator;
        }
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<Term> getSubjectsAsTerms() {
        final List<Term> terms = new ArrayList<Term>();
        final Iterator<String> iter = subjects.iterator();
        while (iter.hasNext()) {
            terms.add(new Term("", iter.next()));
        }
        return terms;
    }

    public void setSubjects(final List<String> subjects) {
        this.subjects = subjects;
    }

    public void addSubject(final String subject) {
        if (subject != null) {
            subjects.add(subject);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        if (fileName != null) {
            this.fileName = fileName;
        }
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(final String tempFileName) {
        if (tempFileName != null) {
            this.tempFileName = tempFileName;
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        if (mimeType != null) {
            this.mimeType = mimeType;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(final String generator) {
        this.generator = generator;
    }

    public Date getContentCreated() {
        if (contentCreated != null) {
            return new Date(contentCreated.getTime());
        } else {
            return null;
        }
    }

    public void setContentCreated(final Date contentCreated) {
        if (contentCreated != null) {
            this.contentCreated = new Date(contentCreated.getTime());
        } else {
            this.contentCreated = null;
        }
    }

    public Date getContentLastModified() {
        if (contentLastModified != null) {
            return new Date(contentLastModified.getTime());
        } else {
            return null;
        }
    }

    public void setContentLastModified(final Date contentLastModified) {
        if (contentLastModified != null) {
            this.contentLastModified = new Date(contentLastModified.getTime());
        } else {
            this.contentLastModified = null;
        }
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(final int pageCount) {
        this.pageCount = pageCount;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(final String keyword) {
        this.keyword = keyword;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MetadataDTO that = (MetadataDTO) o;

        if (contentCreated != null ? !contentCreated
                .equals(that.contentCreated) : that.contentCreated != null) {
            return false;
        }
        if (contentLastModified != null ? !contentLastModified
                .equals(that.contentLastModified)
                : that.contentLastModified != null) {
            return false;
        }
        if (!creator.equals(that.creator)) {
            return false;
        }
        if (!fileName.equals(that.fileName)) {
            return false;
        }
        if (!mimeType.equals(that.mimeType)) {
            return false;
        }
        if (!subjects.equals(that.subjects)) {
            return false;
        }
        if (!tempFileName.equals(that.tempFileName)) {
            return false;
        }
        if (!title.equals(that.title)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = fileName.hashCode();
        result = 31 * result + tempFileName.hashCode();
        result = 31 * result + mimeType.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + creator.hashCode();
        result = 31 * result
                + (contentCreated != null ? contentCreated.hashCode() : 0);
        result = 31
                * result
                + (contentLastModified != null ? contentLastModified.hashCode()
                        : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("title=").append(title);
        sb.append(", mimeType=").append(mimeType);
        sb.append(", fileName=").append(fileName);
        sb.append(", tempFileName=").append(tempFileName);
        sb.append(", creator=").append(creator);
        sb.append(", generator=").append(generator);
        sb.append(", keyword=").append(keyword);
        sb.append(", pageCount=").append(pageCount);
        sb.append(", contentCreated=").append(
                contentCreated == null ? null : contentCreated.getTime());
        sb.append(", contentLastModified=").append(
                contentLastModified == null ? null : contentLastModified
                        .getTime());
        sb.append(", subjects=").append(subjects);
        sb.append("}");
        return sb.toString();
    }
}
