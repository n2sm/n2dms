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

package com.openkm.dao.bean;

import java.io.Serializable;

public class ProfileMenuEdit implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean lockVisible;

    private boolean unlockVisible;

    private boolean deleteVisible;

    private boolean checkInVisible;

    private boolean checkOutVisible;

    private boolean cancelCheckOutVisible;

    private boolean renameVisible;

    private boolean copyVisible;

    private boolean moveVisible;

    private boolean addPropertyGroupVisible;

    private boolean removePropertyGroupVisible;

    private boolean addNoteVisible;

    private boolean removeNoteVisible;

    private boolean addCategoryVisible;

    private boolean removeCategoryVisible;

    private boolean addKeywordVisible;

    private boolean removeKeywordVisible;

    private boolean addSubscriptionVisible;

    private boolean removeSubscriptionVisible;

    private boolean mergePdfVisible;

    public boolean isLockVisible() {
        return lockVisible;
    }

    public void setLockVisible(final boolean lockVisible) {
        this.lockVisible = lockVisible;
    }

    public boolean isUnlockVisible() {
        return unlockVisible;
    }

    public void setUnlockVisible(final boolean unlockVisible) {
        this.unlockVisible = unlockVisible;
    }

    public boolean isDeleteVisible() {
        return deleteVisible;
    }

    public void setDeleteVisible(final boolean deleteVisible) {
        this.deleteVisible = deleteVisible;
    }

    public boolean isCheckInVisible() {
        return checkInVisible;
    }

    public void setCheckInVisible(final boolean checkInVisible) {
        this.checkInVisible = checkInVisible;
    }

    public boolean isCheckOutVisible() {
        return checkOutVisible;
    }

    public void setCheckOutVisible(final boolean checkOutVisible) {
        this.checkOutVisible = checkOutVisible;
    }

    public boolean isCancelCheckOutVisible() {
        return cancelCheckOutVisible;
    }

    public void setCancelCheckOutVisible(final boolean cancelCheckOutVisible) {
        this.cancelCheckOutVisible = cancelCheckOutVisible;
    }

    public boolean isRenameVisible() {
        return renameVisible;
    }

    public void setRenameVisible(final boolean renameVisible) {
        this.renameVisible = renameVisible;
    }

    public boolean isCopyVisible() {
        return copyVisible;
    }

    public void setCopyVisible(final boolean copyVisible) {
        this.copyVisible = copyVisible;
    }

    public boolean isMoveVisible() {
        return moveVisible;
    }

    public void setMoveVisible(final boolean moveVisible) {
        this.moveVisible = moveVisible;
    }

    public boolean isAddPropertyGroupVisible() {
        return addPropertyGroupVisible;
    }

    public void setAddPropertyGroupVisible(final boolean addPropertyGroupVisible) {
        this.addPropertyGroupVisible = addPropertyGroupVisible;
    }

    public boolean isRemovePropertyGroupVisible() {
        return removePropertyGroupVisible;
    }

    public void setRemovePropertyGroupVisible(
            final boolean removePropertyGroupVisible) {
        this.removePropertyGroupVisible = removePropertyGroupVisible;
    }

    public boolean isAddNoteVisible() {
        return addNoteVisible;
    }

    public void setAddNoteVisible(final boolean addNoteVisible) {
        this.addNoteVisible = addNoteVisible;
    }

    public boolean isRemoveNoteVisible() {
        return removeNoteVisible;
    }

    public void setRemoveNoteVisible(final boolean removeNoteVisible) {
        this.removeNoteVisible = removeNoteVisible;
    }

    public boolean isAddCategoryVisible() {
        return addCategoryVisible;
    }

    public void setAddCategoryVisible(final boolean addCategoryVisible) {
        this.addCategoryVisible = addCategoryVisible;
    }

    public boolean isRemoveCategoryVisible() {
        return removeCategoryVisible;
    }

    public void setRemoveCategoryVisible(final boolean removeCategoryVisible) {
        this.removeCategoryVisible = removeCategoryVisible;
    }

    public boolean isAddKeywordVisible() {
        return addKeywordVisible;
    }

    public void setAddKeywordVisible(final boolean addKeywordVisible) {
        this.addKeywordVisible = addKeywordVisible;
    }

    public boolean isRemoveKeywordVisible() {
        return removeKeywordVisible;
    }

    public void setRemoveKeywordVisible(final boolean removeKeywordVisible) {
        this.removeKeywordVisible = removeKeywordVisible;
    }

    public boolean isAddSubscriptionVisible() {
        return addSubscriptionVisible;
    }

    public void setAddSubscriptionVisible(final boolean addSubscriptionVisible) {
        this.addSubscriptionVisible = addSubscriptionVisible;
    }

    public boolean isRemoveSubscriptionVisible() {
        return removeSubscriptionVisible;
    }

    public void setRemoveSubscriptionVisible(
            final boolean removeSubscriptionVisible) {
        this.removeSubscriptionVisible = removeSubscriptionVisible;
    }

    public boolean isMergePdfVisible() {
        return mergePdfVisible;
    }

    public void setMergePdfVisible(final boolean mergePdfVisible) {
        this.mergePdfVisible = mergePdfVisible;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("lockVisible=").append(lockVisible);
        sb.append(", unlockVisible=").append(unlockVisible);
        sb.append(", checkInVisible=").append(checkInVisible);
        sb.append(", checkOutVisible=").append(checkOutVisible);
        sb.append(", cancelCheckOutVisible=").append(cancelCheckOutVisible);
        sb.append(", deleteVisible=").append(deleteVisible);
        sb.append(", renameVisible=").append(renameVisible);
        sb.append(", copyVisible=").append(copyVisible);
        sb.append(", moveVisible=").append(moveVisible);
        sb.append(", addPropertyGroupVisible=").append(addPropertyGroupVisible);
        sb.append(", removePropertyGroupVisible=").append(
                removePropertyGroupVisible);
        sb.append(", addNoteVisible=").append(addNoteVisible);
        sb.append(", removeNoteVisible=").append(removeNoteVisible);
        sb.append(", addCategoryVisible=").append(addCategoryVisible);
        sb.append(", removeCategoryVisible=").append(removeCategoryVisible);
        sb.append(", addKeywordVisible=").append(addKeywordVisible);
        sb.append(", removeKeywordVisible=").append(removeKeywordVisible);
        sb.append(", addSubscriptionVisible=").append(addSubscriptionVisible);
        sb.append(", removeSubscriptionVisible=").append(
                removeSubscriptionVisible);
        sb.append(", mergePdfVisible=").append(mergePdfVisible);
        sb.append("}");
        return sb.toString();
    }
}
