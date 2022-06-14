package cc.hicore.MiraiHCP;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import cc.hicore.Utils.Utils;

public class CrashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String dumpInfo = getIntent().getStringExtra("dump");
        if (TextUtils.isEmpty(dumpInfo)){
            finish();
            return;
        }
        setContentView(R.layout.crash_notify_info);
        EditText edInfo = findViewById(R.id.Crash_Info);
        edInfo.setKeyListener(null);
        edInfo.setText(dumpInfo);

        findViewById(R.id.Crash_Copy_Info).setOnClickListener(v->{
            Utils.SetTextClipboard(this,dumpInfo);
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
        });

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
