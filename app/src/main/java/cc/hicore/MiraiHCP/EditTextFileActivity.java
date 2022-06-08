package cc.hicore.MiraiHCP;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.ToastUtils;

public class EditTextFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text_file);

        //设置窗口风格为全屏风格
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );


        int type = getIntent().getIntExtra("type",0);
        String path = getIntent().getStringExtra("path");
        if (type == 0 || path == null){
            finish();
            return;
        }
        EditText ed = findViewById(R.id.EditText_EditTextView);
        if (type == 2){
            ed.setKeyListener(null);
            findViewById(R.id.EditText_Button_Save_All).setVisibility(View.GONE);

        }


        ed.setText(FileUtils.ReadFileString(path));
        findViewById(R.id.EditText_Button_Save_All).setOnClickListener(v->{
            FileUtils.WriteToFile(path,ed.getText().toString());
            ToastUtils.ShowToast(this,"已保存");
        });
    }
}