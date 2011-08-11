package org.vaadin.applet.sample;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class AppletIntegrationSampleApplication extends Application {

    private static final long serialVersionUID = -2720874981487688798L;

    @Override
    public void init() {
        Window mainWindow = new Window("AppletIntegration Sample Application");
        Label label = new Label("Hello Vaadin user");
        mainWindow.addComponent(label);
        setMainWindow(mainWindow);
    }

}
