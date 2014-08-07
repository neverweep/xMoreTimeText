package de.xiaoxia.xmoretimetext;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class Utils {
    private static Boolean mHasGeminiSupport = null;

    public static boolean hasGeminiSupport() {
        if (mHasGeminiSupport != null) return mHasGeminiSupport;
        mHasGeminiSupport = SystemProp.getBoolean("ro.mediatek.gemini_support", false);
        return mHasGeminiSupport;
    }

    static class SystemProp extends Utils {
        private SystemProp(){}
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
    }
}
