package cc.hicore.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import cc.hicore.MiraiHCP.R;

public class ToastUtils {
    public static void ShowToast(Context context,String ToastText){
        if (!Thread.currentThread().getName().equals("main")) {
            Utils.PostToMain(()->ShowToast(context,ToastText));
            return;
        }
        Toast toast = new Toast(context);

        ViewGroup group = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.toast_custom_view,null);

        TextView text = group.findViewById(R.id.text);
        text.setText(ToastText);

        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(group);
        toast.show();
    }
}
