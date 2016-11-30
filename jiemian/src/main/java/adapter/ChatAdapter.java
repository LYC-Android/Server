package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import bean.MyUser;
import bean.Sicker;
import mrcheng.myapplication.ChatActivity;
import mrcheng.myapplication.MainActivity;
import mrcheng.myapplication.R;

/**
 * Created by mr.cheng on 2016/9/12.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<MyUser> mDatas;

    public ChatAdapter(Context context, ArrayList<MyUser> datas) {
        mContext = context;
        mDatas = datas;
    }
    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;
    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder=new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.online_item, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
         holder.number.setText("病历号:" + mDatas.get(holder.getLayoutPosition()).getMedicalNumber());
        holder.name.setText("姓名:"+mDatas.get(holder.getLayoutPosition()).getRealName());
        holder.age.setText("年龄:" + mDatas.get(holder.getLayoutPosition()).getAge());
        if (mDatas.get(holder.getLayoutPosition()).getIsBoys()){
            holder.sex.setText("性别:男");
        }else {
            holder.sex.setText("性别:女");
        }
        if (mOnItemClickLitener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });
        }
    }
    public void addData(int position,MyUser myUser) {
        mDatas.add(position, myUser);
        notifyItemInserted(position);
    }

    public void removeData(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView age,number,name,sex;

        public MyViewHolder(View itemView) {
            super(itemView);
            age = (TextView) itemView.findViewById(R.id.online_age);
            number= (TextView)itemView. findViewById(R.id.online_Number);
            sex= (TextView) itemView.findViewById(R.id.online_sex);
            name= (TextView) itemView.findViewById(R.id.online_nickName);

        }
    }
}
