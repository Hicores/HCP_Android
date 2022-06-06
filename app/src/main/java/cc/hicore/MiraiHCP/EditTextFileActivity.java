package cc.hicore.MiraiHCP;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import cc.hicore.Utils.FileUtils;

public class EditTextFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text_file);

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
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        });
    }
}