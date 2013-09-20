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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GWTAvailableOption
 * 
 * @author jllort
 *
 */
public class GWTAvailableOption implements IsSerializable {
    private boolean createFolderOption = true;

    private boolean findFolderOption = true;

    private boolean findDocumentOption = true;

    private boolean downloadOption = true;

    private boolean downloadPdfOption = true;

    private boolean lockOption = true;

    private boolean unLockOption = true;

    private boolean addDocumentOption = true;

    private boolean checkoutOption = true;

    private boolean checkinOption = true;

    private boolean cancelCheckoutOption = true;

    private boolean deleteOption = true;

    private boolean addPropertyGroupOption = true;

    private boolean removePropertyGroupOption = true;

    private boolean addSubscriptionOption = true;

    private boolean removeSubscriptionOption = true;

    private boolean homeOption = true;

    private boolean refreshOption = true;

    private boolean workflowOption = true;

    private boolean scannerOption = true;

    private boolean uploaderOption = true;

    private boolean renameOption = true;

    private boolean copyOption = true;

    private boolean moveOption = true;

    private boolean addBookmarkOption = true;

    private boolean setHomeOption = true;

    private boolean exportOption = true;

    private boolean mediaPlayerOption = true;

    private boolean imageViewerOption = true;

    private boolean gotoFolderOption = true;

    private boolean createFromTemplateOption = true;

    private boolean purgeOption = true;

    private boolean restoreOption = true;

    private boolean purgeTrashOption = true;

    private boolean sendDocumentLinkOption = true;

    private boolean sendDocumentAttachmentOption = true;

    private boolean skinOption = true;

    private boolean debugOption = true;

    private boolean administrationOption = true;

    private boolean manageBookmarkOption = true;

    private boolean helpOption = true;

    private boolean documentationOption = true;

    private boolean bugReportOption = true;

    private boolean supportRequestOption = true;

    private boolean publicForumOption = true;

    private boolean versionChangesOption = true;

    private boolean projectWebOption = true;

    private boolean aboutOption = true;

    private boolean languagesOption = true;

    private boolean preferencesOption = true;

    private boolean addNoteOption = true;

    private boolean addCategoryOption = true;

    private boolean addKeywordOption = true;

    private boolean removeNoteOption = true;

    private boolean removeCategoryOption = true;

    private boolean removeKeywordOption = true;

    private boolean mergePdfOption = true;

    public GWTAvailableOption() {
    }

    public boolean isCreateFolderOption() {
        return createFolderOption;
    }

    public void setCreateFolderOption(final boolean createFolderOption) {
        this.createFolderOption = createFolderOption;
    }

    public boolean isFindFolderOption() {
        return findFolderOption;
    }

    public void setFindFolderOption(final boolean findFolderOption) {
        this.findFolderOption = findFolderOption;
    }

    public boolean isDownloadOption() {
        return downloadOption;
    }

    public void setDownloadOption(final boolean downloadOption) {
        this.downloadOption = downloadOption;
    }

    public boolean isDownloadPdfOption() {
        return downloadPdfOption;
    }

    public void setDownloadPdfOption(final boolean downloadPdfOption) {
        this.downloadPdfOption = downloadPdfOption;
    }

    public boolean isLockOption() {
        return lockOption;
    }

    public void setLockOption(final boolean lockOption) {
        this.lockOption = lockOption;
    }

    public boolean isUnLockOption() {
        return unLockOption;
    }

    public void setUnLockOption(final boolean unLockOption) {
        this.unLockOption = unLockOption;
    }

    public boolean isAddDocumentOption() {
        return addDocumentOption;
    }

    public void setAddDocumentOption(final boolean addDocumentOption) {
        this.addDocumentOption = addDocumentOption;
    }

    public boolean isCheckoutOption() {
        return checkoutOption;
    }

    public void setCheckoutOption(final boolean checkoutOption) {
        this.checkoutOption = checkoutOption;
    }

    public boolean isCheckinOption() {
        return checkinOption;
    }

    public void setCheckinOption(final boolean checkinOption) {
        this.checkinOption = checkinOption;
    }

    public boolean isCancelCheckoutOption() {
        return cancelCheckoutOption;
    }

    public void setCancelCheckoutOption(final boolean cancelCheckoutOption) {
        this.cancelCheckoutOption = cancelCheckoutOption;
    }

    public boolean isDeleteOption() {
        return deleteOption;
    }

    public void setDeleteOption(final boolean deleteOption) {
        this.deleteOption = deleteOption;
    }

