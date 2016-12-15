package mrcheng.myapplication;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import adapter.HistoryAdapter;
import bean.MyUser;
import bean.Report;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import util.BaseActivity;

public class HistoryActivity extends BaseActivity {

    @InjectView(R.id.recycle)
    RecyclerView mRecycle;
    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    @InjectView(R.id.no_data)
    LinearLayout mNoData;

    private HistoryAdapter mAdapter;
    private ArrayList<Report> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.inject(this);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        setTitle("历史病历");
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        mRecycle.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new HistoryAdapter(HistoryActivity.this, mDatas);
        mRecycle.setAdapter(mAdapter);
        GetMessage();
    }

    private void GetMessage() {
        BmobQuery<Report> query = new BmobQuery<>();
        query.addQueryKeys("username,createdAt,objectId");
        MyUser myUser = BmobUser.getCurrentUser(HistoryActivity.this, MyUser.class);
        query.addWhereEqualTo("author", myUser.getUsername());
        query.order("-createdAt");
        query.findObjects(HistoryActivity.this, new FindListener<Report>() {
            @Override
            public void onSuccess(List<Report> list) {
                if (list.size() > 0) {
                    mDatas.addAll(list);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mNoData.setVisibility(View.VISIBLE);
                    Toast.makeText(HistoryActivity.this, "没有数据", Toast.LENGTH_SHORT).show();
                }
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError(int i, String s) {
                mNoData.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                Toast.makeText(HistoryActivity.this, "错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                HistoryActivity.this.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }
}
