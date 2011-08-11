package org.vaadin.applet;

import java.applet.Applet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import com.vaadin.terminal.StreamVariable;

/**
 * This class can be used as base to implement Java Applets that integrate to
 * Vaadin application.
 *
 * The class implements thread that polls for JavaScript (GWT) calls of
 * {@link #execute(String)} and {@link #execute(String, Object[])} methods. This
 * allows function privilege elevation if the applet has been signed
 * accordingly. To support this behavior the inheriting applet should implement
 * the {@link #doExecute(String, Object[])} method.
 *
 * Also the class introduces {@link #vaadinSync()} method for syncing the rest
 *
 * @author Sami Ekblad
 *
 */
public abstract class AbstractVaadinApplet extends Applet {

    private static final long serialVersionUID = -1091104541127400420L;

    protected static final String PARAM_APP_SESSION = "appSession";
    protected static final String PARAM_APP_URL = "appUrl";
    protected static final String PARAM_APPLET_ID = "appletId";
    protected static final String PARAM_PAINTABLE_ID = "paintableId";
    protected static final String PARAM_APP_DEBUG = "appDebug";
    protected static final String PARAM_ACTION_URL = "actionUrl";

    protected static long MAX_JS_WAIT_TIME = 10000;

    private boolean debug = false;

    private JsPollerThread pollerThread;

    private Object pollerLock = new Object[] {};

    public boolean runPoller = true;

    private String applicationURL;

    private String sessionCookie;

    private String paintableId;

    private String appletId;

    private String actionUrl;

    @Override
    public void init() {
        setDebug("true".equals(getParameter(PARAM_APP_DEBUG)));
        setAppletId(getParameter(PARAM_APPLET_ID));
        setPaintableId(getParameter(PARAM_PAINTABLE_ID));
        setApplicationURL(getParameter(PARAM_APP_URL));
        setApplicationSessionCookie(getParameter(PARAM_APP_SESSION));
        setAction(getParameter(PARAM_ACTION_URL));

        // Start the poller thread for JS commands
        pollerThread = new JsPollerThread();
        pollerThread.start();
    }

    private void setAction(String submitAction) {
        actionUrl = submitAction;
        debug("actionUrl=" + submitAction);
    }

    /**
     * Get the submit actionUrl.
     *
     * Submit actionUrl can be used to post multipart data
     * back to the Vaadin server-side application.
     *
     * Note: This is not by the AppletIntegration automatically. It must be subclassed and a variable named "actionUrl" must be added to paintContent pointing to the {@link StreamVariable}.
     *
     * @return
     */
    protected String getActionUrl() {
        return actionUrl;
    }

    /**
     * Set the id of the applet in DOM.
     *
     * @param paintableId
     */
    private void setAppletId(String appletId) {
        this.appletId = appletId;
        debug("appletId=" + appletId);
    }

    /**
     * Get the id of the applet in DOM.
     *
     * @return The id of this applet in the Vaadin application DOM document.
     */
    protected String getAppleteId() {
        return appletId;
    }

    /**
     * Set the paintable id of the applet widget.
     *
     * @param paintableId
     *            The id of this applet widget in the Vaadin application.
     */
    private void setPaintableId(String paintableId) {
        this.paintableId = paintableId;
        debug("paintableId=" + paintableId);
    }

    /**
     * Get the paintable id of the applet widget.
     *
     * @return The id of this applet widget in the Vaadin application.
     */
    protected String getPaintableId() {
        return paintableId;
    }

    /**
     * Set the application session cookie. Called from init.
     *
     * @param appUrl
     */
    private void setApplicationSessionCookie(String appSessionCookie) {
        sessionCookie = appSessionCookie;
        debug("sessionCookie=" + sessionCookie);
    }

    /**
     * Get the application session cookie.
     *
     * @return The session cookie needed to communicate back to the Vaadin
     *         application instance.
     */
    protected String getApplicationSessionCookie() {
        return sessionCookie;
    }

    /**
     * Set the application URL. Called from init.
     *
     * @param appUrl
     */
    private void setApplicationURL(String appUrl) {
        applicationURL = appUrl;
        debug("applicationURL=" + applicationURL);
    }

    /**
     * Get the application URL.
     *
     * @return The URL of the Vaadin application.
     */
    protected String getApplicationURL() {
        return applicationURL;
    }

    /**
     * Debug a string if debugging has been enabled.
     *
     * @param string
     */
    protected void debug(String string) {
        if (!isDebug()) {
            return;
        }
        System.err.println("debug: " + string);
    }

    /**
     * Stop the poller and destroy the applet.
     *
     */
    @Override
    public void destroy() {
        runPoller = false;
        super.destroy();
    }

    /**
     * Invokes vaadin.forceSync that synchronizes the client-side GWT
     * application with server. This is an asynchronous method call that returns
     * immediately.
     *
     */
    public void vaadinSync() {
        jsCallAsync("vaadin.forceSync()");
    }

