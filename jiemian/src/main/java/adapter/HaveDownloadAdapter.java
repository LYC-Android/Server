package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.List;

import bean.File_Message;
import mrcheng.myapplication.History_DetailActivity;
import mrcheng.myapplication.OutLineActivity;
import mrcheng.myapplication.R;
import util.HaveDownloadFragment;

/**
 * Created by mr.cheng on 2016/9/12.
 */
public class HaveDownloadAdapter extends RecyclerView.Adapter<HaveDownloadAdapter.MyViewHolder> {
    private Context mContext;
    private List<File_Message> mDatas;
    private LinearLayout mNoDownload;

    public HaveDownloadAdapter(Context context, List<File_Message> datas, LinearLayout noDownload) {
        mContext = context;
        mDatas = datas;
        this.mNoDownload=noDownload;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder=new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_have_download,parent,false));

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
    holder.time.setText("上传日期:"+mDatas.get(holder.getLayoutPosition()).getNumber());
        holder.name.setText("姓名:" + mDatas.get(holder.getLayoutPosition()).getName());
        holder.look.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OutLineActivity.class);
                intent.putExtra("ObjectId", mDatas.get(position).getUserObjectId());
                intent.putExtra("path", mDatas.get(position).getFilePath());
                intent.putExtra("time", mDatas.get(position).getNumber());
                mContext.startActivity(intent);

            }
        });
       holder.delete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               File file=new File(mDatas.get(holder.getLayoutPosition()).getFilePath());
               if (file.delete()){
                   DataSupport.delete(File_Message.class, mDatas.get(holder.getLayoutPosition()).getId());
               }else {
                   Toast.makeText(mContext, "不存在此目录", Toast.LENGTH_SHORT).show();
               }
               mDatas.remove(holder.getLayoutPosition());
               notifyItemRemoved(holder.getLayoutPosition());
               checkIsEmpty();

           }
       });
        if (mDatas.get(position).isHaveReport()) {
            holder.baogao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, History_DetailActivity.class);
                    intent.putExtra("objectId", mDatas.get(position).getReportId());
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder.baogao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "暂无报告", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
      TextView name,time;
        Button look,delete,baogao;
        public MyViewHolder(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.name);
            time= (TextView) itemView.findViewById(R.id.time);
            look= (Button) itemView.findViewById(R.id.look);
            delete= (Button) itemView.findViewById(R.id.delete);
            baogao= (Button) itemView.findViewById(R.id.baogao);
        }
    }
    private void checkIsEmpty(){
        if (mDatas.size()>0){
            mNoDownload.setVisibility(View.GONE);
        }else {
            mNoDownload.setVisibility(View.VISIBLE);
        }
    }
}
