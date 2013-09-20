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

package com.openkm.frontend.client.widget.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.openkm.frontend.client.Main;
import com.openkm.frontend.client.bean.GWTDashboardDocumentResult;
import com.openkm.frontend.client.bean.GWTDashboardFolderResult;
import com.openkm.frontend.client.bean.GWTDashboardMailResult;
import com.openkm.frontend.client.bean.GWTDocument;
import com.openkm.frontend.client.bean.GWTFolder;
import com.openkm.frontend.client.bean.GWTMail;
import com.openkm.frontend.client.bean.GWTPermission;
import com.openkm.frontend.client.constants.service.RPCService;
import com.openkm.frontend.client.service.OKMDashboardService;
import com.openkm.frontend.client.service.OKMDashboardServiceAsync;
import com.openkm.frontend.client.util.CommonUI;
import com.openkm.frontend.client.util.OKMBundleResources;
import com.openkm.frontend.client.util.Util;

/**
 * DashboardWidget
 * 
 * @author jllort
 *
 */
public class DashboardWidget extends Composite {
    private final OKMDashboardServiceAsync dashboardService = (OKMDashboardServiceAsync) GWT
            .create(OKMDashboardService.class);

    private static int HEADER_SQUARE = 24;

    private static int SEPARATOR_HEIGHT = 20;

    private static int SEPARATOR_WIDTH = 20;

    private VerticalPanel vPanel;

    private SimplePanel spTop;

    private HorizontalPanel hPanel;

    private SimplePanel spLeft;

    private VerticalPanel vCenterPanel;

    private SimplePanel spRight;

    private Header header;

    private SimplePanel panelData;

    private FlexTable table;

    private Image zoomImage;

    private Image viewedImage;

    private Image feedImage;

    private boolean zoom = false;

    private boolean flagZoom = true;

    private List<GWTDashboardDocumentResult> lastDocList = new ArrayList<GWTDashboardDocumentResult>();

    private List<GWTDashboardFolderResult> lastFolderList = new ArrayList<GWTDashboardFolderResult>();

    private List<GWTDashboardMailResult> lastMailList = new ArrayList<GWTDashboardMailResult>();

    private WidgetToFire widgetToFire;

    private String source;

    public Status status;

    private String headerTextKey;

    private String feedUrl = "";

    /**
     * DashboardWidget
     */
    public DashboardWidget(final String source, final String headerTextKey,
            final String iconUrl, final boolean zoom, final String feedUrl) {
        status = new Status();
        status.setStyleName("okm-StatusPopup");

        spTop = new SimplePanel();
        spLeft = new SimplePanel();
        spRight = new SimplePanel();
        panelData = new SimplePanel();
        table = new FlexTable();
        vCenterPanel = new VerticalPanel();
        hPanel = new HorizontalPanel();
        header = new Header(iconUrl, zoom);
        vPanel = new VerticalPanel();
        this.source = source;
        this.headerTextKey = headerTextKey;
        this.feedUrl = feedUrl;

        // Sets or unsets visible table
        table.setVisible(zoom);

        header.setHeaderText(Main.i18n(headerTextKey));

        panelData.add(table);

        vCenterPanel.add(header);
        vCenterPanel.add(panelData);

        hPanel.add(spLeft);
        hPanel.add(vCenterPanel);
        hPanel.add(spRight);

        vPanel.add(spTop);
        vPanel.add(hPanel);

        spTop.setHeight("" + SEPARATOR_HEIGHT);
        spLeft.setWidth("" + SEPARATOR_WIDTH);
        spRight.setWidth("" + SEPARATOR_WIDTH);

        vPanel.setStyleName("okm-DashboardWidget ");
        panelData.setStyleName("data");
        table.setStyleName("okm-NoWrap");

        panelData.setWidth("99.6%");
        header.setWidth("100%");

        table.setCellPadding(0);
        table.setCellSpacing(0);

        vPanel.addStyleName("okm-DisableSelect");

        initWidget(vPanel);
    }

    /**
     * Sets the widget to be fired
     * 
     * @param widgetToFire
     */
    public void setWidgetToFire(final WidgetToFire widgetToFire) {
        this.widgetToFire = widgetToFire;
    }

    /**
     * setHeaderText
     * 
     * @param text
     */
    public void setHeaderText(final String text) {
        header.setHeaderText(text);
    }

    /**
     * setHeaderResults
     * 
     * @param value
     */
    public void setHeaderResults(final int value) {
        header.setHeaderResults(value);
    }

