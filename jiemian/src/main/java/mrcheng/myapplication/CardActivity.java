package mrcheng.myapplication;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cardmodel.main.ConnectTask;
import cardmodel.main.ControlTask;
import util.BaseActivity;
import util.Constant;

/**
 * Created by mr.cheng on 2016/10/24.
 */
public class CardActivity extends BaseActivity {
    @InjectView(R.id.connect_status)
    TextView mConnectStatus;
    @InjectView(R.id.card_number)
    TextView mCardNumber;
    @InjectView(R.id.reconnect_model)
    Button mReconnectModel;
    @InjectView(R.id.find_card)
    Button mFindCard;
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.sex)
    TextView mSex;
    @InjectView(R.id.age)
    TextView mAge;
    @InjectView(R.id.medicalNumber)
    TextView mMedicalNumber;
    @InjectView(R.id.send_request)
    Button mSendRequest;
    private ConnectTask connectTask;
    private ControlTask controlTask;
    private Handler mHandler=new Handler(Looper.getMainLooper());
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_card);
        ButterKnife.inject(this);
        initView();
        connectTask=new ConnectTask(mConnectStatus);
        connectTask.execute();
//        startService(new Intent(CardActivity.this,TCPService.class));
    }

    private void initView() {
        mConnectStatus.setText("连接状态:--");
        mCardNumber.setText("卡号:--");
        mRealName.setText("姓名:--");
        mAge.setText("年龄:--");
        mSex.setText("性别:--");
        mMedicalNumber.setText("病历号:--");
    }

    @OnClick({R.id.reconnect_model, R.id.find_card, R.id.send_request})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reconnect_model:
                reconnectModel();
                break;
            case R.id.find_card:
                findcard();
                break;
            case R.id.send_request:
                if (controlTask!=null&&controlTask.getUdpService()!=null){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            controlTask.getUdpService().findSicker();
                        }
                    }).start();
                }
                break;
        }
    }

    /**
     * 寻卡，读卡号
     */
    private void findcard() {
       if (connectTask!=null&&connectTask.getSTATU()){
           controlTask=new ControlTask(CardActivity.this,mCardNumber,connectTask.getInputStream(),connectTask.getOutputStream(),
                   Constant.READ_CARD_CMD,Constant.READ_CARD_ERROR
           ,mAge,mSex,mRealName,mMedicalNumber,mHandler);
           controlTask.execute();
       }else {
           Toast.makeText(CardActivity.this, "请先连接模块，再操作", Toast.LENGTH_SHORT).show();
       }
    }

    /**
     * 如果已经连接成功则，XXx
     */
    private void reconnectModel() {
        if (!connectTask.isSuccess()){
            try {
                // 取消任务
                if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mConnectStatus.setTextColor(Color.BLACK);
                    mConnectStatus.setText("连接已经断开，正在重连！");
                    Thread.sleep(500);
                    // 如果Task还在运行，则先取消它
                    connectTask.cancel(true);
                    connectTask=new ConnectTask(mConnectStatus);
                    connectTask.execute();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if (connectTask.getSTATU()){
            Toast.makeText(CardActivity.this, "已经连接上模块了", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(CardActivity.this, TCPService.class));
    }
}