package org.vaadin.applet.client.ui;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.Paintable;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import org.vaadin.applet.AppletIntegration;

/**
 * @author artamonov
 * @version $Id$
 */
@Connect(AppletIntegration.class)
public class AppletIntegrationConnector extends AbstractComponentConnector implements Paintable {

    @Override
    public VAppletIntegration getWidget() {
        return (VAppletIntegration) super.getWidget();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        getWidget().updateFromUIDL(uidl, client);
    }
}