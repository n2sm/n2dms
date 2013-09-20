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

package com.openkm.frontend.client.widget.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.AbstractScrollTable.ResizePolicy;
import com.google.gwt.gen2.table.client.AbstractScrollTable.ScrollPolicy;
import com.google.gwt.gen2.table.client.AbstractScrollTable.ScrollTableImages;
import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
import com.google.gwt.gen2.table.client.FixedWidthGrid;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.SelectionGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.openkm.frontend.client.Main;
import com.openkm.frontend.client.bean.GWTGrantedUser;
import com.openkm.frontend.client.bean.GWTPermission;
import com.openkm.frontend.client.bean.GWTUser;
import com.openkm.frontend.client.service.OKMAuthService;
import com.openkm.frontend.client.service.OKMAuthServiceAsync;
import com.openkm.frontend.client.util.RoleComparator;
import com.openkm.frontend.client.util.Util;

/**
 * SecurityScrollTable
 * 
 * @author jllort
 *
 */
public class SecurityScrollTable extends Composite implements ClickHandler {
    private final OKMAuthServiceAsync authService = (OKMAuthServiceAsync) GWT
            .create(OKMAuthService.class);

    // Number of columns
    private String path;

    private ScrollTable table;

    private FixedWidthFlexTable headerTable;

    private FixedWidthGrid dataTable;

    private Button button;

    private String withPermission = "img/icon/security/yes.gif";

    private String withoutPermission = "img/icon/security/no.gif";

    private int userRow = 0;

    private int rolRow = 0;

    private int numberOfColumns = 0;

    /**
     * SecurityScrollTable
     */
    public SecurityScrollTable() {
        final ScrollTableImages scrollTableImages = new ScrollTableImages() {
            @Override
            public AbstractImagePrototype scrollTableAscending() {
                return new AbstractImagePrototype() {
                    @Override
                    public void applyTo(final Image image) {
                        image.setUrl("img/sort_asc.gif");
                    }

                    @Override
                    public Image createImage() {
                        return new Image("img/sort_asc.gif");
                    }

                    @Override
                    public String getHTML() {
                        return "<img border=\"0\" src=\"img/sort_asc.gif\"/>";
                    }
                };
            }

            @Override
            public AbstractImagePrototype scrollTableDescending() {
                return new AbstractImagePrototype() {
                    @Override
                    public void applyTo(final Image image) {
                        image.setUrl("img/sort_desc.gif");
                    }

                    @Override
                    public Image createImage() {
                        return new Image("img/sort_desc.gif");
                    }

                    @Override
                    public String getHTML() {
                        return "<img border=\"0\" src=\"img/sort_desc.gif\"/>";
                    }
                };
            }

            @Override
            public AbstractImagePrototype scrollTableFillWidth() {
                return new AbstractImagePrototype() {
                    @Override
                    public void applyTo(final Image image) {
                        image.setUrl("img/fill_width.gif");
                    }

                    @Override
                    public Image createImage() {
                        return new Image("img/fill_width.gif");
                    }

                    @Override
                    public String getHTML() {
                        return "<img border=\"0\" src=\"img/fill_width.gif\"/>";
                    }
                };
            }
        };

        headerTable = new FixedWidthFlexTable();
        dataTable = new FixedWidthGrid();

        table = new ScrollTable(dataTable, headerTable, scrollTableImages);
        table.setCellSpacing(0);
        table.setCellPadding(2);
        table.setSize("540", "140");

        button = new Button(Main.i18n("button.update"), this);
        button.setStyleName("okm-ChangeButton");

        initSecurity();

        initWidget(table);
    }

    /**
     * initSecurity
     */
    public void initSecurity() {
        int col = 0;
        table.setColumnWidth(col, 110);
        table.setPreferredColumnWidth(col++, 110);
        table.setColumnWidth(col, 90);
        table.setPreferredColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);

