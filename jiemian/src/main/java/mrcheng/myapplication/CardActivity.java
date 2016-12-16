package mrcheng.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.MyUser;
import bean.Request;
import bean.Resopnse;
import bean.emptyConversation;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cardmodel.main.ConnectTask;
import cardmodel.main.ControlTask;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import util.BaseActivity;
import util.Constant;

/**
 * Created by mr.cheng on 2016/10/24.
 */
public class CardActivity extends BaseActivity implements MessageListHandler {
    @InjectView(R.id.connect_status)
    TextView mConnectStatus;
    @InjectView(R.id.card_number)
    TextView mCardNumber;
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.sex)
    TextView mSex;
    @InjectView(R.id.age)
    TextView mAge;
    @InjectView(R.id.medicalNumber)
    TextView mMedicalNumber;
    private ConnectTask connectTask;
    private ControlTask controlTask;
    private static final int XINDIAN_CODE = 102;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BmobIMConversation c;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_card);
        ButterKnife.inject(this);
        initView();
        connectTask = new ConnectTask(mConnectStatus);
        connectTask.execute();
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
                if (controlTask == null) {
                    Toast.makeText(CardActivity.this, "请先查询", Toast.LENGTH_SHORT).show();
                } else {
                    if (controlTask.getUsername() != null && controlTask.getObjectId() != null) {
                        TestMehod(controlTask.getObjectId(), controlTask.getUsername());
                    } else {
                        Toast.makeText(CardActivity.this, "查无此人", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }

    /**
     * 寻卡，读卡号
     */
    private void findcard() {
        if (connectTask != null && connectTask.getSTATU()) {
            controlTask = new ControlTask(CardActivity.this, mCardNumber, connectTask.getInputStream(), connectTask.getOutputStream(),
                    Constant.READ_CARD_CMD, Constant.READ_CARD_ERROR
                    , mAge, mSex, mRealName, mMedicalNumber, mHandler);
            controlTask.execute();
        } else {
            Toast.makeText(CardActivity.this, "请先连接模块，再操作", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 如果已经连接成功则，XXx
     */
    private void reconnectModel() {
        if (!connectTask.isSuccess()) {
            try {
                // 取消任务
                if (connectTask != null && connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mConnectStatus.setTextColor(Color.BLACK);
                    mConnectStatus.setText("连接已经断开，正在重连！");
                    Thread.sleep(500);
                    // 如果Task还在运行，则先取消它
                    connectTask.cancel(true);
                    connectTask = new ConnectTask(mConnectStatus);
                    connectTask.execute();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (connectTask.getSTATU()) {
            Toast.makeText(CardActivity.this, "已经连接上模块了", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.test)
    public void onClick() {
        TestMehod("fd609a48e9", "123");
    }

    private void TestMehod(String objectId, String username) {
        MyUser myUser = new MyUser();
        myUser.setObjectId(objectId);
        myUser.setUsername(username);

        emptyConversation ee = new emptyConversation(myUser);
        BmobIMUserInfo info = ee.getInfo();
        c = BmobIMConversation.obtain(BmobIMClient.getInstance(), BmobIM.getInstance().startPrivateConversation(info, true, null));
        MyUser myUser1 = BmobUser.getCurrentUser(CardActivity.this, MyUser.class);
        Request msg = new Request();
        msg.setContent("1");
        Map<String, Object> map = new HashMap<>();
        map.put("objectId", myUser1.getObjectId());
        map.put("realName", myUser1.getRealName());
        msg.setExtraMap(map);
        c.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                if (e == null) {
                    Toast.makeText(CardActivity.this, "发送请求成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardActivity.this, "发送请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case XINDIAN_CODE:
                if (resultCode == RESULT_OK) {
                    Resopnse resopnse = new Resopnse();
                    resopnse.setContent("1");
                    Map<String, Object> map = new HashMap<>();
                    map.put("cancle", true);
                    resopnse.setExtraMap(map);
                    c.sendMessage(resopnse, new MessageSendListener() {
                        @Override
                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                            Toast.makeText(CardActivity.this, "已结束此次传输", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMessageReceive(List<MessageEvent> list) {
        for (int i = 0; i < list.size(); i++) {
            addMessage2Chat(list.get(i));
        }
    }

    private void addMessage2Chat(MessageEvent event) {
        BmobIMMessage msg = event.getMessage();
        if (msg.getMsgType().equals("response")) {
            if (c != null && c.getConversationId().equals(event.getConversation().getConversationId())) {
                voice(msg);
                return;
            }
        }
    }

    private void voice(BmobIMMessage msg) {
        Resopnse resopnse = Resopnse.convert(msg);
        if (resopnse.getReceive()) {
            Intent intent = new Intent(CardActivity.this, MPAndroidActivity.class);
            intent.putExtra("ObjectId", "fd609a48e9");
            intent.putExtra("username", "123");
//            intent.putExtra("objectId", controlTask.getObjectId());
//            intent.putExtra("username", controlTask.getUsername());
            startActivity(intent);
        } else {
            Toast.makeText(CardActivity.this, "对方拒绝了您的请求", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        BmobIM.getInstance().addMessageListHandler(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        BmobIM.getInstance().removeMessageListHandler(this);
        super.onPause();
    }
}