    /**
     * Invokes vaadin.appletUpdateVariable sends a variable to server.
     *
     * @param variableName
     * @param newValue
     * @param immediate
     */
    public void vaadinUpdateVariable(String variableName, boolean newValue,
            boolean immediate) {
        String cmd = "vaadin.appletUpdateBooleanVariable('" + getPaintableId()
                + "','" + variableName + "'," + newValue + "," + immediate
                + ")";
        jsCall(cmd);
    }

    /**
     * Invokes vaadin.appletUpdateVariable sends a variable to server.
     *
     * @param variableName
     * @param newValue
     * @param immediate
     */
    public void vaadinUpdateVariable(String variableName, int newValue,
            boolean immediate) {
        String cmd = "vaadin.appletUpdateIntVariable('" + getPaintableId()
                + "','" + variableName + "'," + newValue + "," + immediate
                + ")";
        jsCall(cmd);
    }

    /**
     * Invokes vaadin.appletUpdateVariable sends a variable to server.
     *
     * @param variableName
     * @param newValue
     * @param immediate
     */
    public void vaadinUpdateVariable(String variableName, double newValue,
            boolean immediate) {
        String cmd = "vaadin.appletUpdateDoubleVariable('" + getPaintableId()
                + "','" + variableName + "'," + newValue + "," + immediate
                + ")";
        jsCall(cmd);
    }

    /**
     * Invokes vaadin.appletUpdateVariable sends a variable to server.
     *
     * @param variableName
     * @param newValue
     * @param immediate
     */
    public void vaadinUpdateVariable(String variableName, String newValue,
            boolean immediate) {
        newValue = escapeJavaScript(newValue);
        String cmd = "vaadin.appletUpdateStringVariable('" + getPaintableId()
                + "','" + variableName + "','" + newValue + "'," + immediate
                + ")";
        jsCall(cmd);
    }

    /*
     * TODO: Variable support missing for: String[], Object[], long, float,
     * Map<String,Object>, Paintable
     */

    /**
     * Helper to call synchronously JavaScript and wrap the InterruptedException
     * to a RuntimeException. If special handling for timeouts is needed the
     * {@link RuntimeException} should be catched.
     */
    private Object jsCall(String cmd) {
        try {
            return jsCallSync(cmd);
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Synchronous JavaScript call timed out.", e);
        }
    }

    /**
     * Execute a JavaScript asynchronously. Note that this return immediately
     * and JavaScript timing problems may occur if called sequentially multiple
     * times.
     *
     * @param command
     */
    public void jsCallAsync(String command) {
        JSCallThread t = new JSCallThread(command);
        t.start();
    }

    /**
     * Execute a JavaScript synchronously.
     *
     * @param command
     * @throws InterruptedException
     */
    public Object jsCallSync(String command) throws InterruptedException {
        JSCallThread t = new JSCallThread(command);
        t.start();
        t.join(MAX_JS_WAIT_TIME);
        return t.getResult();
    }

    /**
     * Thread for polling incoming JavaScript commands. Threading is used to
     * change the call stack. If an applet function is invoked from JavaScript
     * it will always use JavaScript permissions regardless of applet signing.
     *
     * This thread allows commands to be sent to the applet and executed with
     * the applet's privileges.
     *
     * @author Sami Ekblad
     */
    public class JsPollerThread extends Thread {

        private static final long POLLER_DELAY = 100;
        private String jsCommand;
        private Object[] jsParams;

        @Override
        public void run() {
            debug("Poller thread started.");
            while (runPoller) {

                // Check if a command was received
                String cmd = null;
                Object[] params = null;
                synchronized (pollerLock) {
                    if (jsCommand != null) {
                        cmd = jsCommand;
                        params = jsParams;
                        jsCommand = null;
                        jsParams = null;
                        debug("Received JavaScript command '" + cmd + "'");
                    }
                }

                if (cmd != null) {
                    doExecute(cmd, params);
                }

                try {
                    Thread.sleep(POLLER_DELAY);
                } catch (InterruptedException e) {
                }
            }
            debug("Poller thread stopped.");
        }
    }

    /**
     * Thread for executing outgoing JavaScript commands. This thread
     * implementation is used to asynchronously invoke JavaScript commands from
     * applet.
     *
     * @author Sami Ekblad
     *
     */
    public class JSCallThread extends Thread {

        private String command = null;
        private Object result = null;
        private boolean success = false;

        /**
         * Constructor
         *
         * @param command
         *            Complete JavaScript command to be executed including
         */
        public JSCallThread(String command) {
            super();
            // SE: We need to remove all line changes to avoid exceptions
            this.command = command.replaceAll("\n", " ");
        }

        @Override
        public void run() {

            debug("Call JavaScript '" + command + "'");

            String jscmd = command;

            try {
                Method getWindowMethod = null;
                Method evalMethod = null;
                Object jsWin = null;
                Class<?> c = Class.forName("netscape.javascript.JSObject");
                Method ms[] = c.getMethods();
                for (int i = 0; i < ms.length; i++) {
                    if (ms[i].getName().compareTo("getWindow") == 0) {
                        getWindowMethod = ms[i];
                    } else if (ms[i].getName().compareTo("eval") == 0) {
                        evalMethod = ms[i];
                    }

                }

                // Get window of the applet
                jsWin = getWindowMethod.invoke(c,
                        new Object[] { AbstractVaadinApplet.this });

                // Invoke the command
                result = evalMethod.invoke(jsWin, new Object[] { jscmd });

                if (!(result instanceof String) && result != null) {
                    result = result.toString();
                }
                success = true;
                debug("JavaScript result: " + result);
            }

            catch (InvocationTargetException e) {
                success = true;
                result = e;
                debug(e);
            } catch (Exception e) {
                success = true;
                result = e;
                debug(e);
            }
        }

