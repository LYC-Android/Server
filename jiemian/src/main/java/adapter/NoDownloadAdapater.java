package adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import bean.DowmloadTable;
import mrcheng.myapplication.DetailActivity;
import mrcheng.myapplication.R;

public class NoDownloadAdapater extends RecyclerView.Adapter<NoDownloadAdapater.ViewHolder> {

    private Context mContext;
    private ArrayList<DowmloadTable> mdatas;

    public NoDownloadAdapater(Context context, ArrayList<DowmloadTable> mdatas) {
        mContext = context;
        this.mdatas = mdatas;
    }
    @Override
    public NoDownloadAdapater.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_no_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NoDownloadAdapater.ViewHolder holder, final int position) {
                holder.name.setText("姓名:"+mdatas.get(position).getAuthor().getRealName());
                holder.number.setText("病历号:"+mdatas.get(position).getAuthor().getMedicalNumber());
                holder.message.setText(mdatas.get(position).getUpdatedAt());
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("title",mdatas.get(position).getAuthor().getRealName());
                intent.putExtra("ObjectId",mdatas.get(position).getObjectId());
                intent.putExtra("UserId", mdatas.get(position).getAuthor().getObjectId());
                mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mdatas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
       TextView name,number,message;
        CardView mCardView;

        public ViewHolder(View view) {
            super(view);
            name= (TextView) view.findViewById(R.id.name);
            number= (TextView) view.findViewById(R.id.number);
            message= (TextView) view.findViewById(R.id.message);
            mCardView= (CardView) view.findViewById(R.id.cv);

        }
    }
}
