package cc.hicore.MiraiHCP;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
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
    }
}