    /**
     * setWidth
     * 
     * @param width
     */
    public void setWidth(final int width) {
        vCenterPanel.setWidth("" + (width - 2 * SEPARATOR_WIDTH));
    }

    /**
     * removeAllRows
     */
    private void removeAllRows() {
        while (table.getRowCount() > 0) {
            table.removeRow(0);
        }
    }

    /**
     * Setting documents
     * 
     * @param docList document list
     */
    public void setDocuments(final List<GWTDashboardDocumentResult> docList) {
        int documentsNotViewed = 0;
        removeAllRows();

        for (final ListIterator<GWTDashboardDocumentResult> it = docList
                .listIterator(); it.hasNext();) {
            final int row = table.getRowCount();
            final GWTDashboardDocumentResult dsDocumentResult = it.next();
            final GWTDocument doc = dsDocumentResult.getDocument();
            final Anchor docName = new Anchor();
            docName.setText(doc.getName());
            docName.setTitle(doc.getPath());
            docName.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (!dsDocumentResult.isVisited()) {
                        markPathAsViewed(doc.getPath());
                    }

                    visiteNode(source, doc.getUuid(),
                            dsDocumentResult.getDate());
                    final String docPath = doc.getPath();
                    final String path = docPath.substring(0,
                            docPath.lastIndexOf("/"));
                    CommonUI.openPath(path, docPath);
                }
            });

            docName.setStyleName("okm-Hyperlink");
            table.setHTML(row, 0, Util.mimeImageHTML(doc.getMimeType()));
            table.setWidget(row, 1, docName);
            final DateTimeFormat dtf = DateTimeFormat.getFormat(Main
                    .i18n("general.date.pattern"));
            table.setHTML(row, 2, dtf.format(dsDocumentResult.getDate()));
            table.getCellFormatter().setWidth(row, 0, "20");
            table.getCellFormatter().setWidth(row, 1, "100%"); // Table sets de 100% of space
            table.getCellFormatter().setHorizontalAlignment(row, 2,
                    HasHorizontalAlignment.ALIGN_RIGHT);
            table.getCellFormatter().setStyleName(row, 2, "okm-NoWrap");

            if (!dsDocumentResult.isVisited()) {
                documentsNotViewed++;
                table.getRowFormatter().setStyleName(row, "okm-NotViewed");
            }
        }

        header.setHeaderNotViewedResults(documentsNotViewed);
        lastDocList = docList; // Saves actual list
    }

    /**
     * Setting folders
     * 
     * @param folderList folder list
     */
    public void setFolders(final List<GWTDashboardFolderResult> folderList) {
        int folderNotViewed = 0;
        removeAllRows();
        for (final ListIterator<GWTDashboardFolderResult> it = folderList
                .listIterator(); it.hasNext();) {
            final int row = table.getRowCount();
            final GWTDashboardFolderResult folderResult = it.next();
            final GWTFolder folder = folderResult.getFolder();

            if ((folder.getPermissions() & GWTPermission.WRITE) == GWTPermission.WRITE) {
                if (folder.isHasChildren()) {
                    table.setHTML(row, 0,
                            Util.imageItemHTML("img/menuitem_childs.gif"));
                } else {
                    table.setHTML(row, 0,
                            Util.imageItemHTML("img/menuitem_empty.gif"));
                }
            } else {
                if (folder.isHasChildren()) {
                    table.setHTML(row, 0,
                            Util.imageItemHTML("img/menuitem_childs_ro.gif"));
                } else {
                    table.setHTML(row, 0,
                            Util.imageItemHTML("img/menuitem_empty_ro.gif"));
                }
            }

            final Anchor folderName = new Anchor();
            folderName.setText(folder.getName());
            folderName.setTitle(folder.getPath());
            folderName.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (!folderResult.isVisited()) {
                        markPathAsViewed(folder.getPath());
                        visiteNode(source, folder.getUuid(),
                                folderResult.getDate());
                    }

                    CommonUI.openPath(folder.getPath(), "");
                }
            });

            folderName.setStyleName("okm-Hyperlink");
            table.setWidget(row, 1, folderName);
            final DateTimeFormat dtf = DateTimeFormat.getFormat(Main
                    .i18n("general.date.pattern"));
            table.setHTML(row, 2, dtf.format(folder.getCreated()));
            table.getCellFormatter().setWidth(row, 0, "20");
            table.getCellFormatter().setWidth(row, 1, "100%"); // Table sets de 100% of space
            table.getCellFormatter().setHorizontalAlignment(row, 2,
                    HasHorizontalAlignment.ALIGN_RIGHT);
            table.getCellFormatter().setStyleName(row, 2, "okm-NoWrap");
            if (!folderResult.isVisited()) {
                folderNotViewed++;
                table.getRowFormatter().setStyleName(row, "okm-NotViewed");
            }
        }

        header.setHeaderNotViewedResults(folderNotViewed);
        lastFolderList = folderList;
    }

    /**
     * Setting mails
     * 
     * @param mailList mail list
     */
    public void setMails(final List<GWTDashboardMailResult> mailList) {
        int documentsNotViewed = 0;
        removeAllRows();

        for (final ListIterator<GWTDashboardMailResult> it = mailList
                .listIterator(); it.hasNext();) {
            final int row = table.getRowCount();
            final GWTDashboardMailResult dsMailResult = it.next();
            final GWTMail mail = dsMailResult.getMail();
            final Anchor mailName = new Anchor();
            mailName.setText(mail.getSubject());
            mailName.setTitle(mail.getPath());
            mailName.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (!dsMailResult.isVisited()) {
                        markPathAsViewed(mail.getPath());
                    }

                    visiteNode(source, mail.getUuid(), dsMailResult.getDate());
                    final String mailPath = mail.getPath();
                    final String path = mailPath.substring(0,
                            mailPath.lastIndexOf("/"));
                    CommonUI.openPath(path, mailPath);
                }
            });

            mailName.setStyleName("okm-Hyperlink");
            table.setHTML(row, 0, Util.mimeImageHTML(mail.getMimeType()));
            table.setWidget(row, 1, mailName);
            final DateTimeFormat dtf = DateTimeFormat.getFormat(Main
                    .i18n("general.date.pattern"));
            table.setHTML(row, 2, dtf.format(dsMailResult.getDate()));
            table.getCellFormatter().setWidth(row, 0, "20");
            table.getCellFormatter().setWidth(row, 1, "100%"); // Table sets de 100% of space
            table.getCellFormatter().setHorizontalAlignment(row, 2,
                    HasHorizontalAlignment.ALIGN_RIGHT);
            table.getCellFormatter().setStyleName(row, 2, "okm-NoWrap");

            if (!dsMailResult.isVisited()) {
                documentsNotViewed++;
                table.getRowFormatter().setStyleName(row, "okm-NotViewed");
            }
        }

        header.setHeaderNotViewedResults(documentsNotViewed);
        lastMailList = mailList; // Saves actual list
    }

    /**
     * Mark all table rows as viewed
     */
    public void markAllRowsAsViewed() {
        int decrement = 0;

        for (int i = 0; i < table.getRowCount(); i++) {
            table.getRowFormatter().removeStyleName(i, "okm-NotViewed");
        }

        for (final ListIterator<GWTDashboardDocumentResult> it = lastDocList
                .listIterator(); it.hasNext();) {
            final GWTDashboardDocumentResult dsDocumentResult = it.next();

            if (!dsDocumentResult.isVisited()) {
                visiteNode(source, dsDocumentResult.getDocument().getUuid(),
                        dsDocumentResult.getDate());
                dsDocumentResult.setVisited(true);
            }
        }

        for (final ListIterator<GWTDashboardFolderResult> it = lastFolderList
                .listIterator(); it.hasNext();) {
            final GWTDashboardFolderResult folderResult = it.next();

            if (!folderResult.isVisited()) {
                visiteNode(source, folderResult.getFolder().getUuid(),
                        folderResult.getDate());
                folderResult.setVisited(true);
            }
        }

        for (final ListIterator<GWTDashboardMailResult> it = lastMailList
                .listIterator(); it.hasNext();) {
            final GWTDashboardMailResult mailResult = it.next();

            if (!mailResult.isVisited()) {
                visiteNode(source, mailResult.getMail().getUuid(),
                        mailResult.getDate());
                mailResult.setVisited(true);
            }
        }

        decrement = header.getNotViewed();
        header.decrementNotViewed(decrement);

        // Refreshing other panels data
        if (widgetToFire != null) {
            widgetToFire.decrementNewDocuments(decrement);
        }
    }

    /**
     * Mark folder or documetn path as viewed
     * 
     * Could be more than one document / folder with same path at the same list ( for example last downloading )
     * 
     * @param widget
     */
    private void markPathAsViewed(final String path) {
        int count = 0;
        int decrement = 0;
        for (final ListIterator<GWTDashboardDocumentResult> it = lastDocList
                .listIterator(); it.hasNext();) {
            final GWTDashboardDocumentResult dsDocumentResult = it.next();
            if (dsDocumentResult.getDocument().getPath().equals(path)) {
                table.getRowFormatter().removeStyleName(count++,
                        "okm-NotViewed");
                decrement++;
                dsDocumentResult.setVisited(true);
            } else {
                count++;
            }
        }

        count = 0;
        for (final ListIterator<GWTDashboardFolderResult> it = lastFolderList
                .listIterator(); it.hasNext();) {
            final GWTDashboardFolderResult dsFolderResult = it.next();
            if (dsFolderResult.getFolder().getPath().equals(path)) {
                table.getRowFormatter().removeStyleName(count++,
                        "okm-NotViewed");
                decrement++;
                dsFolderResult.setVisited(true);
            } else {
                count++;
            }
        }

        count = 0;
        for (final ListIterator<GWTDashboardMailResult> it = lastMailList
                .listIterator(); it.hasNext();) {
            final GWTDashboardMailResult dsMailResult = it.next();
            if (dsMailResult.getMail().getPath().equals(path)) {
                table.getRowFormatter().removeStyleName(count++,
                        "okm-NotViewed");
                decrement++;
                dsMailResult.setVisited(true);
            } else {
                count++;
            }
        }

        header.decrementNotViewed(decrement);

        // Refreshing other panels data
        if (widgetToFire != null) {
            widgetToFire.decrementNewDocuments(decrement);
        }

    }

    /**
     * getNotViewed
     * 
     * @return
     */
    public int getNotViewed() {
        return header.getNotViewed();
    }

    /**
     * Visite a node
     */
    public void visiteNode(final String source, final String node,
            final Date date) {
        dashboardService.visiteNode(source, node, date, callbackVisiteNode);
    }

    /**
     * Callback handler
     */
    final AsyncCallback<Object> callbackVisiteNode = new AsyncCallback<Object>() {
        @Override
        public void onSuccess(final Object result) {
            // This method should call markPathAsViewed
        }

        @Override
        public void onFailure(final Throwable caught) {
            Main.get().showError("visiteNode", caught);
        }
    };

    /**
     * Refreshing language
     */
    public void langRefresh() {
        header.setHeaderText(Main.i18n(headerTextKey));
        header.setHeaderNotViewedResults(header.getNotViewed());
    }

    /**
     * Header
     */
    private class Header extends HorizontalPanel implements HasClickHandlers {
        private SimplePanel spLeft;

        private SimplePanel spRight;

        private SimplePanel iconImagePanel;

        private HorizontalPanel center;

        private HorizontalPanel titlePanel;

        private HTML headerText;

        private HTML headerResults;

        private HTML headerNotViewedResults;

        private int notViewed = 0;

        private Image iconImage;

        /**
         * Header
         */
        public Header(final String iconUrl, final boolean visible) {
            super();
            sinkEvents(Event.ONCLICK);
            iconImage = new Image(iconUrl);
            zoom = visible;

            if (zoom) {
                zoomImage = new Image(OKMBundleResources.INSTANCE.zoomOut());
            } else {
                zoomImage = new Image(OKMBundleResources.INSTANCE.zoomIn());
            }

            zoomImage.setStyleName("okm-Hyperlink");
            viewedImage = new Image(OKMBundleResources.INSTANCE.viewed());
            viewedImage.setStyleName("okm-Hyperlink");

            viewedImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    flagZoom = false;
                    markAllRowsAsViewed();
                }
            });

            feedImage = new Image(OKMBundleResources.INSTANCE.feed());
            feedImage.setStyleName("okm-Hyperlink");

            feedImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    Window.open(RPCService.FeedService + feedUrl, "_blank",
                            null);
                }
            });

            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    if (flagZoom) {
                        zoom = !zoom;
                        table.setVisible(zoom);

                        if (zoom) {
                            zoomImage.setResource(OKMBundleResources.INSTANCE
                                    .zoomOut());
                        } else {
                            zoomImage.setResource(OKMBundleResources.INSTANCE
                                    .zoomIn());
                        }
                    } else {
                        flagZoom = true;
                    }
                }
            });

            setHeight("" + HEADER_SQUARE);

            spLeft = new SimplePanel();
            spRight = new SimplePanel();
            iconImagePanel = new SimplePanel();
            center = new HorizontalPanel();
            titlePanel = new HorizontalPanel();
            headerText = new HTML("");
            headerResults = new HTML("");
            headerNotViewedResults = new HTML("");

            iconImagePanel.add(iconImage);

            titlePanel.add(headerText);
            titlePanel.add(headerResults);

            center.add(iconImagePanel);
            center.add(titlePanel);
            center.add(headerNotViewedResults);
            center.add(feedImage);
            center.add(viewedImage);
            center.add(zoomImage);

            spLeft.setSize("" + HEADER_SQUARE, "" + HEADER_SQUARE);
            center.setWidth("100%");
            center.setCellVerticalAlignment(iconImagePanel,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellHorizontalAlignment(iconImagePanel,
                    HasHorizontalAlignment.ALIGN_LEFT);
            center.setCellHorizontalAlignment(feedImage,
                    HasHorizontalAlignment.ALIGN_LEFT);
            center.setCellHorizontalAlignment(viewedImage,
                    HasHorizontalAlignment.ALIGN_CENTER);
            center.setCellHorizontalAlignment(zoomImage,
                    HasHorizontalAlignment.ALIGN_RIGHT);
            center.setCellVerticalAlignment(titlePanel,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellVerticalAlignment(headerNotViewedResults,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellHorizontalAlignment(headerNotViewedResults,
                    HasHorizontalAlignment.ALIGN_RIGHT);

            center.setCellVerticalAlignment(feedImage,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellVerticalAlignment(viewedImage,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellVerticalAlignment(zoomImage,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            center.setCellWidth(iconImagePanel, "22");
            center.setCellWidth(feedImage, "16");
            center.setCellWidth(viewedImage, "22");
            center.setCellWidth(zoomImage, "16");
            center.setHeight("" + HEADER_SQUARE);
            spRight.setSize("" + HEADER_SQUARE, "" + HEADER_SQUARE);

            titlePanel.setCellVerticalAlignment(headerResults,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            titlePanel.setCellVerticalAlignment(headerNotViewedResults,
                    HasVerticalAlignment.ALIGN_MIDDLE);
            titlePanel.setCellHorizontalAlignment(headerResults,
                    HasHorizontalAlignment.ALIGN_LEFT);
            titlePanel.setCellHorizontalAlignment(headerNotViewedResults,
                    HasHorizontalAlignment.ALIGN_LEFT);

            add(spLeft);
            add(center);
            add(spRight);

            spLeft.setStyleName("topLeft");
            center.setStyleName("topCenter");
            spRight.setStyleName("topRight");

            setCellWidth(spLeft, "" + HEADER_SQUARE);
            setCellWidth(spRight, "" + HEADER_SQUARE);
            setCellVerticalAlignment(center, HasVerticalAlignment.ALIGN_MIDDLE);
        }

        /**
         * setHeaderText
         */
        public void setHeaderText(final String text) {
            headerText.setHTML(text);
        }

        /**
         * setHeaderResults
         */
        public void setHeaderResults(final int value) {
            headerResults.setHTML("&nbsp;&nbsp;(" + value + ")&nbsp;&nbsp;");
        }

        /**
         * setHeaderNotViewedResults
         */
        public void setHeaderNotViewedResults(final int value) {
            notViewed = value;

            if (value > 0) {
                headerNotViewedResults.setHTML("&nbsp;"
                        + Main.i18n("dashboard.new.items") + ":&nbsp;" + value
                        + "&nbsp;&nbsp;");
                titlePanel.setStyleName("okm-NotViewed");
                headerNotViewedResults.setStyleName("okm-NotViewed");
                viewedImage.setResource(OKMBundleResources.INSTANCE.pending());

            } else {
                headerNotViewedResults.setHTML("");
                titlePanel.removeStyleName("okm-NotViewed");
                headerNotViewedResults.removeStyleName("okm-NotViewed");
                viewedImage.setResource(OKMBundleResources.INSTANCE.viewed());
            }
        }

        /**
         * Decrements viewed
         */
        public void decrementNotViewed(final int value) {
            notViewed -= value;
            setHeaderNotViewedResults(notViewed);
        }

        /**
         * getNotViewed
         */
        public int getNotViewed() {
            return notViewed;
        }

        @Override
        public HandlerRegistration addClickHandler(final ClickHandler handler) {
            return addHandler(handler, ClickEvent.getType());
        }
    }

    /**
     * Sets the refreshing
     */
    public void setRefreshing() {
        final int left = getAbsoluteLeft() + getOffsetWidth() / 2;
        final int top = getAbsoluteTop() + getOffsetHeight() / 2;
        status.setFlag_getDashboard();

        if (zoom) {
            status.refresh(left, top);
        }
    }

    /**
     * Unsets the refreshing
     */
    public void unsetRefreshing() {
        status.unsetFlag_getDashboard();
    }
}
