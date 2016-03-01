package com.zozo.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 *
 * 获取机器的一些固有属性。
 * Created by lugl1 on 2016/3/1.
 *
 */
public final class DeviceUtil {
    private static final String TAG = "DeviceUtil";

    /**
     * 获取IMEI
     * @param context
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();

        if (TextUtils.isEmpty(imei) ) {
            imei = "";
        }

        return imei;
    }

    /**
     * 获取MAC地址
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        if (TextUtils.isEmpty(mac) ) {
            mac = "";
        }

        return mac;
    }

    /**
     * 获取手机分辨率(ANDROID 版本大于等于13)
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static Point getScreenSize(Context context) {
        if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return  size;
        }
        return null;
    }

    /**
     * 获取手机分辨率（ANDROID 版本小于13）
     *
     *
     * @param activity
     * @return
     */
    public static ScreenSize getScreenSize(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            Display display = activity.getWindowManager().getDefaultDisplay();

            ScreenSize ss = new ScreenSize();
            ss.width = display.getWidth();
            ss.height = display.getHeight();
            return ss;
        }

        return  null;
    }

    public static class ScreenSize {
        public int width, height;
    }

    /**
     * 得到CPU核心数
     *
     * @return CPU核心数
     */
    public static int getNumCores() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });
            return files.length;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * 系统运行的是模式：Dalvik、ART
     */
    public static enum RuntimeValue {
        DALVIK, ART
    }

    /**
     * 获取当前设备运行的是Dalvik，还是ART
     *
     * @param context
     * @return
     */
    public static RuntimeValue getRuntimeValue(Context context) {
        if (getCurrentRuntimeValue().equals("Dalvik")) {
            return RuntimeValue.DALVIK;
        } else if (getCurrentRuntimeValue().contains("ART")) {
            return RuntimeValue.ART;
        } else {
            return null;
        }
    }

    /**
     * 获取手机当前的Runtime
     *
     * @return 正常情况下可能取值Dalvik, ART, ART debug build;
     */
    public static String getCurrentRuntimeValue() {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            try {
                Method get = systemProperties.getMethod("get",
                        String.class, String.class);
                if (get == null) {
                    return "WTF?!";
                }
                try {
                    final String value = (String) get.invoke(
                            systemProperties, "persist.sys.dalvik.vm.lib",
                        /* Assuming default is */"Dalvik");
                    if ("libdvm.so".equals(value)) {
                        return "Dalvik";
                    } else if ("libart.so".equals(value)) {
                        return "ART";
                    } else if ("libartd.so".equals(value)) {
                        return "ART debug build";
                    }

                    return value;
                } catch (IllegalAccessException e) {
                    return "IllegalAccessException";
                } catch (IllegalArgumentException e) {
                    return "IllegalArgumentException";
                } catch (InvocationTargetException e) {
                    return "InvocationTargetException";
                }
            } catch (NoSuchMethodException e) {
                return "SystemProperties.get(String key, String def) method is not found";
            }
        } catch (ClassNotFoundException e) {
            return "SystemProperties class is not found";
        }
    }

    /**
     * 获取设备唯一标识
     * @param context
     * @return
     */
    public static String getUUID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String uniqueId = deviceUuid.toString();
        if (BuildConfig.DEBUG) Log.d(TAG, "uuid=" + uniqueId);

        return uniqueId;
    }

    /**
     * 获取设备运行的最大内存
     *
     * @return 最大内存
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory() / 1024;
    }
}
