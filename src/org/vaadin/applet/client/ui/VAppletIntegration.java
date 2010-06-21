package org.vaadin.applet.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

/**
 * Client side applet integration widget which communicates with the server.
 *
 *
 * @author Sami Ekblad
 *
 */
public class VAppletIntegration extends HTML implements Paintable {

    protected static final String PARAM_APP_SESSION = "appSession";
    protected static final String PARAM_APP_URL = "appUrl";
    protected static final String PARAM_APP_DEBUG = "appDebug";
    protected static final String PARAM_PAINTABLE_ID = "paintableId";
    protected static final String PARAM_APPLET_ID = "appletId";

    /** Client-server communication attributes. */
    public static final String ATTR_APPLET_SESSION = "appletSession";
    public static final String ATTR_APPLET_CLASS = "appletClass";
    public static final String ATTR_APPLET_ARCHIVES = "appletArchives";
    public static final String ATTR_APPLET_PARAM_NAMES = "appletParamNames";
    public static final String ATTR_APPLET_PARAM_VALUES = "appletParamValues";
    public static final String ATTR_APPLET_CODEBASE = "appletCodebase";

    public static final String ATTR_CMD = "cmd";
    public static final String ATTR_CMD_PARAMS = "cmdParams";

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-applet";

    /** The client side widget identifier */
    protected String paintableId;

    /** Reference to the server connection object. */
    protected ApplicationConnection client;

    /** Generated applet id. Unique across the application. */
    private String appletId;

    /** Has the applet been initialized. Applet is initialized only once. */
    private boolean appletInitialized;
    private String appletClass;
    private String[] archives = new String[] {};
    private Map<String, String> appletParameters;
    private String appletSession;
    private String height = "0";
    private String width = "0";
    private String codebase;

    /**
     * The constructor should first call super() to initialize the component and
     * then handle any initialization relevant to Vaadin.
     */
    public VAppletIntegration() {

        // The content will be changed in update function
        setHTML("");

        // Temporary applet id. Should not be needed.
        appletId = CLASSNAME;

        // This method call of the Paintable interface sets the component
        // style name in DOM tree
        setStyleName(CLASSNAME);

    }

    /**
     * Called whenever an update is received from the server
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // This call should be made first.
        // It handles sizes, captions, tooltips, etc. automatically.
        if (client.updateComponent(this, uidl, true)) {
            // If client.updateComponent returns true there has been no changes
            // and we do not need to update anything.
            return;
        }

        // Save reference to server connection object to be able to send
        // user interaction later
        this.client = client;

        // Save the client side identifier (paintable id) for the widget
        paintableId = uidl.getId();

        appletId = CLASSNAME + paintableId;

        // Create the Java applet using HTML
        if (!appletInitialized) {

            // Applet class
            if (!uidl.hasAttribute(ATTR_APPLET_CLASS)) {
                ApplicationConnection.getConsole().log(
                        "Missing attribute " + ATTR_APPLET_CLASS);
                return;
            }
            appletClass = uidl.getStringAttribute(ATTR_APPLET_CLASS);

            // Applet session
            if (!uidl.hasAttribute(ATTR_APPLET_SESSION)) {
                ApplicationConnection.getConsole().log(
                        "Missing attribute " + ATTR_APPLET_SESSION);
                return;
            }
            appletSession = uidl.getStringAttribute(ATTR_APPLET_SESSION);

            // Applet archives
            if (!uidl.hasAttribute(ATTR_APPLET_ARCHIVES)) {
                ApplicationConnection.getConsole().log(
                        "Missing attribute " + ATTR_APPLET_ARCHIVES);
                return;
            }

            // Allow overriding of the default codebase
            if (uidl.hasAttribute(ATTR_APPLET_CODEBASE)) {
                codebase = uidl.getStringAttribute(ATTR_APPLET_CODEBASE);
            }

            // Width and height if provided
            if (uidl.hasAttribute("width")) {
                setWidth(uidl.getStringAttribute("width"));
            } else {
                setWidth("0");
            }

            if (uidl.hasAttribute("height")) {
                setHeight(uidl.getStringAttribute("height"));
            } else {
                setHeight("0");
            }

            archives = uidl.getStringArrayAttribute(ATTR_APPLET_ARCHIVES);

            // Applet appletParameters
            appletParameters = getDefaultIntegrationParameters();
            if (uidl.hasAttribute(ATTR_APPLET_PARAM_NAMES)) {
                ValueMap map = uidl.getMapAttribute(ATTR_APPLET_PARAM_NAMES);
                Set<String> keys = map.getKeySet();
                for (String key : keys) {
                    appletParameters.put(key, map.getString(key));
                }
            }

            // Create the HTML
            setHTML(getAppletHTML());
            appletInitialized = true;
        }

        // Execute the command
        String cmd = null;
        String[] cmdParams = null;
        if (uidl.hasAttribute(ATTR_CMD)) {
            cmd = uidl.getStringAttribute(ATTR_CMD);
        }
        if (uidl.hasAttribute(ATTR_CMD_PARAMS)) {
            cmdParams = uidl.getStringArrayAttribute(ATTR_CMD_PARAMS);
        }
        if (cmd != null) {
            execute(cmd, cmdParams);
        }

    }

    /**
     * Execute a command in applet using AbstractVaadinApplet.execute method.
     * Note that this requires that the applet has a method called "execute"
     *
     * @param cmd
     * @param cmdParams
     */
    public void execute(String cmd, String[] cmdParams) {
        ApplicationConnection.getConsole().log(
                "Applet command: " + getAppletId() + ",'" + cmd + "','"
                        + cmdParams + "'");
        if (cmdParams != null && cmdParams.length > 0) {
            internalAppletExecute(getAppletId(), cmd, cmdParams);
        } else {
            internalAppletExecute(getAppletId(), cmd);
        }
    }

