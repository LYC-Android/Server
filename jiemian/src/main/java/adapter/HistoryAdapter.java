package adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import bean.DatabaseInfo;
import bean.Report;
import butterknife.ButterKnife;
import butterknife.InjectView;
import mrcheng.myapplication.History_DetailActivity;
import mrcheng.myapplication.R;

/**
 * Created by mr.cheng on 2016/12/13.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Report> mDatas;

    public HistoryAdapter(Context context, ArrayList<Report> datas) {
        mContext = context;
        mDatas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_have_download, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mDelete.setVisibility(View.GONE);
        holder.mTime.setText("提交时间:" + mDatas.get(position).getCreatedAt());
        List<DatabaseInfo> databaseInfos = DataSupport.select("realName").
                where("username = ?", mDatas.get(position).getUsername()).find(DatabaseInfo.class);
        if (databaseInfos.size() > 0) {
            holder.mName.setText("姓名:" + databaseInfos.get(0).getRealName());
        } else {
            holder.mName.setText("姓名:--");
        }

        holder.mLook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, History_DetailActivity.class);
                intent.putExtra("objectId", mDatas.get(position).getObjectId());
                mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) mContext).toBundle());
            }
        });
        holder.mBaogao.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.name)
        TextView mName;
        @InjectView(R.id.time)
        TextView mTime;
        @InjectView(R.id.look)
        Button mLook;
        @InjectView(R.id.delete)
        Button mDelete;
        @InjectView(R.id.baogao)
        Button mBaogao;
        ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

}
