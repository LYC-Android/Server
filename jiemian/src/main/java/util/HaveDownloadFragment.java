package util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wang.avi.AVLoadingIndicatorView;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;

import adapter.HaveDownloadAdapter;
import bean.File_Message;
import butterknife.ButterKnife;
import butterknife.InjectView;
import mrcheng.myapplication.R;

/**
 * Created by mr.cheng on 2016/9/12.
 */
public class HaveDownloadFragment extends Fragment {
    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @InjectView(R.id.no_download)
    LinearLayout mNoDownload;
    private ArrayList<File_Message> mDatas;
    private HaveDownloadAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =
                inflater.inflate(R.layout.list_fragment, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mDatas = new ArrayList<>();
        mAdapter = new HaveDownloadAdapter(getActivity(), mDatas,mNoDownload);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void getInformation() {
        mDatas.clear();
        mDatas.addAll(DataSupport.findAll(File_Message.class));
        mProgress.hide();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        getInformation();
        checkIsEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
    private void checkIsEmpty(){
        if (mDatas.size()>0){
            mNoDownload.setVisibility(View.GONE);
        }else {
            mNoDownload.setVisibility(View.VISIBLE);
        }
    }
}
