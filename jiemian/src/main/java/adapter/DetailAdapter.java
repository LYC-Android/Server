
package adapter;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bean.File_Message;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import mrcheng.myapplication.OutLineActivity;
import mrcheng.myapplication.R;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> urls;
    private ArrayList<String> times;
    private String name;
    private String mObjectId;
    private static final String TAG = "DetailAdapter";

    public DetailAdapter(Context context, String name, ArrayList<String> times, ArrayList<String> urls, String objectid) {
        mContext = context;
        this.name = name;
        this.times = times;
        this.urls = urls;
        mObjectId = objectid;
    }


    @Override
    public DetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DetailAdapter.ViewHolder holder, final int position) {
        holder.tv.setText("上传时间:" + times.get(position));
        List<File_Message> messages = DataSupport.select("filepath").where("fileurl = ?", urls.get(position)).find(File_Message.class);
        if (messages.size() > 0) holder.cp.setProgress(100);
        holder.cp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.cp.getProgress() == 100) {
                    Intent intent = new Intent(mContext, OutLineActivity.class);
                    List<File_Message> datas = DataSupport.select("filePath").
                            where("fileUrl = ?", urls.get(position))
                            .find(File_Message.class);
                    if (datas.size() > 0) {
                        intent.putExtra("path", datas.get(0).getFilePath());
                        intent.putExtra("ObjectId", mObjectId);
                        intent.putExtra("time", times.get(position));
                        mContext.startActivity(intent);
                    } else {
                        Toast.makeText(mContext, "无次文件", Toast.LENGTH_SHORT).show();
                    }

                } else if (holder.cp.getProgress() == 0 || holder.cp.getProgress() == -1) {
                    final File_Message file_message = new File_Message();
                    BmobFile bmobFile = new BmobFile(name + "_" + times.get(position) + ".txt", "", urls.get(position));
                    File saveFile = new File(mContext.getApplicationContext().getCacheDir() + "/bmob/", bmobFile.getFilename());
                    bmobFile.download(mContext, saveFile, new DownloadFileListener() {
                        @Override
                        public void onSuccess(String s) {
                            holder.cp.setProgress(100);
                            file_message.setUserObjectId(mObjectId);
                            file_message.setFilePath(s);
                            file_message.setFileUrl(urls.get(position));
                            file_message.setName(name);
                            file_message.setNumber(times.get(position));
                            file_message.setHaveReport(false);
                            file_message.save();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            holder.cp.setProgress(-1);
                        }

                        @Override
                        public void onProgress(Integer progress, long total) {
                            holder.cp.setProgress(progress);
                            super.onProgress(progress, total);

                        }
                    });
                } else {
                    Toast.makeText(mContext, "正在下载", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircularProgressButton cp;
        TextView tv;
        CardView cv;

        public ViewHolder(View view) {
            super(view);
            cp = (CircularProgressButton) view.findViewById(R.id.btnWithText);
            tv = (TextView) view.findViewById(R.id.time);
            cv = (CardView) view.findViewById(R.id.cv);

        }
    }
}
