package org.vaadin.applet.sample;

import java.util.Arrays;

import org.vaadin.applet.AppletIntegration;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class AppletIntegrationSampleApplication extends Application {

    private static final long serialVersionUID = 8738850341513839745L;

    @Override
    public void init() {

        Window mainWindow = new Window("Applettest Application");
        setMainWindow(mainWindow);

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
        mainWindow.addComponent(applet);
    }

}