        /**
         * Get result of the execution.
         *
         * @return
         */
        public Object getResult() {
            return result;
        }

        /**
         * Get the result of execution as string.
         *
         * @return
         */
        public String getResultAsString() {
            if (result == null) {
                return null;
            }
            return (String) (result instanceof String ? result : result
                    .toString());
        }

        /**
         * Get the exception that occurred during JavaScript invocation.
         *
         * @return
         */
        public Exception getException() {
            return (Exception) (result instanceof Exception ? result : null);
        }

        /**
         * Check if the JavaScript invocation was an success.
         *
         * @return
         */
        public boolean isSuccess() {
            return success;
        }

    }

    public void setDebug(boolean debug) {
        boolean change = this.debug != debug;
        this.debug = debug;
        if (change) {
            debug("" + isDebug());
        }
    }

    public void debug(Exception e) {
        if (!isDebug()) {
            return;
        }
        System.err.println("debug: Exception " + e);
        e.printStackTrace();
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * Execute method that should be invoked from a JavaScript. This invokes a
     * second thread (with applet's permission) to execute the command.
     *
     * @param command
     * @param params
     */
    public void execute(String command) {
        execute(command, null);
    }

    /**
     * Execute method that should be invoked from a JavaScript. This invokes a
     * second thread (with applet's permission) to execute the command.
     *
     * @param command
     * @param params
     */
    public void execute(String command, Object[] params) {
        if (pollerThread == null) {
            debug("Poller thread stopped. Cannot execute: '" + command + "'");
            return;
        }
        synchronized (pollerLock) {
            pollerThread.jsCommand = command;
            pollerThread.jsParams = params;
        }
    }

    /**
     * Function to to actually execute a specific command.
     *
     * The inheriting applet must implement this to execute commands sent from
     * JavaScript.
     *
     * Implementation may be empty if no JavaScript initiated commands are
     * supported.
     *
     * @param command
     */
    protected abstract void doExecute(String command, Object[] params);

    /*
     * --- Following methods are copied from
     * org.apache.commons.lang.StringEscapeUtils under Apache 2.0 license--
     */

    /**
     * <p>
     * Escapes the characters in a <code>String</code> using JavaScript String
     * rules.
     * </p>
     * <p>
     * Escapes any values it finds into their JavaScript String form. Deals
     * correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     * </p>
     *
     * <p>
     * So a tab becomes the characters <code>'\\'</code> and <code>'t'</code>.
     * </p>
     *
     * <p>
     * The only difference between Java strings and JavaScript strings is that
     * in JavaScript, a single quote must be escaped.
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn\'t say, \"Stop!\"
     * </pre>
     *
     * </p>
     *
     * @param str
     *            String to escape values in, may be null
     * @return String with escaped values, <code>null</code> if null string
     *         input
     */
    public static String escapeJavaScript(String str) {
        if (str == null) {
            return null;
        }

        StringBuffer writer = new StringBuffer(str.length() * 2);

        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                writer.append("\\u");
                writer.append(hex(ch));
            } else if (ch > 0xff) {
                writer.append("\\u0");
                writer.append(hex(ch));
            } else if (ch > 0x7f) {
                writer.append("\\u00");
                writer.append(hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                case '\b':
                    writer.append('\\');
                    writer.append('b');
                    break;
                case '\n':
                    writer.append('\\');
                    writer.append('n');
                    break;
                case '\t':
                    writer.append('\\');
                    writer.append('t');
                    break;
                case '\f':
                    writer.append('\\');
                    writer.append('f');
                    break;
                case '\r':
                    writer.append('\\');
                    writer.append('r');
                    break;
                default:
                    if (ch > 0xf) {
                        writer.append("\\u00");
                        writer.append(hex(ch));
                    } else {
                        writer.append("\\u000");
                        writer.append(hex(ch));
                    }
                    break;
                }
            } else {
                switch (ch) {
                case '\'':
                    // If we wanted to escape for Java strings then we would
                    // not need this next line.
                    writer.append('\\');
                    writer.append('\'');
                    break;
                case '"':
                    writer.append('\\');
                    writer.append('"');
                    break;
                case '\\':
                    writer.append('\\');
                    writer.append('\\');
                    break;
                default:
                    writer.append(ch);
                    break;
                }
            }
        }

        return writer.toString();
    }

    /**
     * <p>
     * Returns an upper case hexadecimal <code>String</code> for the given
     * character.
     * </p>
     *
     * @param ch
     *            The character to convert.
     * @return An upper case hexadecimal <code>String</code>
     */
    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
}