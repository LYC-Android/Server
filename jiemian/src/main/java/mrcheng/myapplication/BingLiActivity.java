package mrcheng.myapplication;

import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bean.DatabaseInfo;
import bean.File_Message;
import bean.MyUser;
import bean.Report;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.SaveListener;
import util.BaseActivity;

/**
 * 传入RR QTS XINLV 病人的objectId
 */
public class BingLiActivity extends BaseActivity {

    @InjectView(R.id.Realname)
    TextView mRealname;
    @InjectView(R.id.age)
    TextView mAge;
    @InjectView(R.id.sex)
    TextView mSex;
    @InjectView(R.id.createtime)
    TextView mCreatetime;
    @InjectView(R.id.report)
    EditText mReport;
    @InjectView(R.id.xinlv)
    TextView mXinlv;
    @InjectView(R.id.RR)
    TextView mRR;
    @InjectView(R.id.QRS)
    TextView mQRS;
    @InjectView(R.id.xindian_1)
    CheckBox mXindian1;
    @InjectView(R.id.xindian_2)
    CheckBox mXindian2;
    @InjectView(R.id.xindian_3)
    CheckBox mXindian3;
    @InjectView(R.id.xindian_4)
    CheckBox mXindian4;
    @InjectView(R.id.commit_time)
    TextView mCommitTime;
    @InjectView(R.id.bt_submit)
    Button mBtSubmit;
    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    Report report = new Report();
    private static final String TAG = "BingLiActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bing_li);
        ButterKnife.inject(this);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        setTitle("心电报告");
        init();
    }

    private void init() {
        String mObjectId = getIntent().getStringExtra("objectId");
        List<DatabaseInfo> mList = DataSupport.where("objectId = ?", mObjectId).find(DatabaseInfo.class);
        String username;
        if (mList.size() > 0) {
            mRealname.setText("姓名:" + mList.get(0).getRealName());
            mAge.setText("年龄:" + mList.get(0).getAge());
            mSex.setText("性别:" + mList.get(0).getSex());
            username = mList.get(0).getUsername();
        } else {
            mRealname.setText("姓名:" + "--");
            mAge.setText("年龄:" + "--");
            mSex.setText("性别:" + "--");
            username = "";
        }
        report.setUsername(username);

        String name;
        if (getIntent().getStringExtra("time") != null) {
            name = getIntent().getStringExtra("time");
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
            Date date = new Date(System.currentTimeMillis());
            name = format.format(date);
        }
        String createTime = "检查日期:" + name;
        mCreatetime.setText(createTime);
        report.setCreateTime(createTime);

        MyUser myUser = BmobUser.getCurrentUser(BingLiActivity.this, MyUser.class);
        report.setAuthor(myUser.getUsername());

        String finishTime;
        if (myUser.getRealName() != null) {
            finishTime = "报告医生:" + myUser.getRealName() + "    " + "报告时间:" + name;
            mCommitTime.setText(finishTime);
        } else {
            finishTime = "报告医生:" + "" + "    " + "报告时间:" + name;
            mCommitTime.setText(finishTime);
        }
        report.setResultTime(finishTime);

        String xinlv;
        if (getIntent().getStringExtra("xinlv") != null) {
            xinlv = "心率:窦性心率: " + getIntent().getStringExtra("xinlv") + "/min";
            mXinlv.setText(xinlv);
            report.setXinlv(getIntent().getStringExtra("xinlv"));
        } else {
            xinlv = "心率:窦性心率: " + "--/min";
            mXinlv.setText(xinlv);
            report.setXinlv("--");
        }

        String RR;
        if (getIntent().getStringExtra("RR") != null) {
            RR = "心动周期(R-R): " + getIntent().getStringExtra("RR") + "秒";
            mRR.setText(RR);
            report.setRR(getIntent().getStringExtra("RR"));
        } else {
            RR = "心动周期(R-R): " + "--秒";
            mRR.setText(RR);
            report.setRR("--");
        }

        String QRS;
        if (getIntent().getStringExtra("QRS") != null) {
            QRS = "QRS时限:" + getIntent().getStringExtra("QRS") + "秒";
            mQRS.setText(QRS);
            report.setQRS(getIntent().getStringExtra("QRS"));
        } else {
            QRS = "QRS时限:" + "--秒";
            mQRS.setText(QRS);
            report.setQRS("--");
        }
    }

    @OnClick(R.id.bt_submit)
    public void onClick() {
        if (mXindian1.isChecked()) {
            report.setSuggest("在正常范围内");
        } else if (mXindian2.isChecked()) {
            report.setSuggest("大致正常");
        } else if (mXindian3.isChecked()) {
            report.setSuggest("可能不正常");
        } else {
            report.setSuggest("显示不正常");
        }
        report.setMessage(mReport.getText().toString().trim());
        mBtSubmit.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        report.save(BingLiActivity.this, new SaveListener() {
            @Override
            public void onSuccess() {

                if (getIntent().getStringExtra("path") != null) {
                    File_Message file_message = new File_Message();
                    file_message.setHaveReport(true);
                    file_message.setReportId(report.getObjectId());
                    file_message.updateAll("filePath = ?", getIntent().getStringExtra("path"));
                }

                Toast.makeText(BingLiActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                mBtSubmit.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(BingLiActivity.this, "提交失败，请检查网络", Toast.LENGTH_SHORT).show();
                mBtSubmit.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
            }
        });
    }

    @OnClick({R.id.xindian_1, R.id.xindian_2, R.id.xindian_3, R.id.xindian_4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.xindian_1:
                if (mXindian1.isChecked()) {
                    mXindian2.setChecked(false);
                    mXindian3.setChecked(false);
                    mXindian4.setChecked(false);
                } else {
                    mXindian1.setChecked(true);
                }
                break;
            case R.id.xindian_2:
                if (mXindian2.isChecked()) {
                    mXindian1.setChecked(false);
                    mXindian4.setChecked(false);
                    mXindian3.setChecked(false);
                } else {
                    mXindian2.setChecked(true);
                }
                break;
            case R.id.xindian_3:
                if (mXindian3.isChecked()) {
                    mXindian4.setChecked(false);
                    mXindian1.setChecked(false);
                    mXindian2.setChecked(false);
                } else {
                    mXindian3.setChecked(true);
                }
                break;
            case R.id.xindian_4:
                if (mXindian4.isChecked()) {
                    mXindian1.setChecked(false);
                    mXindian2.setChecked(false);
                    mXindian3.setChecked(false);
                } else {
                    mXindian4.setChecked(true);
                }
                break;
        }
    }
}
