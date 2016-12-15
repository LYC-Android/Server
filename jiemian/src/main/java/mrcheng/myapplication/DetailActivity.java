package mrcheng.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.MenuItem;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import adapter.DetailAdapter;
import bean.DowmloadTable;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.GetListener;
import util.BaseActivity;

/**
 * Created by mr.cheng on 2016/9/11.
 */
public class DetailActivity extends BaseActivity {
    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    private DetailAdapter mAdapter;
    private ArrayList<String> times;
    private ArrayList<String> urls;
    private String title;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.inject(this);
        Intent intent = getIntent();
        getWindow().setEnterTransition(new Slide());
        title = intent.getStringExtra("title");
        String objectid = intent.getStringExtra("ObjectId");
        setTitle(title + "的历史心电");
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
        times = new ArrayList<>();
        urls = new ArrayList<>();
        mAdapter = new DetailAdapter(DetailActivity.this, title, times, urls,getIntent().getStringExtra("UserId"));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        getInformation(objectid);
    }

    private void getInformation(String objectid) {
        BmobQuery<DowmloadTable> query = new BmobQuery<>();
        query.getObject(DetailActivity.this, objectid, new GetListener<DowmloadTable>() {
            @Override
            public void onSuccess(DowmloadTable dowmloadTable) {
                if (dowmloadTable != null) {
                    times.addAll(dowmloadTable.getTimes());
                    urls.addAll(dowmloadTable.getUrls());
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DetailActivity.this, "没有数据", Toast.LENGTH_SHORT).show();
                }
                mProgress.hide();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(DetailActivity.this,s, Toast.LENGTH_SHORT).show();
                mProgress.hide();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }
}