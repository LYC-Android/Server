package mrcheng.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import bean.MyUser;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobUser;

/**
 * Created by mr.cheng on 2016/10/29.
 */
public class LuancherActivity extends Activity {

    @InjectView(R.id.imageView)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_laucher);
        ButterKnife.inject(this);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
        alphaAnimation.setDuration(1500);
        mImageView.setAnimation(alphaAnimation);
        alphaAnimation.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                goLoginActivity();
            }
        }, 2000);
    }

    private void goLoginActivity() {
        final MyUser myUser = BmobUser.getCurrentUser(LuancherActivity.this, MyUser.class);
        if (myUser == null) {
            startActivity(new Intent(LuancherActivity.this, LoginActivity.class));
            finish();
        } else {
            startActivity(new Intent(LuancherActivity.this, ChatActivity.class));
            finish();
        }
    }
}
