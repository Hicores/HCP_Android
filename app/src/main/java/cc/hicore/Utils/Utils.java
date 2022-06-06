package cc.hicore.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

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
}