        table.setColumnWidth(col, 80);
        table.setColumnSortable(col++, false);
        table.setColumnWidth(col, 110);
        table.setPreferredColumnWidth(col++, 110);
        table.setColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);
        table.setColumnWidth(col++, 90);

        // Level 1 headers		
        col = 0;
        headerTable.setHTML(0, col++, Main.i18n("security.role.name"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.read"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.write"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.delete"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.security"));

        headerTable.setWidget(0, col, button);
        headerTable.getCellFormatter().setHorizontalAlignment(0, col,
                HasHorizontalAlignment.ALIGN_CENTER);
        headerTable.getCellFormatter().setVerticalAlignment(0, col++,
                HasVerticalAlignment.ALIGN_MIDDLE);
        headerTable.setHTML(0, col++, Main.i18n("security.user.name"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.read"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.write"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.delete"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.security"));

        numberOfColumns = col; // Setting the number of columns

        // Table data
        dataTable.setSelectionPolicy(SelectionGrid.SelectionPolicy.ONE_ROW);
        table.setResizePolicy(ResizePolicy.UNCONSTRAINED);
        table.setScrollPolicy(ScrollPolicy.BOTH);

        headerTable.addStyleName("okm-DisableSelect");
        dataTable.addStyleName("okm-DisableSelect");
    }

    /**
     * Sets the document or folder ID
     * 
     * @param path The document or folder ID
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Lang refresh
     */
    public void langRefresh() {
        int col = 0;
        headerTable.setHTML(0, col++, Main.i18n("security.role.name"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.read"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.write"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.delete"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.role.permission.security"));

        button.setText(Main.i18n("button.update"));
        col++; // Button column
        headerTable.setHTML(0, col++, Main.i18n("security.user.name"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.read"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.write"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.delete"));
        headerTable.setHTML(0, col++,
                Main.i18n("security.user.permission.security"));
    }

    /**
     * Removes all rows except the first
     */
    private void removeAllRows() {
        userRow = 0;
        rolRow = 0;
        // Purge all rows except first
        while (dataTable.getRowCount() > 0) {
            dataTable.removeRow(0);
        }
        dataTable.resize(0, numberOfColumns);
    }

    /**
     * Adds a new user row
     * 
     * @param userName The user name value
     * @param permission The permission value
     */
    private void addUserRow(final GWTUser user, final Integer permission) {
        final int rows = userRow++;

        if (dataTable.getRowCount() <= rows) {
            dataTable.insertRow(rows);
        }

        int col = 6;

        dataTable.setHTML(rows, col++, user.getUsername());

        if ((permission & GWTPermission.READ) == GWTPermission.READ) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.WRITE) == GWTPermission.WRITE) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.DELETE) == GWTPermission.DELETE) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.SECURITY) == GWTPermission.SECURITY) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);
    }

    /**
     * Adds a new group row
     * 
     * @param groupName The group value name
     * @param permission The permission value
     */
    private void addRolRow(final String groupName, final Integer permission) {
        final int rows = rolRow++;

        if (dataTable.getRowCount() <= rows) {
            dataTable.insertRow(rows);
        }

        int col = 0;
        dataTable.setHTML(rows, col++, groupName);

        if ((permission & GWTPermission.READ) == GWTPermission.READ) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col, "O");
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.WRITE) == GWTPermission.WRITE) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.DELETE) == GWTPermission.DELETE) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);

        if ((permission & GWTPermission.SECURITY) == GWTPermission.SECURITY) {
            dataTable
                    .setHTML(rows, col, Util.imageItemHTML(withPermission, ""));
        } else {
            dataTable.setHTML(rows, col,
                    Util.imageItemHTML(withoutPermission, ""));
        }
        dataTable.getCellFormatter().setHorizontalAlignment(rows, col++,
                HasHorizontalAlignment.ALIGN_CENTER);
    }

    /**
     * Call back get granted roles
     */
    final AsyncCallback<Map<String, Integer>> callbackGetGrantedRoles = new AsyncCallback<Map<String, Integer>>() {
        @Override
        public void onSuccess(final Map<String, Integer> result) {
            final List<String> rolesList = new ArrayList<String>();

            // Ordering grant roles to list
            for (final String string : result.keySet()) {
                rolesList.add(string);
            }
            Collections.sort(rolesList, RoleComparator.getInstance());

            for (final String groupName : rolesList) {
                final Integer permission = result.get(groupName);
                addRolRow(groupName, permission);
            }

            Main.get().mainPanel.desktop.browser.tabMultiple.status
                    .unsetRoleSecurity();
        }

        @Override
        public void onFailure(final Throwable caught) {
            Main.get().mainPanel.desktop.browser.tabMultiple.status
                    .unsetRoleSecurity();
            Main.get().showError("GetGrantedRoles", caught);
        }
    };

    /**
     * Gets the granted users
     */
    private void getGrantedUsers() {
        if (path != null) {
            Main.get().mainPanel.desktop.browser.tabMultiple.status
                    .setUserSecurity();
            authService.getGrantedUsers(path,
                    new AsyncCallback<List<GWTGrantedUser>>() {
                        @Override
                        public void onSuccess(final List<GWTGrantedUser> result) {
                            for (final GWTGrantedUser gu : result) {
                                addUserRow(gu.getUser(), gu.getPermissions());
                            }

                            Main.get().mainPanel.desktop.browser.tabMultiple.status
                                    .unsetUserSecurity();
                        }

                        @Override
                        public void onFailure(final Throwable caught) {
                            Main.get().mainPanel.desktop.browser.tabMultiple.status
                                    .unsetUserSecurity();
                            Main.get().showError("GetGrantedUsers", caught);
                        }
                    });
        }
    }

    /**
     * Gets the granted roles
     */
    private void getGrantedRoles() {
        removeAllRows();

        if (path != null) {
            Main.get().mainPanel.desktop.browser.tabMultiple.status
                    .setRoleSecurity();
            authService.getGrantedRoles(path, callbackGetGrantedRoles);
        }
    }

    /**
     * Sets visibility to buttons ( true / false )
     * 
     * @param visible The visible value
     */
    public void setVisibleButtons(final boolean visible) {
        button.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @Override
    public void onClick(final ClickEvent event) {
        Main.get().securityPopup.show(path);
    }

    /**
     * Sets the change permission
     * 
     * @param permission The permission value
     */
    public void setChangePermision(final boolean permission) {
        button.setEnabled(permission);
    }

    /**
     * Get grants
     */
    public void GetGrants() {
        removeAllRows();
        getGrantedUsers();
        getGrantedRoles();
    }

    /**
     * fillWidth
     */
    public void fillWidth() {
        table.fillWidth();
    }
}