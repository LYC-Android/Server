package util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import adapter.NoDownloadAdapater;
import bean.DowmloadTable;
import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import mrcheng.myapplication.R;

public class NoDownloadFragment extends Fragment {

    @InjectView(R.id.progress)
    AVLoadingIndicatorView mProgress;
    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @InjectView(R.id.no_download)
    LinearLayout mNoDownload;
    private NoDownloadAdapater mAdapater;
    private ArrayList<DowmloadTable> mDatas;

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
        mAdapater = new NoDownloadAdapater(getActivity(), mDatas);
        mRecyclerView.setAdapter(mAdapater);
        getInformation();

    }

    private void getInformation() {
        BmobQuery<DowmloadTable> query = new BmobQuery<DowmloadTable>();
        query.addQueryKeys("author");
        query.include("author[realName|medicalNumber]");
        query.findObjects(getActivity(), new FindListener<DowmloadTable>() {
            @Override
            public void onSuccess(List<DowmloadTable> list) {
                if (list.size() > 0) {
                    mDatas.addAll(list);
                    mAdapater.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "没有数据", Toast.LENGTH_SHORT).show();
                }
                mProgress.hide();
                checkIsEmpty();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
                mProgress.hide();
                checkIsEmpty();
            }
        });
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