    public boolean isAddPropertyGroupOption() {
        return addPropertyGroupOption;
    }

    public void setAddPropertyGroupOption(final boolean addPropertyGroupOption) {
        this.addPropertyGroupOption = addPropertyGroupOption;
    }

    public boolean isRemovePropertyGroupOption() {
        return removePropertyGroupOption;
    }

    public void setRemovePropertyGroupOption(
            final boolean removePropertyGroupOption) {
        this.removePropertyGroupOption = removePropertyGroupOption;
    }

    public boolean isAddSubscriptionOption() {
        return addSubscriptionOption;
    }

    public void setAddSubscriptionOption(final boolean addSubscriptionOption) {
        this.addSubscriptionOption = addSubscriptionOption;
    }

    public boolean isRemoveSubscriptionOption() {
        return removeSubscriptionOption;
    }

    public void setRemoveSubscriptionOption(
            final boolean removeSubscriptionOption) {
        this.removeSubscriptionOption = removeSubscriptionOption;
    }

    public boolean isHomeOption() {
        return homeOption;
    }

    public void setHomeOption(final boolean homeOption) {
        this.homeOption = homeOption;
    }

    public boolean isRefreshOption() {
        return refreshOption;
    }

    public void setRefreshOption(final boolean refreshOption) {
        this.refreshOption = refreshOption;
    }

    public boolean isWorkflowOption() {
        return workflowOption;
    }

    public void setWorkflowOption(final boolean workflowOption) {
        this.workflowOption = workflowOption;
    }

    public boolean isScannerOption() {
        return scannerOption;
    }

    public void setScannerOption(final boolean scannerOption) {
        this.scannerOption = scannerOption;
    }

    public boolean isUploaderOption() {
        return uploaderOption;
    }

    public void setUploaderOption(final boolean uploaderOption) {
        this.uploaderOption = uploaderOption;
    }

    public boolean isRenameOption() {
        return renameOption;
    }

    public void setRenameOption(final boolean renameOption) {
        this.renameOption = renameOption;
    }

    public boolean isMoveOption() {
        return moveOption;
    }

    public void setMoveOption(final boolean moveOption) {
        this.moveOption = moveOption;
    }

    public boolean isCopyOption() {
        return copyOption;
    }

    public void setCopyOption(final boolean copyOption) {
        this.copyOption = copyOption;
    }

    public boolean isAddBookmarkOption() {
        return addBookmarkOption;
    }

    public void setAddBookmarkOption(final boolean addBookmarkOption) {
        this.addBookmarkOption = addBookmarkOption;
    }

    public boolean isSetHomeOption() {
        return setHomeOption;
    }

    public void setSetHomeOption(final boolean setHomeOption) {
        this.setHomeOption = setHomeOption;
    }

    public boolean isExportOption() {
        return exportOption;
    }

    public void setExportOption(final boolean exportOption) {
        this.exportOption = exportOption;
    }

    public boolean isMediaPlayerOption() {
        return mediaPlayerOption;
    }

    public void setMediaPlayerOption(final boolean mediaPlayerOption) {
        this.mediaPlayerOption = mediaPlayerOption;
    }

    public boolean isImageViewerOption() {
        return imageViewerOption;
    }

    public void setImageViewerOption(final boolean imageViewerOption) {
        this.imageViewerOption = imageViewerOption;
    }

    public boolean isGotoFolderOption() {
        return gotoFolderOption;
    }

    public void setGotoFolderOption(final boolean gotoFolderOption) {
        this.gotoFolderOption = gotoFolderOption;
    }

    public boolean isCreateFromTemplateOption() {
        return createFromTemplateOption;
    }

    public void setCreateFromTemplateOption(
            final boolean createFromTemplateOption) {
        this.createFromTemplateOption = createFromTemplateOption;
    }

    public boolean isPurgeOption() {
        return purgeOption;
    }

    public void setPurgeOption(final boolean purgeOption) {
        this.purgeOption = purgeOption;
    }

    public boolean isRestoreOption() {
        return restoreOption;
    }

    public void setRestoreOption(final boolean restoreOption) {
        this.restoreOption = restoreOption;
    }

    public boolean isPurgeTrashOption() {
        return purgeTrashOption;
    }

    public void setPurgeTrashOption(final boolean purgeTrashOption) {
        this.purgeTrashOption = purgeTrashOption;
    }

    public boolean isSendDocumentLinkOption() {
        return sendDocumentLinkOption;
    }

    public void setSendDocumentLinkOption(final boolean sendDocumentLinkOption) {
        this.sendDocumentLinkOption = sendDocumentLinkOption;
    }

