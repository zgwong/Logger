package com.zgwong.android;

import android.support.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by zgwong on 16/8/22.
 * simple log printer
 */
public final class Logger {
    private static final int LIMIT_SIZE = 4000;
    private static final int JSON_INDENT = 2;

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int ASSERT = 6;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT})
    @interface LevelDef {
    }

    private static int logLevel = DEBUG;

    // no instance
    private Logger() {
    }

    public static void setLevel(@LevelDef int level) {
        if (level >= VERBOSE && level <= ASSERT) {
            logLevel = level;
        }
    }

    // v
    public static void v(String tag, String msg, Throwable e) {
        log(VERBOSE, tag, msg, e);
    }

    public static void v(String tag, String msg) {
        log(VERBOSE, tag, msg, null);
    }

    public static void v(String msg, Throwable e) {
        log(VERBOSE, null, msg, e);
    }

    public static void v(String msg) {
        log(VERBOSE, null, msg, null);
    }

    public static void v(Object obj) {
        log(VERBOSE, null, getObjMsg(obj), null);
    }

    // d
    public static void d(String tag, String msg, Throwable e) {
        log(DEBUG, tag, msg, e);
    }

    public static void d(String tag, String msg) {
        log(DEBUG, tag, msg, null);
    }

    public static void d(String msg, Throwable e) {
        log(DEBUG, null, msg, e);
    }

    public static void d(String msg) {
        log(DEBUG, null, msg, null);
    }

    public static void d(Object obj) {
        log(DEBUG, null, getObjMsg(obj), null);
    }

    // i
    public static void i(String tag, String msg, Throwable e) {
        log(INFO, tag, msg, e);
    }

    public static void i(String tag, String msg) {
        log(INFO, tag, msg, null);
    }

    public static void i(String msg, Throwable e) {
        log(INFO, null, msg, e);
    }

    public static void i(String msg) {
        log(INFO, null, msg, null);
    }

    public static void i(Object obj) {
        log(INFO, null, getObjMsg(obj), null);
    }

    // w
    public static void w(String tag, String msg, Throwable e) {
        log(WARN, tag, msg, e);
    }

    public static void w(String tag, String msg) {
        log(WARN, tag, msg, null);
    }

    public static void w(String msg, Throwable e) {
        log(WARN, null, msg, e);
    }

    public static void w(String msg) {
        log(WARN, null, msg, null);
    }

    public static void w(Object obj) {
        log(WARN, null, getObjMsg(obj), null);
    }

    // e
    public static void e(String tag, String msg, Throwable e) {
        log(ERROR, tag, msg, e);
    }

    public static void e(String tag, String msg) {
        log(ERROR, tag, msg, null);
    }

    public static void e(String msg, Throwable e) {
        log(ERROR, null, msg, e);
    }

    public static void e(String msg) {
        log(ERROR, null, msg, null);
    }

    public static void e(Object obj) {
        log(ERROR, null, getObjMsg(obj), null);
    }

    // wtf
    public static void wtf(String tag, String msg, Throwable e) {
        log(ASSERT, tag, msg, e);
    }

    public static void wtf(String tag, String msg) {
        log(ASSERT, tag, msg, null);
    }

    public static void wtf(String msg, Throwable e) {
        log(WARN, null, msg, e);
    }

    public static void wtf(String msg) {
        log(ASSERT, null, msg, null);
    }

    public static void wtf(Object obj) {
        log(ASSERT, null, getObjMsg(obj), null);
    }

    // json
    public static void json(String tag, String json) {
        log(INFO, tag, getJsonMsg(json), null);
    }

    public static void json(String json) {
        log(INFO, null, getJsonMsg(json), null);
    }

    // xml
    public static void xml(String tag, String xml) {
        log(INFO, tag, getXmlMsg(xml), null);
    }

    public static void xml(String xml) {
        log(INFO, null, getXmlMsg(xml), null);
    }

    private static void log(int level, String tag, String msg, Throwable e){
        if (level >= logLevel) {
            StackTraceElement ste = getStackTraceElement();
            if (isEmpty(tag)) {
                tag = ste.getFileName();
            }
            String log = getLogMsg(ste, msg);
            byte[] bytes = new byte[0];
            try {
                bytes = log.getBytes("UTF-8");
                int length = bytes.length;
                if (length <= LIMIT_SIZE) {
                    print(level, tag, log, e);
                } else {
                    for (int i = 0; i < length; i += LIMIT_SIZE) {
                        int count = Math.min(length - i, LIMIT_SIZE);
                        print(level, tag, new String(bytes, i, count, "utf-8"), e);
                    }
                }
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static void print(int level, String tag, String msg, Throwable e) {
        switch (level) {
            case VERBOSE:
                android.util.Log.v(tag, msg, e);
                break;
            case DEBUG:
                android.util.Log.d(tag, msg, e);
                break;
            case INFO:
                android.util.Log.i(tag, msg, e);
                break;
            case WARN:
                android.util.Log.w(tag, msg, e);
                break;
            case ERROR:
                android.util.Log.e(tag, msg, e);
                break;
            case ASSERT:
                android.util.Log.wtf(tag, msg, e);
                break;
            default:
                break;
        }
    }

    private static StackTraceElement getStackTraceElement() {
        StackTraceElement[] stes = Thread.currentThread().getStackTrace();
        for (int i = stes.length; i >= 0; i--) {
            StackTraceElement e = stes[i - 1];
            if (Logger.class.getName().equals(e.getClassName())) {
                return stes[i];
            }
        }
        return null;
    }

    private static String getLogMsg(StackTraceElement ste, String msg) {
        StringBuilder builder = new StringBuilder();
        if (ste != null) {
            builder.append("[(").append(ste.getFileName())
                    .append(":").append(ste.getLineNumber())
                    .append(")#").append(ste.getMethodName())
                    .append("]");
        }
        builder.append(msg);
        return builder.toString();
    }

    private static String getObjMsg(Object obj) {
        if (obj == null) {
            return "Empty/obj is null";
        }
        if (obj.getClass().isArray()) {
            return java.util.Arrays.deepToString((Object[]) obj);
        } else {
            return obj.toString();
        }
    }

    private static String getJsonMsg(String json) {
        String msg = "Invalid Json";
        if (isEmpty(json)) {
            msg = "Empty/Null json content";
        } else {
            try {
                json = json.trim();
                if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    msg = "\n" + jsonObject.toString(JSON_INDENT);
                }
                if (json.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(json);
                    msg = "\n" + jsonArray.toString(JSON_INDENT);
                }
            } catch (JSONException e) {
                // ignore
            }
        }
        return msg;
    }

    private static String getXmlMsg(String xml) {
        String msg = "Invalid xml";
        if (isEmpty(xml)) {
            msg = "Empty/Null xml content";
        } else {
            try {
                Source xmlInput = new StreamSource(new StringReader(xml));
                StreamResult xmlOutput = new StreamResult(new StringWriter());
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.transform(xmlInput, xmlOutput);
                msg = xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
            } catch (TransformerException e) {
                // ignore
            }
        }
        return msg;
    }

    private static boolean isEmpty(String msg) {
        return msg == null || msg.length() == 0;
    }
}
