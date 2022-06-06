package cc.hicore.MiraiHCP.LoginManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.InputType;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;

import cc.hicore.Utils.Utils;

public class LoginSolverDialog {
    public interface SolveResult{
        void onResult(String result);
    }
    public static void onSolveSlideCaptcha(Context context,String Link,SolveResult onResult){
        Utils.PostToMain(()->{
            LinearLayout layout = new LinearLayout(context);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("请进行验证")
                    .setView(layout)
                    .setNegativeButton("终止登录", (dialogInterface, i) -> onResult.onResult(null))
                    .create();
            WebView webView = new WebView(context);
            webView.setWebViewClient(new WebViewClient(){
                @Nullable
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (request.getUrl().toString().startsWith("jsbridge://CAPTCHA/onVerifyCAPTCHA?p=")){
                        try{
                            String requestResult = URLDecoder.decode(request.getUrl().toString(),"UTF-8");
                            int index = requestResult.indexOf("{");
                            int index2 = requestResult.indexOf("}",index);
                            String jsonResult = requestResult.substring(index,index2+1);
                            JSONObject obj = new JSONObject(jsonResult);
                            String ticker = obj.getString("ticket");
                            Utils.PostToMain(()->onResult.onResult(ticker));
                            dialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    return super.shouldInterceptRequest(view, request);
                }
            });
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(Link);

            layout.addView(webView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,Utils.dip2px(context,500)));

            dialog.show();
        });

    }
    public static void onSolvePictureCaptcha(Context context,byte[] picData,SolveResult onResult){
        Utils.PostToMain(()->{
            LinearLayout layout = new LinearLayout(context);
            ImageView image = new ImageView(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            image.setImageDrawable(Drawable.createFromStream(new ByteArrayInputStream(picData),"QRCode"));
            layout.addView(image);

            EditText ed = new EditText(context);
            layout.addView(ed);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("请输入验证码")
                    .setView(layout)
                    .setNegativeButton("终止登录", (dialogInterface, i) -> onResult.onResult(null))
                    .setNeutralButton("确定输入", (dialog1, which) -> onResult.onResult(ed.getText().toString()))
                    .create();


            dialog.show();
        });
    }
    public static void onDeviceProtectCaptcha(Context context,String url,SolveResult onResult){
        Utils.PostToMain(()->{
            LinearLayout mRoot = new LinearLayout(context);
            mRoot.setOrientation(LinearLayout.VERTICAL);
            TextView text = new TextView(context);
            text.setText("你的账号当前设定了登录保护认证,请打开下面的链接进行认证后再继续使用");
            mRoot.addView(text);

            EditText ed = new EditText(context);
            ed.setText(url);
            ed.setInputType(InputType.TYPE_NULL);
            mRoot.addView(ed);

            LinearLayout toolBar = new LinearLayout(context);
            toolBar.setOrientation(LinearLayout.VERTICAL);
            Button btnOpenIn = new Button(context);
            btnOpenIn.setText("软件内打开");
            btnOpenIn.setOnClickListener(v->{
                openInDialog(context,url);
            });
            toolBar.addView(btnOpenIn);

            Button btnOpenOut = new Button(context);
            btnOpenOut.setText("浏览器打开");
            btnOpenOut.setOnClickListener(v->{
                Uri u = Uri.parse(url);
                Intent in = new Intent(Intent.ACTION_VIEW,u);
                context.startActivity(in);
            });
            toolBar.addView(btnOpenOut);

            Button btnCopy = new Button(context);
            btnCopy.setText("复制链接到剪贴板");
            btnCopy.setOnClickListener(v->{
                Utils.SetTextClipboard(context,url);
            });
            toolBar.addView(btnCopy);
            mRoot.addView(toolBar);


            new AlertDialog.Builder(context)
                    .setTitle("设备保护认证")
                    .setCancelable(false)
                    .setView(mRoot)
                    .setNeutralButton("取消认证", (dialog, which) -> {
                        onResult.onResult(null);
                    }).setNegativeButton("我已完成认证", (dialog, which) -> {
                        onResult.onResult("Success");
                    }).show();
        });
    }

    private static void openInDialog(Context context,String URL){
        LinearLayout layout = new LinearLayout(context);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("网页查看")
                .setView(layout)
                .setNegativeButton("关闭", (dialogInterface, i) -> {

                })
                .create();
        WebView webView = new WebView(context);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(URL);

        layout.addView(webView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,Utils.dip2px(context,500)));

        dialog.show();
    }
}
