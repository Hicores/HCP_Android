package cc.hicore.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.text.DecimalFormat;

public class Utils {
    public static void PostToMain(Runnable run){
        new Handler(Looper.getMainLooper()).post(run);
    }
    public static int dip2px(Context context, float dpValue) {
        if (dpValue > 0) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } else {
            float f = -dpValue;
            final float scale = context.getResources().getDisplayMetrics().density;
            return -(int) (f * scale + 0.5f);
        }

    }
    public static void SetTextClipboard(Context context,String str) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("text", str);
        manager.setPrimaryClip(data);
    }
    private static final int GB = 1024 * 1024 * 1024;
    //定义MB的计算常量
    private static final int MB = 1024 * 1024;
    //定义KB的计算常量
    private static final int KB = 1024;

    public static String bytes2kb(long bytes) {
        DecimalFormat format = new DecimalFormat("###.00");
        if (bytes / GB >= 1) {
            return format.format((double) bytes / GB) + "GB";
        } else if (bytes / MB >= 1) {
            return format.format((double) bytes / MB) + "MB";
        } else if (bytes / KB >= 1) {
            return format.format((double) bytes / KB) + "KB";
        } else {
            return bytes + "字节";
        }
    }
}