    /**
     * This is the internal method that invokes the execute method in applet.
     * Note that this requires that the applet has a method called "execute"
     *
     * @param id
     * @param cmd
     */
    private native void internalAppletExecute(String id, String cmd)
    /*-{
       if ($doc.applets[id]) {
            $doc.applets[id].execute(cmd);
        }
    }-*/;

    /**
     * This is the internal method that invokes the execute method in applet.
     * Note that this requires that the applet has a method called "execute"
     * with string parameters.
     *
     * @param id
     * @param cmd
     * @param cmdParams
     */
    private native void internalAppletExecute(String id, String cmd,
            String[] cmdParams)
    /*-{
       if ($doc.applets[id]) {
            $doc.applets[id].execute(cmd,cmdParams);
        }
    }-*/;

    /**
     * Get paintable id of this widget.
     *
     * @return
     */
    protected String getPaintableId() {
        return paintableId;
    }

    /**
     * Get applet HTML needed to initalize applet.
     *
     * Note: the default implementation does not return anything.
     *
     * @return String containing the APPLET tag needed to initialize the Java
     *         applet.
     */
    protected String getAppletHTML() {

        // Compose dependency JAR files
        List<String> archives = getArchives();
        String archiveAttribute = "";
        if (archives != null) {
            boolean first = true;
            for (String a : archives) {
                if (!first) {
                    archiveAttribute += ",";
                } else {
                    first = false;
                }
                archiveAttribute += a;
            }
        }

        // Compose applet appletParameters
        Map<String, String> appletParams = getAppletParameters();
        String appletParamStr = "";
        if (appletParams != null) {
            for (String name : appletParams.keySet()) {
                appletParamStr += "<param name=\"" + name + "\" value=\""
                        + appletParams.get(name) + "\" />";
            }
        }

        return "<applet mayscript=\"true\" code=\"" + "" + getAppletClass()
                + "" + "\" codebase=\"" + getCodebase()
                + "\" width=\"" + getWidth() + "\" height=\"" + getHeight()
                + "\" id=\"" + getAppletId() + "\" name=\"" + getAppletId()
                + "\" archive=\"" + archiveAttribute + "\">" + appletParamStr
                + "</applet>";
    }

    /** Get codebase of this applet.
     * By default the code base points to GWT.getModuleBaseURL().
     *
     * @return
     */
    private String getCodebase() {
        return codebase == null? GWT.getModuleBaseURL() : codebase;
    }

    protected String getHeight() {
        return height;
    }

    protected String getWidth() {
        return width;
    }

    @Override
    public void setWidth(String w) {
        super.setWidth(w);
        width = w;
    }

    @Override
    public void setHeight(String h) {
        super.setHeight(h);
        height = h;
    }

    /**
     * Get appletParameters for the applet as a Map.
     *
     * @return
     */
    protected Map<String, String> getAppletParameters() {
        return appletParameters;
    }

    /**
     * Get id for this applet.
     *
     * @return
     */
    protected String getAppletId() {
        return appletId;
    }

    /**
     * Get list of archives needed to run the applet.
     *
     * @return
     */
    protected List<String> getArchives() {
        ArrayList<String> res = new ArrayList<String>();
        for (int i = 0; i < archives.length; i++) {
            res.add(archives[i]);
        }
        return res;
    }

    /**
     * Get name of the applet class.
     *
     * @return
     */
    protected String getAppletClass() {
        return appletClass;
    }

    /**
     * Get default appletParameters for the applet.
     *
     * @return
     */
    private Map<String, String> getDefaultIntegrationParameters() {
        Map<String, String> res = new HashMap<String, String>();

        // Add default appletParameters
        res.put(PARAM_APPLET_ID, "" + getAppletId());
        res.put(PARAM_PAINTABLE_ID, "" + getPaintableId());

        String sessionId = appletSession;
        if (sessionId == null) {
            sessionId = Cookies.getCookie("JSESSIONID");
        }
        res.put(PARAM_APP_SESSION, "JSESSIONID=" + sessionId);
        res.put(PARAM_APP_DEBUG, ApplicationConnection.isDebugMode() ? "true"
                : "false");
        res.put(PARAM_APP_URL, GWT.getHostPageBaseURL() + client.getAppUri());

        return res;
    }

}
