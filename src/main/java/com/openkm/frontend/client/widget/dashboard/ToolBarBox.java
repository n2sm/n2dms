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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * ToolBarBox
 * 
 * @author jllort
 *
 */
public class ToolBarBox extends HorizontalPanel implements HasClickHandlers,
        HasAllMouseHandlers {

    private VerticalPanel vPanel;

    private Image image;

    private HTML html;

    /**
     * ToolBarBox
     * 
     * @param url
     * @param text
     */
    public ToolBarBox(final Image img, final String text) {
        super();
        sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);

        vPanel = new VerticalPanel();
        final HTML space1 = new HTML("&nbsp;");
        final HTML space2 = new HTML("&nbsp;");
        image = img;
        html = new HTML(text);
        html.setText(text);
        html.setTitle(text);
        image.setTitle(text);

        vPanel.add(image);
        vPanel.add(html);

        add(space1);
        add(vPanel);
        add(space2);

        vPanel.setCellHorizontalAlignment(html,
                HasHorizontalAlignment.ALIGN_CENTER);
        vPanel.setCellHorizontalAlignment(image,
                HasHorizontalAlignment.ALIGN_CENTER);

        setCellVerticalAlignment(vPanel, HasVerticalAlignment.ALIGN_MIDDLE);
        setCellHorizontalAlignment(vPanel, HasHorizontalAlignment.ALIGN_CENTER);

        setCellWidth(space1, "15");
        setCellWidth(space2, "15");

        html.setStyleName("okm-noWrap");

        setHeight("59");
        setWidth("100%");
    }

    /**
     * setLabelText
     * 
     * @param text
     */
    public void setLabelText(final String text) {
        html.setText(text);
        html.setTitle(text);
        image.setTitle(text);
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.google.gwt.event.dom.client.ClickHandler)
     */
    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        return addHandler(handler, ClickEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler(com.google.gwt.event.dom.client.MouseDownHandler)
     */
    @Override
    public HandlerRegistration addMouseDownHandler(
            final MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler(com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    @Override
    public HandlerRegistration addMouseMoveHandler(
            final MouseMoveHandler handler) {
        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    @Override
    public HandlerRegistration addMouseOutHandler(final MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    @Override
    public HandlerRegistration addMouseOverHandler(
            final MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(com.google.gwt.event.dom.client.MouseUpHandler)
     */
    @Override
    public HandlerRegistration addMouseUpHandler(final MouseUpHandler handler) {
        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler(com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    @Override
    public HandlerRegistration addMouseWheelHandler(
            final MouseWheelHandler handler) {
        return addDomHandler(handler, MouseWheelEvent.getType());
    }
}