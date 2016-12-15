package util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import bean.DialogWrapper;
import bean.Request;
import bean.Resopnse;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;
import mrcheng.myapplication.MainActivity;

/**
 * Created by mr.cheng on 2016/9/14.
 */
public class BaseActivity extends AppCompatActivity {
//    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mContext=BaseActivity.this;
        ActivityCollector.addActivity(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        BmobNotificationManager.getInstance(this).cancelNotification();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    @Subscribe
    public void onEventMainThread(DialogWrapper event) {
//          processCustomMessage(event.getMsg(),event.getInfo());
    }
//    private void processCustomMessage(BmobIMMessage msg, BmobIMUserInfo info) {
//        String type = msg.getMsgType();
//        if (type.equals("request")){
//            //发给谁，就填谁的用户信息
//            //启动一个暂态会话，也就是isTransient为true,表明该会话仅执行发送消息的操作，不会保存会话和消息到本地数据库中，
//            final BmobIMConversation c = BmobIM.getInstance().startPrivateConversation(info,true,null);
//            //这个obtain方法才是真正创建一个管理消息发送的会话
//            final BmobIMConversation conversation = BmobIMConversation.obtain(BmobIMClient.getInstance(),c);
//            Request request=Request.convert(msg);
//            final String mObjecid=msg.getConversationId();
//            String name=request.getRealName();
//            AlertDialog YesOrNodialog = new AlertDialog.Builder(mContext).create();
//            YesOrNodialog.setTitle(name+"请求与你连接");
//            YesOrNodialog.setCancelable(false);
//            YesOrNodialog.setButton(DialogInterface.BUTTON_POSITIVE, "接受", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Resopnse resopnse = new Resopnse();
//                    resopnse.setContent("1");
//                    Map<String, Object> map = new HashMap<String, Object>();
//                    map.put("receive", true);
//                    resopnse.setExtraMap(map);
//                    conversation.sendMessage(resopnse, new MessageSendListener() {
//                        @Override
//                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
//                            if (e == null) {
//                                Intent intent = new Intent(mContext, MainActivity.class);
//                                intent.putExtra("objectId", mObjecid);
//                                (mContext).startActivity(intent);
//                            } else {
//                                Toast.makeText(mContext, "发送请求失败", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//            });
//            YesOrNodialog.setButton(DialogInterface.BUTTON_NEGATIVE, "拒绝", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Resopnse resopnse = new Resopnse();
//                    resopnse.setContent("1");
//                    Map<String, Object> map = new HashMap<String, Object>();
//                    map.put("receive", false);
//                    resopnse.setExtraMap(map);
//                    conversation.sendMessage(resopnse, new MessageSendListener() {
//                        @Override
//                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
//                            if (e == null) {
//                                Toast.makeText(mContext, "拒绝了连接", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(mContext, "发送请求失败", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//            });
//            YesOrNodialog.show();
//        }
//
//    }
}
