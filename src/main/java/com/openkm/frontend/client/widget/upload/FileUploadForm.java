/**
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
package com.openkm.frontend.client.widget.upload;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.openkm.frontend.client.constants.service.RPCService;

/**
 * FileUploadForm
 * 
 * @author jllort
 *
 */
public class FileUploadForm extends Composite {

    private FormPanel uploadForm;

    private VerticalPanel vPanel;

    private TextBox inputPath;

    private TextBox inputAction;

    private TextBox inputRenameDocument;

    private CheckBox notifyToUser;

    private CheckBox importZip;

    private TextArea versionComment;

    private TextBox users;

    private TextBox roles;

    private FileUpload fileUpload;

    private TextArea message;

    /**
     * FileUploadForm
     */
    public FileUploadForm(final FileUpload fileUpload, final String size) {
        this.fileUpload = fileUpload;
        fileUpload.setStyleName("okm-Input");
        fileUpload.getElement().setAttribute("size", size);
        // Set the name of the upload file form element
        fileUpload.setName("uploadFormElement");

        uploadForm = new FormPanel();
        vPanel = new VerticalPanel();
        inputPath = new TextBox();
        inputAction = new TextBox();
        inputRenameDocument = new TextBox();
        notifyToUser = new CheckBox();
        importZip = new CheckBox();
        versionComment = new TextArea();
        users = new TextBox();
        roles = new TextBox();
        message = new TextArea();

        // Set Form details
        // Set the action to call on submit
        uploadForm.setAction(RPCService.FileUploadService);
        // Set the form encoding to multipart to indicate a file upload
        uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
        // Set the method to Post
        uploadForm.setMethod(FormPanel.METHOD_POST);

        inputPath.setName("path");
        inputPath.setVisible(false);
        vPanel.add(inputPath);

        inputAction.setName("action");
        inputAction.setVisible(false);
        vPanel.add(inputAction);

        inputRenameDocument.setName("rename");
        inputRenameDocument.setVisible(false);
        vPanel.add(inputRenameDocument);

        notifyToUser.setName("notify");
        notifyToUser.setVisible(false);
        vPanel.add(notifyToUser);

        importZip.setName("importZip");
        importZip.setVisible(false);
        vPanel.add(importZip);

        versionComment.setName("comment");
        versionComment.setVisible(false);
        vPanel.add(versionComment);

        users.setName("users");
        users.setVisible(false);
        vPanel.add(users);

        roles.setName("roles");
        roles.setVisible(false);
        vPanel.add(roles);

        message.setName("message");
        message.setVisible(false);
        vPanel.add(message);

        vPanel.add(fileUpload);

        uploadForm.setWidget(vPanel);

        initWidget(uploadForm);
    }

    /**
     * addSubmitCompleteHandler
     * 
     * @param submitCompleHandler
     */
    public void addSubmitCompleteHandler(
            final SubmitCompleteHandler submitCompleHandler) {
        uploadForm.addSubmitCompleteHandler(submitCompleHandler);
    }

    /**
     * setEncoding
     * 
     * @param encoding
     */
    public void setEncoding(final String encodingType) {
        uploadForm.setEncoding(encodingType);
    }

    /**
     * Set the path
     * @param path String path
     */
    public void setPath(final String path) {
        inputPath.setText(path);
    }

    /**
     * setAction
     * 
     * @param action
     */
    public void setAction(final String action) {
        inputAction.setText(action);
    }

    /**
     * setRename
     * 
     * @param rename
     */
    public void setRename(final String rename) {
        if (rename != null && !rename.equals("")) {
            inputRenameDocument.setText(rename);
        }
    }

    /**
     * setNotifyToUser
     * 
     * @param value
     */
    public void setNotifyToUser(final boolean value) {
        notifyToUser.setValue(value);
    }

    /**
     * isNotifyToUser
     * 
     * @return
     */
    public boolean isNotifyToUser() {
        return notifyToUser.getValue();
    }

    /**
     * setImportZip
     * 
     * @param value
     */
    public void setImportZip(final boolean value) {
        importZip.setValue(value);
    }

    /**
     * isImportZip
     * 
     * @return
     */
    public boolean isImportZip() {
        return importZip.getValue();
    }

    /**
     * setVersionCommnent
     * 
     * @param comment
     */
    public void setVersionCommnent(final String comment) {
        versionComment.setText(comment);
    }

    /**
     * setUsers
     * 
     * @param users
     */
    public void setUsers(final String users) {
        this.users.setText(users);
    }

    /**
     * setRoles
     * 
     * @param roles
     */
    public void setRoles(final String roles) {
        this.roles.setText(roles);
    }

    /**
     * setMessage
     * 
     * @param message
     */
    public void setMessage(final String message) {
        this.message.setText(message);
    }

    /**
     * getFileName
     * 
     * @return
     */
    public String getFileName() {
        return fileUpload.getFilename();
    }

    public void submit() {
        uploadForm.submit();
    }
}