    public boolean isSendDocumentAttachmentOption() {
        return sendDocumentAttachmentOption;
    }

    public void setSendDocumentAttachmentOption(
            final boolean sendDocumentAttachmentOption) {
        this.sendDocumentAttachmentOption = sendDocumentAttachmentOption;
    }

    public boolean isSkinOption() {
        return skinOption;
    }

    public void setSkinOption(final boolean skinOption) {
        this.skinOption = skinOption;
    }

    public boolean isDebugOption() {
        return debugOption;
    }

    public void setDebugOption(final boolean debugOption) {
        this.debugOption = debugOption;
    }

    public boolean isAdministrationOption() {
        return administrationOption;
    }

    public void setAdministrationOption(final boolean administrationOption) {
        this.administrationOption = administrationOption;
    }

    public boolean isManageBookmarkOption() {
        return manageBookmarkOption;
    }

    public void setManageBookmarkOption(final boolean manageBookmarkOption) {
        this.manageBookmarkOption = manageBookmarkOption;
    }

    public boolean isHelpOption() {
        return helpOption;
    }

    public void setHelpOption(final boolean helpOption) {
        this.helpOption = helpOption;
    }

    public boolean isDocumentationOption() {
        return documentationOption;
    }

    public void setDocumentationOption(final boolean documentationOption) {
        this.documentationOption = documentationOption;
    }

    public boolean isBugReportOption() {
        return bugReportOption;
    }

    public void setBugReportOption(final boolean bugReportOption) {
        this.bugReportOption = bugReportOption;
    }

    public boolean isSupportRequestOption() {
        return supportRequestOption;
    }

    public void setSupportRequestOption(final boolean supportRequestOption) {
        this.supportRequestOption = supportRequestOption;
    }

    public boolean isPublicForumOption() {
        return publicForumOption;
    }

    public void setPublicForumOption(final boolean publicForumOption) {
        this.publicForumOption = publicForumOption;
    }

    public boolean isVersionChangesOption() {
        return versionChangesOption;
    }

    public void setVersionChangesOption(final boolean versionChangesOption) {
        this.versionChangesOption = versionChangesOption;
    }

    public boolean isProjectWebOption() {
        return projectWebOption;
    }

    public void setProjectWebOption(final boolean projectWebOption) {
        this.projectWebOption = projectWebOption;
    }

    public boolean isAboutOption() {
        return aboutOption;
    }

    public void setAboutOption(final boolean aboutOption) {
        this.aboutOption = aboutOption;
    }

    public boolean isLanguagesOption() {
        return languagesOption;
    }

    public void setLanguagesOption(final boolean languagesOption) {
        this.languagesOption = languagesOption;
    }

    public boolean isPreferencesOption() {
        return preferencesOption;
    }

    public void setPreferencesOption(final boolean preferencesOption) {
        this.preferencesOption = preferencesOption;
    }

    public boolean isFindDocumentOption() {
        return findDocumentOption;
    }

    public void setFindDocumentOption(final boolean findDocumentOption) {
        this.findDocumentOption = findDocumentOption;
    }

    public boolean isAddNoteOption() {
        return addNoteOption;
    }

    public void setAddNoteOption(final boolean addNoteOption) {
        this.addNoteOption = addNoteOption;
    }

    public boolean isAddCategoryOption() {
        return addCategoryOption;
    }

    public void setAddCategoryOption(final boolean addCategoryOption) {
        this.addCategoryOption = addCategoryOption;
    }

    public boolean isAddKeywordOption() {
        return addKeywordOption;
    }

    public void setAddKeywordOption(final boolean addKeywordOption) {
        this.addKeywordOption = addKeywordOption;
    }

    public boolean isRemoveNoteOption() {
        return removeNoteOption;
    }

    public void setRemoveNoteOption(final boolean removeNoteOption) {
        this.removeNoteOption = removeNoteOption;
    }

    public boolean isRemoveCategoryOption() {
        return removeCategoryOption;
    }

    public void setRemoveCategoryOption(final boolean removeCategoryOption) {
        this.removeCategoryOption = removeCategoryOption;
    }

    public boolean isRemoveKeywordOption() {
        return removeKeywordOption;
    }

    public void setRemoveKeywordOption(final boolean removeKeywordOption) {
        this.removeKeywordOption = removeKeywordOption;
    }

    public boolean isMergePdfOption() {
        return mergePdfOption;
    }

    public void setMergePdfOption(final boolean mergeOption) {
        mergePdfOption = mergeOption;
    }
}
