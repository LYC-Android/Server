package mrcheng.myapplication;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import bean.DatabaseInfo;
import bean.Trasmit;
import butterknife.ButterKnife;
import butterknife.InjectView;
import thread.MyDrawMethod;
import thread.MyDrawThread;
import util.Constant;

public class MainActivity extends Activity implements View.OnClickListener, Runnable, SurfaceHolder.Callback {
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.medicalNumber)
    TextView mMedicalNumber;
    @InjectView(R.id.phoneNumeber)
    TextView mPhoneNumeber;
    @InjectView(R.id.sex)
    TextView mSex;
    @InjectView(R.id.age)
    TextView mAge;
    @InjectView(R.id.battery)
    TextView mBattery;
    @InjectView(R.id.xinlv)
    TextView mXinlv;
    @InjectView(R.id.one)
    TextView mOne;
    @InjectView(R.id.two)
    TextView mTwo;
    @InjectView(R.id.three)
    TextView mThree;
    @InjectView(R.id.four)
    TextView mFour;
    @InjectView(R.id.five)
    TextView mFive;
    @InjectView(R.id.six)
    TextView mSix;
    @InjectView(R.id.seven)
    TextView mSeven;
    @InjectView(R.id.eight)
    TextView mEight;
    @InjectView(R.id.SurfaceView)
    SurfaceView sfv;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private MyDrawMethod myDrawMethod;
    private static final int AnimationTime = 200;
    private LinearLayout top, left, right;
    private Button X_Zhou, Y_Zhou, lvboqi;
    private boolean isMenuShow = true;
    private boolean isX2, isY2, islvbooff;
    ObjectAnimator leftShow, leftDismiss, rightShow, rightDismiss, topShow, topDismiss;
    private int leftMenuWidth, rightMenuWidth, topMenuHeight;
    private List<Float> TempList = new ArrayList<>();
    private List<Float> drawList = new ArrayList<>();
    private MyDrawThread Drawthread;
    private Socket remoteSocket;
    private ObjectInputStream objectInputStream;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();

        ip = getIntent().getStringExtra("ip");
        initAnimation();
        new Thread(this).start();
        sfv.getHolder().addCallback(this);
    }

    @Override
    public void run() {
        try {
            remoteSocket = new Socket(ip, Constant.TCP_PORT);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "连接成功,请耐心等待数据", Toast.LENGTH_SHORT).show();
                }
            });
            objectInputStream = new ObjectInputStream(new BufferedInputStream(remoteSocket.getInputStream()));
            Object object;
            while ((object = objectInputStream.readObject()) != null) {
                final Trasmit trasmit = (Trasmit) object;
                if (!trasmit.getIsOnline()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "该病人已取消连接", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBattery.setText("病人剩余电量 : " + trasmit.getBatteryInfo() + "%");
                    }
                });
                TempList.addAll(trasmit.getDatas());
                synchronized (drawList) {
                    drawList.addAll(TempList);
                    TempList.clear();
                    Drawthread.setHaveDatas(false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (remoteSocket != null) {
                    remoteSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        top.post(new Runnable() {
            @Override
            public void run() {
                topMenuHeight = top.getHeight();
                topShow = ObjectAnimator.ofFloat(top, "translationY", 0);
                topShow.setDuration(AnimationTime);

                topDismiss = ObjectAnimator.ofFloat(top, "translationY", -topMenuHeight);
                topDismiss.setDuration(AnimationTime);
            }
        });
        left.post(new Runnable() {
            @Override
            public void run() {
                leftMenuWidth = left.getWidth();
                leftShow = ObjectAnimator.ofFloat(left, "translationX", 0);
                leftShow.setDuration(AnimationTime);
                leftDismiss = ObjectAnimator.ofFloat(left, "translationX", -leftMenuWidth);
                leftDismiss.setDuration(AnimationTime);
            }
        });
        right.post(new Runnable() {
            @Override
            public void run() {
                rightMenuWidth = right.getWidth();
                rightShow = ObjectAnimator.ofFloat(right, "translationX", 0);
                rightShow.setDuration(AnimationTime);
                rightDismiss = ObjectAnimator.ofFloat(right, "translationX", rightMenuWidth);
                rightDismiss.setDuration(AnimationTime);

            }
        });

    }

    /**
     * 初始化控件
     */
    private void initView() {
        top = (LinearLayout) findViewById(R.id.top_menu);
        left = (LinearLayout) findViewById(R.id.left_menu);
        right = (LinearLayout) findViewById(R.id.right_menu);
        X_Zhou = (Button) findViewById(R.id.X_zhou);
        Y_Zhou = (Button) findViewById(R.id.Y_zhou);
        lvboqi = (Button) findViewById(R.id.shuzilvbo);
        X_Zhou.setOnClickListener(this);
        Y_Zhou.setOnClickListener(this);
        lvboqi.setOnClickListener(this);
        sfv.setOnClickListener(new MyOnlclickListener());

        String mObjectId = getIntent().getStringExtra("objectId");
        List<DatabaseInfo> mList = DataSupport.where("objectId = ?", mObjectId).find(DatabaseInfo.class);
        if (mList.size() > 0) {
            mAge.setText("年龄 : " + mList.get(0).getAge());
            mRealName.setText("姓名 : " + mList.get(0).getRealName());
            mMedicalNumber.setText("病历号 : " + mList.get(0).getMedicalNumber());
            mPhoneNumeber.setText("电话 : " + mList.get(0).getPhoneNumber());
            mSex.setText("性别 : " + mList.get(0).getSex());
        } else {
            Toast.makeText(this, "没有该病人的信息", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关闭菜单
     */
    private void dismissMenu() {
        topDismiss.start();
        leftDismiss.start();
        rightDismiss.start();
        isMenuShow = false;
    }

    /**
     * 显示菜单
     */
    private void showMenu() {
        topShow.start();
        leftShow.start();
        rightShow.start();
        isMenuShow = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.X_zhou:
                if (isX2) {
                    X_Zhou.setText(R.string.x_zhou_x1);
                    isX2 = false;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "X轴x1", Toast.LENGTH_SHORT).show();
                } else {
                    X_Zhou.setText(R.string.x_zhou_x2);
                    isX2 = true;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "X轴x2", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.Y_zhou:
                if (isY2) {
                    Y_Zhou.setText(R.string.y_zhou_x1);
                    isY2 = false;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "Y轴x1", Toast.LENGTH_SHORT).show();
                } else {
                    Y_Zhou.setText(R.string.y_zhou_x2);
                    isY2 = true;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "Y轴x2", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.shuzilvbo:
                if (islvbooff) {
                    lvboqi.setText(R.string.lvbo_on);
                    islvbooff = false;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "数字滤波器开启", Toast.LENGTH_SHORT).show();
                } else {
                    lvboqi.setText(R.string.lvbo_off);
                    islvbooff = true;
                    dismissMenu();
                    Toast.makeText(MainActivity.this, "数字滤波器关闭", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        myDrawMethod = new MyDrawMethod(MainActivity.this, sfv, mOne, mTwo, mThree, mFour,
                mFive, mSix, mSeven, mEight, mXinlv, mHandler);
        Drawthread = new MyDrawThread(myDrawMethod, drawList);
        Drawthread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "destory", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 为SurfaceView建立一个监听用来弹窗的
     */
    class MyOnlclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isMenuShow) {
                dismissMenu();
            } else {
                showMenu();
            }
        }
    }

    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            ExitDialog(MainActivity.this).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 提示退出系统
     *
     * @param context
     * @return
     */
    private Dialog ExitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("系统信息");
        builder.setMessage("确定要退出吗?");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        return builder.create();
    }

}
