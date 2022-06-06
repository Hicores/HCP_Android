package cc.hicore.MiraiHCP;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        registerTabEvent();
        registerSetButtonEvent();
    }

    private void add_new_account_click(){

    }
    private void add_new_plugin_click(){

    }
    //注册Tab栏切换事件
    private void registerTabEvent(){
        TextView account_tab = findViewById(R.id.Tab_Account_List);
        TextView plugin_tab = findViewById(R.id.Tab_Plugin);
        TextView set_tab = findViewById(R.id.Tab_Set);

        View account_view = findViewById(R.id.Main_Account_View);
        View plugin_view = findViewById(R.id.Main_Plugin_View);
        View set_view = findViewById(R.id.Main_Set_View);

        View add_new_button = findViewById(R.id.Main_Add_New_Button);

        AtomicBoolean isNewAccount = new AtomicBoolean();

        account_tab.setOnClickListener(v->{
            account_view.setVisibility(View.VISIBLE);
            plugin_view.setVisibility(View.GONE);
            set_view.setVisibility(View.GONE);

            add_new_button.setVisibility(View.VISIBLE);
            isNewAccount.getAndSet(true);

            account_tab.setBackgroundColor(Color.parseColor("#aa9999"));
            plugin_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            set_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));

        });

        plugin_tab.setOnClickListener(v->{
            account_view.setVisibility(View.GONE);
            plugin_view.setVisibility(View.VISIBLE);
            set_view.setVisibility(View.GONE);

            add_new_button.setVisibility(View.VISIBLE);
            isNewAccount.getAndSet(false);

            account_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            plugin_tab.setBackgroundColor(Color.parseColor("#aa9999"));
            set_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));

        });

        set_tab.setOnClickListener(v->{
            account_view.setVisibility(View.GONE);
            plugin_view.setVisibility(View.GONE);
            set_view.setVisibility(View.VISIBLE);

            add_new_button.setVisibility(View.GONE);

            account_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            plugin_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            set_tab.setBackgroundColor(Color.parseColor("#aa9999"));

        });

        add_new_button.setOnClickListener(v->{
            if (isNewAccount.get()){
                add_new_account_click();
            }else {
                add_new_plugin_click();
            }
        });
    }
    private void registerSetButtonEvent(){

    }
}