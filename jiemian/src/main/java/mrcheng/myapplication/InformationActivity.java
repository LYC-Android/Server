package mrcheng.myapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import bean.MyUser;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import util.ActivityCollector;
import util.BaseActivity;

/**
 * Created by mr.cheng on 2016/9/4.
 */
public class InformationActivity extends BaseActivity {
    @InjectView(R.id.username)
    EditText mUsername;
    @InjectView(R.id.phoneNumeber)
    EditText mPhoneNumeber;
    @InjectView(R.id.boy)
    CheckBox mBoy;
    @InjectView(R.id.girl)
    CheckBox mGirl;
    @InjectView(R.id.submit)
    Button mSubmit;
    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    @InjectView(R.id.hosiptal)
    EditText mHosiptal;
    @InjectView(R.id.yishi)
    CheckBox mYishi;
    @InjectView(R.id.zhuzhiyishi)
    CheckBox mZhuzhiyishi;
    @InjectView(R.id.fuzhuren)
    CheckBox mFuzhuren;
    @InjectView(R.id.zhuren)
    CheckBox mZhuren;
    private ProgressDialog progressDialog;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.inject(this);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        MakeProgress();
        setTitle("个人信息");
        getInformation();
    }

    private void MakeProgress() {
        Transition transition = getWindow().getEnterTransition();
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                if (!ActivityCollector.LingYiFlag) {
                    progressDialog = new ProgressDialog(InformationActivity.this);
                    progressDialog.setTitle("正在获取信息");
                    progressDialog.setMessage("请稍候...");
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (mUsername.getText().toString() != null || mHosiptal.getText().toString() != null || mPhoneNumeber.getText().toString() != null) {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    return;
                                }
                            } else {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    private void getInformation() {

        MyUser myUser = BmobUser.getCurrentUser(InformationActivity.this,MyUser.class);
        BmobQuery<MyUser> query = new BmobQuery<>();
        query.addWhereEqualTo("username", myUser.getUsername());
        query.addQueryKeys("username,mobilePhoneNumber,realName,isBoys,medicalNumber,hospital,zhicheng");
        query.findObjects(InformationActivity.this, new FindListener<MyUser>() {
            @Override
            public void onSuccess(List<MyUser> list) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    ActivityCollector.LingYiFlag = true;
                }
                    if (list.get(0).getRealName() != null) {
                        mUsername.setText(list.get(0).getRealName());
                    }

                    if (list.get(0).getIsBoys() != null) {
                        if (list.get(0).getIsBoys()) {
                            mBoy.setChecked(true);
                        } else {
                            mGirl.setChecked(true);
                            mBoy.setChecked(false);
                        }
                    }
                    if (list.get(0).getMobilePhoneNumber() != null) {
                        mPhoneNumeber.setText(list.get(0).getMobilePhoneNumber());
                    }
                    if (list.get(0).getHospital() != null) {
                        mHosiptal.setText(list.get(0).getHospital());
                    }
                    if (list.get(0).getZhicheng() != null) {
                        switch (list.get(0).getZhicheng()) {
                            case "1":
                                mYishi.setChecked(true);
                                break;
                            case "2":
                                mZhuzhiyishi.setChecked(true);
                                break;
                            case "3":
                                mFuzhuren.setChecked(true);
                                break;
                            case "4":
                                mZhuren.setChecked(true);
                                break;
                        }
                    }


            }

            @Override
            public void onError(int i, String s) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    ActivityCollector.LingYiFlag = true;
                }
                Toast.makeText(InformationActivity.this, "查询失败" + s, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void MyUpdateInfo() {
        MyUser myUser = BmobUser.getCurrentUser(InformationActivity.this,MyUser.class);
        if (mPhoneNumeber.getText().toString() == null) {
            Toast.makeText(InformationActivity.this, "电话号码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mUsername.getText().toString() == null) {
            Toast.makeText(InformationActivity.this, "真实姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBoy.isChecked()) {
            myUser.setIsBoys(true);
        } else {
            myUser.setIsBoys(false);
        }
        if (mHosiptal.getText().toString()==null){
            Toast.makeText(InformationActivity.this, "医院不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mYishi.isChecked()) myUser.setZhicheng("1");
        if (mZhuzhiyishi.isChecked()) myUser.setZhicheng("2");
        if (mFuzhuren.isChecked()) myUser.setZhicheng("3");
        if (mZhuren.isChecked()) myUser.setZhicheng("4");
        myUser.setRealName(mUsername.getText().toString());
        myUser.setMobilePhoneNumber(mPhoneNumeber.getText().toString());
        myUser.setHospital(mHosiptal.getText().toString());
        MyUser myUser1 = BmobUser.getCurrentUser(InformationActivity.this,MyUser.class);
        mSubmit.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        myUser.update(InformationActivity.this, myUser1.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(InformationActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                InformationActivity.this.onBackPressed();
                mProgress.setVisibility(View.GONE);
                mSubmit.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(InformationActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                mProgress.setVisibility(View.GONE);
                mSubmit.setVisibility(View.VISIBLE);
            }
        });
    }

    @OnClick({R.id.yishi, R.id.zhuzhiyishi, R.id.fuzhuren, R.id.zhuren, R.id.boy, R.id.girl, R.id.submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yishi:
                if (mYishi.isChecked()) {
                    mZhuren.setChecked(false);
                    mFuzhuren.setChecked(false);
                    mZhuzhiyishi.setChecked(false);

                } else {
                    mYishi.setChecked(true);
                }
                break;
            case R.id.zhuzhiyishi:
                if (mZhuzhiyishi.isChecked()) {
                    mZhuren.setChecked(false);
                    mFuzhuren.setChecked(false);
                    mYishi.setChecked(false);

                } else {
                    mYishi.setChecked(true);
                }
                break;
            case R.id.fuzhuren:
                if (mFuzhuren.isChecked()) {
                    mZhuren.setChecked(false);
                    mZhuzhiyishi.setChecked(false);
                    mYishi.setChecked(false);

                } else {
                    mYishi.setChecked(true);
                }
                break;
            case R.id.zhuren:
                if (mZhuren.isChecked()) {
                    mFuzhuren.setChecked(false);
                    mZhuzhiyishi.setChecked(false);
                    mYishi.setChecked(false);
                } else {
                    mYishi.setChecked(true);
                }

                break;
            case R.id.boy:
                if (mGirl.isChecked()) {
                    mGirl.setChecked(false);
                }
                if (!mBoy.isChecked()) {
                    mGirl.setChecked(true);
                }
                break;
            case R.id.girl:
                if (mBoy.isChecked()) {
                    mBoy.setChecked(false);
                }
                if (!mGirl.isChecked()) {
                    mBoy.setChecked(true);
                }
                break;
            case R.id.submit:
                MyUpdateInfo();
                break;
        }
    }
}