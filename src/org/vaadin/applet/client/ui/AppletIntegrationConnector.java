/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package org.vaadin.applet.client.ui;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import org.vaadin.applet.AppletIntegration;

import java.util.logging.Logger;

/**
 * @author artamonov
 */
@Connect(AppletIntegration.class)
public class AppletIntegrationConnector extends AbstractComponentConnector implements Paintable {

    protected Logger log = Logger.getLogger("AppletIntegrationConnector");

    @Override
    public VAppletIntegration getWidget() {
        return (VAppletIntegration) super.getWidget();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        log.info("Set applet parameters");

        getWidget().updateFromUIDL(uidl, client);
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        log.info("Applet state changed");

        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("width"))
            getWidget().setWidth(getState().width);

        if (stateChangeEvent.hasPropertyChanged("height"))
            getWidget().setHeight(getState().height);
    }
}