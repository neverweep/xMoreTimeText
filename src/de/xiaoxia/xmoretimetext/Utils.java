package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Utils {
    private static Boolean geminiSupport = null;

    public static boolean geminiSupport() {
        if (geminiSupport != null) return geminiSupport;
        geminiSupport = SystemProp.getBoolean("ro.mediatek.gemini_support", false);
        return geminiSupport;
    }

    static class SystemProp extends Utils {

            private SystemProp() {}

            public static String get(String key) {
                String ret;

                try {
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    ret = (String) callStaticMethod(classSystemProperties, "get", key);
                } catch (Throwable t) {
                    ret = null;
                }
                return ret;
            }

            public static String get(String key, String def) {
                String ret = def;

                try {
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    ret = (String) callStaticMethod(classSystemProperties, "get", key, def);
                } catch (Throwable t) {
                    ret = def;
                }
                return ret;
            }

            public static Integer getInt(String key, Integer def) {
                Integer ret = def;

                try {
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    ret = (Integer) callStaticMethod(classSystemProperties, "getInt", key, def);
                } catch (Throwable t) {
                    ret = def;
                }
                return ret;
            }

            public static Long getLong(String key, Long def) {
                Long ret = def;

                try {
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    ret = (Long) callStaticMethod(classSystemProperties, "getLong", key, def);
                } catch (Throwable t) {
                    ret = def;
                }
                return ret;
            }

            public static Boolean getBoolean(String key, boolean def) {
                Boolean ret = def;

                try {
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    ret = (Boolean) callStaticMethod(classSystemProperties, "getBoolean", key, def);
                } catch (Throwable t) {
                    ret = def;
                }
                return ret;
            }

            public static void set(String key, String val) {
                try{
                    Class<?> classSystemProperties = findClass("android.os.SystemProperties", null);
                    callStaticMethod(classSystemProperties, "set", key, val);
                } catch (Throwable t) {}
            }
        }
}