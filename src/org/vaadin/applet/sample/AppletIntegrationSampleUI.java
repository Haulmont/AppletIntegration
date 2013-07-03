package org.vaadin.applet.sample;

import java.util.Arrays;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.vaadin.applet.AppletIntegration;

public class AppletIntegrationSampleUI extends UI {

    private static final long serialVersionUID = 8738850341513839745L;

    @Override
    protected void init(VaadinRequest request) {
        AppletIntegration applet = new AppletIntegration() {

            private static final long serialVersionUID = 1L;

            @Override
            public void attach() {
                setAppletArchives(Arrays.asList(new String[] { "Othello.jar" }));

                setCodebase("http://www.w3.org/People/mimasa/test/object/java/applets/");
                setAppletClass("Othello.class");
                setWidth("800px");
                setHeight("500px");

            }
        };
        setContent(applet);
    }
}