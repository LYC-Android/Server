package mrcheng.myapplication;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.DatabaseInfo;
import bean.MyUser;
import bean.Resopnse;
import bean.emptyConversation;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.ValueEventListener;
import util.Caculate;
import util.MyXFormatter;
import util.ValueObject;

/**
 * Created by mr.cheng on 2016/11/30.
 */
public class MPAndroidActivity extends Activity {
    @InjectView(R.id.chart1)
    LineChart mChart;
    @InjectView(R.id.xinlv)
    TextView mXinlv;
    @InjectView(R.id.battery)
    TextView mBattery;
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.age)
    TextView mAge;
    private Thread drawThread;
    BmobRealTimeData rtd = new BmobRealTimeData();
    private int index;
    private static final String TAG = "MPAndroidActivity";
    private int N0;
    private float N1;
    private XAxis xl;
    private int addNum;
    private int M;
    private ArrayList<Integer> resultX = new ArrayList<>();
    private ArrayList<Double> resultY = new ArrayList<>();
    private boolean Stop;
    private ILineDataSet set;
    private String objcetdId;
    private String lock = new String("");
    private ArrayList<Double> Result = new ArrayList<>();
    private int counter;
    private int DrawCounter;
    private int[] xinlvdatas;
    private BmobIMConversation c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpandroid);
        ButterKnife.inject(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager wm = (WindowManager)
                getSystemService(Context.WINDOW_SERVICE);
        int mScreenWidth = wm.getDefaultDisplay().getWidth();
        int mScreenHeight = wm.getDefaultDisplay().getHeight();
        float xdpi = getResources().getDisplayMetrics().xdpi;
        Caculate caculate = new Caculate(xdpi, mScreenWidth, mScreenHeight);
        N0 = caculate.getN0();
        N1 = caculate.getN1();
        addNum = caculate.getAddNum();
        M = caculate.getM();
        ValueObject.value = "";
        objcetdId = getIntent().getStringExtra("ObjectId");
        List<DatabaseInfo> databaseInfos = DataSupport.select("age", "realName")
                .where("objectId = ?", objcetdId).find(DatabaseInfo.class);
        if (databaseInfos.size() > 0) {
            mAge.setText("年龄:" + databaseInfos.get(0).getAge());
            mRealName.setText("姓名:" + databaseInfos.get(0).getRealName());
        } else {
            mAge.setText("年龄:--");
            mRealName.setText("姓名:--");
        }
        MyUser myUser = new MyUser();
        myUser.setObjectId(objcetdId);
        myUser.setUsername(getIntent().getStringExtra("username"));
        emptyConversation ee = new emptyConversation(myUser);
        BmobIMUserInfo info = ee.getInfo();
        c = BmobIMConversation.obtain(BmobIMClient.getInstance(), BmobIM.getInstance().startPrivateConversation(info, true, null));
        connect();
        initView();
    }

    /**
     * 设置MPAndroid的一些属性
     */
    private void initView() {
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setBackgroundColor(Color.WHITE);
        LineData data = new LineData();
        mChart.setData(data);
        mChart.setExtraRightOffset(50);
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        xl = mChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setGranularity(1f);
        xl.setLabelCount(9, true);
        xl.setTextSize(16);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xl.setAvoidFirstLastClipping(true);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setInverted(false);
        leftAxis.setEnabled(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(752f);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        feedMultiple();
    }

    /**
     * 使用Socket进行连接
     */
    private void connect() {
        rtd.start(MPAndroidActivity.this, new ValueEventListener() {
            @Override
            public void onConnectCompleted() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MPAndroidActivity.this, "连接成功,请耐心等待数据", Toast.LENGTH_SHORT).show();
                    }
                });
                if (rtd.isConnected()) {
                    rtd.subTableUpdate("Trasmit");
                }
            }
            private List<Double> TempList = new ArrayList<>();
            private List<Double> mDoubls = new ArrayList<>();
            private List<Double> mDatas = new ArrayList<>();
            @Override
            public void onDataChange(JSONObject jsonObject) {
                try {
                    JSONObject object = jsonObject.getJSONObject("data");
                    if (!object.getBoolean("isOnline")) {
                        rtd.unsubTableUpdate("Trasmit");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MPAndroidActivity.this, "该病人已经取消连接", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Stop = true;
                        return;
                    }
                    if (object.getString("batteryInfo") != null) {
                        mBattery.setText("病人剩余电量 : " + object.getString("batteryInfo") + "%");
                    } else {
                        mBattery.setText("病人剩余电量 : --");
                    }
                    JSONArray array = object.getJSONArray("datas");
                    for (int i = 0; i < array.length(); i++) {
                        TempList.add( array.getDouble(i)*1000/1.46);
                    }

                    //拿到4000长度的Doule
                    if (TempList.size() == 4000) {
                        synchronized (lock) {
                            if (!ValueObject.value.equals("")) {
                               lock.wait();
                            }
                            ArrayList<Double> XH = new ArrayList<>();
                            Result.clear();
                            resultX.clear();
                            resultY.clear();
                            for (int i = 0; i < 4000; i++) {
                                mDoubls.add(TempList.get(i));
                                if (mDoubls.size() >= 500) {//Modified 每8秒的数据量为500*8=4000，要画出150*8=1200点

                                    for (int h = 0; h < mDoubls.size(); h++) {
                                        mDatas.add(mDoubls.get(h));
                                        if (h > 0 && h % addNum == 0) {//Modified 每5点插值一次使得8秒数据量达到4800点
                                            mDatas.add((mDoubls.get(h - 1) + mDoubls.get(h + 1)) / 2);
                                        }
                                    }
                                    mDatas.add(mDoubls.get((500) - 1));
                                    for (int l = 0; l < mDatas.size(); l++) {
                                        if (l % M == 0) {//Modified 然后4倍抽取使得8秒画图点数为1200点
                                            XH.add(mDatas.get(l));
                                            Result.add((double) (N0 - (float) (mDatas.get(l) * N1)));
                                        }
                                    }
                                    mDoubls.clear();
                                    mDatas.clear();
                                }
                            }
                            TempList.clear();
                            xinlvdatas = CaculateXinLv(XH);
                            for (int i = 0; i < resultX.size(); i++) {
                                resultY.add(Result.get(resultX.get(i)));
                            }
                            ValueObject.value = "11";
                            lock.notify();
                        }
                    }
                    TempList.clear();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 获得信息
     */
    private void feedMultiple() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addEntry();
            }
        };
        drawThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!Stop) {
                    try {
                        synchronized (lock) {
                            if (ValueObject.value.equals("")) {
                                lock.wait();
                            }
                            index = 0;
                            counter = 0;
                            DrawCounter++;
                            for (int i = 0; i < Result.size(); i++) {
                                runOnUiThread(runnable);
                                try {
                                    Thread.sleep(8000 / 1200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            ValueObject.value = "";
                            lock.notify();


                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        xl.setValueFormatter(new MyXFormatter(0));
        drawThread.start();
    }

    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {
            set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if (set1 == null) {
                set1 = createSe1t();
                data.addDataSet(set1);
            }
            data.addEntry(new Entry(set.getEntryCount(), (float) (Result.get(index) + 0f)), 0);
            if (counter < resultX.size()) {
                if (set.getEntryCount() == (resultX.get(counter) + (DrawCounter - 1) * 1200)) {
                    data.addEntry(new Entry(set.getEntryCount(), (float) (resultY.get(counter) + 0f)), 1);
                    counter++;
                }
            }

            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(1200);
            mChart.setVisibleXRangeMinimum(1200);
            mChart.moveViewToX(data.getEntryCount());
            index++;
            if (index % (1200/xinlvdatas.length) == 0) {
                mXinlv.setText("心率:" + xinlvdatas[(index / (1200/xinlvdatas.length)) - 1] + "/min");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (drawThread != null) {
            drawThread.interrupt();
        }
        if (rtd!=null&&rtd.isConnected()) {
            rtd.unsubTableUpdate("Trasmit");
        }
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);
        set.setDrawCircleHole(false);
        set.setDrawHighlightIndicators(false);
        set.setLineWidth(3f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(Color.BLACK);
        return set;
    }
    private LineDataSet createSe1t() {
        LineDataSet set = new LineDataSet(null, null);
        set.setDrawCircleHole(false);
        set.setDrawHighlightIndicators(false);
        set.setLineWidth(0.2f);
        set.setCircleColor(Color.RED);
        set.setCircleRadius(4f);
        set.setDrawValues(false);
        set.setColor(Color.WHITE);
        return set;
    }
    /**
     * 捕捉back
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            ExitDialog(MPAndroidActivity.this).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    private Dialog ExitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("确定要退出吗?");
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                        Stop = true;
                        finish();
                    }
                });
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        return builder.create();
    }


    private int[] CaculateXinLv(ArrayList<Double> drawList) {
        int size = 1200;
        double[] mFloats = new double[size];
        double[] mDaoshu = new double[size];
        for (int i = 0; i < size; i++) {
            mFloats[i] = drawList.get(i);
        }

        mDaoshu[0] = mDaoshu[1] = mDaoshu[size - 1] = mDaoshu[size - 2] = 0;
        for (int i = 2; i < mFloats.length - 2; i++) {
            mDaoshu[i] = mFloats[i + 1] - mFloats[i - 1] + 2 * (mFloats[i + 2] - mFloats[i - 2]);
        }
        //最大导数
        double max_daoshu = 0;
        for (int i = 0; i < mDaoshu.length; i++) {
            if (max_daoshu < mDaoshu[i]) {
                max_daoshu = mDaoshu[i];
            }
        }
        //存储点数的R波数组
        ArrayList<Integer> dianshuX = new ArrayList<>();
        double threshold = (max_daoshu * 0.12);
        for (int i = 1; i < mFloats.length - 1; i++) {
            if (Math.abs(mFloats[i]) > threshold && ((mDaoshu[i] * mDaoshu[i + 1]) < 0 || ((mDaoshu[i] * mDaoshu[i - 1]) < 0))) {
                dianshuX.add(i);
                if (dianshuX.size() > 1) {
                    if ((dianshuX.get(dianshuX.size() - 1) - dianshuX.get(dianshuX.size() - 2)) < 10) {
                        if (Math.abs(drawList.get(dianshuX.get(dianshuX.size() - 1))) > Math.abs(dianshuX.get(dianshuX.size() - 2))) {
                            dianshuX.remove(dianshuX.size() - 2);
                        } else {
                            dianshuX.remove(dianshuX.size() - 1);
                        }
                    }
                }
            }
        }
        //计算斜线部分，局部变换法
        double[] dianshuY = new double[dianshuX.size()];
        for (int i = 0; i < dianshuY.length; i++) {
            dianshuY[i] = drawList.get(dianshuX.get(i));
        }


        CaculateQRSWave(drawList, dianshuX, dianshuY);


        int[] result1 = new int[dianshuX.size() - 1];
        for (int i = 0; i < dianshuX.size() - 1; i++) {
            result1[i] = dianshuX.get(i + 1) - dianshuX.get(i);
        }

        int fs = 150;
        int[] result2 = new int[result1.length];
        for (int i = 0; i < result1.length; i++) {
            result2[i] = (60 * fs) / result1[i];
        }
        //输出结果result2【】
        return result2;
    }

    /**
     * 计算QRS波的方法
     *
     * @param drawList
     * @param dianshuX
     * @param dianshuY
     */
    private void CaculateQRSWave(ArrayList<Double> drawList, ArrayList<Integer> dianshuX, double[] dianshuY) {
        if (dianshuX.get(0) < 13) {
            //第一个点小于40的时候，忽略第一个点
            resultX.add(dianshuX.get(0));
            double firstBack15Y = drawList.get(dianshuX.get(0) + 14);
            double firstTan = (dianshuY[0] - firstBack15Y) / 15;
            double[] firstBackTempDouble = new double[13];
            for (int j = 0; j < 13; j++) {
                firstBackTempDouble[j] = Math.abs((firstTan * (13 - j)) + firstBack15Y - drawList.get(dianshuX.get(0) + j + 1));
            }
            int firtMaxIndex = 0;
            double firstMax = firstBackTempDouble[0];
            for (int h = 0; h < firstBackTempDouble.length - 1; h++) {
                if (firstMax < firstBackTempDouble[h + 1]) {
                    firstMax = firstBackTempDouble[h + 1];
                    firtMaxIndex = h + 1;
                }
            }
            int FirstbackTempXresult = dianshuX.get(0) + firtMaxIndex;

            int FirstsecondIndex = 0;
            double FirstsecondMin = Math.abs(drawList.get(FirstbackTempXresult + 1) - drawList.get(FirstbackTempXresult + 2));
            for (int j = 0; j < 13 - firtMaxIndex; j++) {
                if (FirstsecondMin > (Math.abs(drawList.get(FirstbackTempXresult + j + 2) - drawList.get(FirstbackTempXresult + j + 3)))) {
                    FirstsecondIndex = j + 1;
                    FirstsecondMin = Math.abs(drawList.get(FirstbackTempXresult + j + 2) - drawList.get(FirstbackTempXresult + j + 3));
                }
            }
            resultX.add(FirstbackTempXresult + FirstsecondIndex + 1);
            //*************************
            double[] foward13Y = new double[dianshuX.size() - 1];
            for (int i = 1; i < dianshuX.size(); i++) {
                foward13Y[i-1] = drawList.get(dianshuX.get(i) - 12);
            }
            for (int i = 1; i < foward13Y.length; i++) {
                double tan = (dianshuY[i] - foward13Y[i-1]) / 12;
                double[] tempResult = new double[11];
                for (int j = 0; j < 11; j++) {
                    tempResult[j] = Math.abs((tan * (j + 1)) + foward13Y[i-1] - drawList.get(dianshuX.get(i) - 11 + j));
                }

                int MaxIndex = 0;
                double max = tempResult[0];
                for (int h = 0; h < tempResult.length - 1; h++) {
                    if (max < tempResult[h + 1]) {
                        max = tempResult[h + 1];
                        MaxIndex = h + 1;
                    }
                }
                int tempXresult = dianshuX.get(i) - 11 + MaxIndex;

                int secondIndex = 0;
                double secondMin = Math.abs(drawList.get(tempXresult - 1) - drawList.get(tempXresult - 2));
                for (int j = 0; j < MaxIndex - 1; j++) {
                    if (secondMin > (Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3)))) {
                        secondIndex = j + 1;
                        secondMin = Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3));
                    }
                }
                resultX.add(tempXresult - secondIndex - 1);
                //加上顶点
                resultX.add(dianshuX.get(i));
                //算完第一个点之后应该算的是后面的15个点。
                if (dianshuX.get(i) <= 1185) {
                    double back15Y = drawList.get(dianshuX.get(i) + 14);
                    tan = (dianshuY[i] - back15Y) / 15;
                    double[] backTempDouble = new double[13];
                    for (int j = 0; j < 13; j++) {
                        backTempDouble[j] = Math.abs((tan * (13 - j)) + back15Y - drawList.get(dianshuX.get(i) + j + 1));
                    }

                    MaxIndex = 0;
                    max = backTempDouble[0];
                    for (int h = 0; h < backTempDouble.length - 1; h++) {
                        if (max < backTempDouble[h + 1]) {
                            max = backTempDouble[h + 1];
                            MaxIndex = h + 1;
                        }
                    }
                    int backTempXresult = dianshuX.get(i) + MaxIndex;

                    secondIndex = 0;
                    secondMin = Math.abs(drawList.get(backTempXresult + 1) - drawList.get(backTempXresult + 2));
                    for (int j = 0; j < 13 - MaxIndex; j++) {
                        if (secondMin > (Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3)))) {
                            secondIndex = j + 1;
                            secondMin = Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3));
                        }
                    }
                    resultX.add(backTempXresult + secondIndex + 1);
                }
            }
        } else {
            //***********************************************
            double[] foward13Y = new double[dianshuX.size()];
            for (int i = 0; i < dianshuX.size(); i++) {
                foward13Y[i] = drawList.get(dianshuX.get(i) - 12);
            }
            for (int i = 0; i < foward13Y.length; i++) {
                double tan = (dianshuY[i] - foward13Y[i]) / 12;
                double[] tempResult = new double[11];
                for (int j = 0; j < 11; j++) {
                    tempResult[j] = Math.abs((tan * (j + 1)) + foward13Y[i] - drawList.get(dianshuX.get(i) - 11 + j));
                }

                int MaxIndex = 0;
                double max = tempResult[0];
                for (int h = 0; h < tempResult.length - 1; h++) {
                    if (max < tempResult[h + 1]) {
                        max = tempResult[h + 1];
                        MaxIndex = h + 1;
                    }
                }
                int tempXresult = dianshuX.get(i) - 11 + MaxIndex;

                int secondIndex = 0;
                double secondMin = Math.abs(drawList.get(tempXresult - 1) - drawList.get(tempXresult - 2));
                for (int j = 0; j < MaxIndex - 1; j++) {
                    if (secondMin > (Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3)))) {
                        secondIndex = j + 1;
                        secondMin = Math.abs(drawList.get(tempXresult - j - 2) - drawList.get(tempXresult - j - 3));
                    }
                }
                resultX.add(tempXresult - secondIndex - 1);
                //加上顶点
                resultX.add(dianshuX.get(i));
                //算完第一个点之后应该算的是后面的15个点。
                if (dianshuX.get(i) <= 1185) {
                    double back15Y = drawList.get(dianshuX.get(i) + 14);
                    tan = (dianshuY[i] - back15Y) / 15;
                    double[] backTempDouble = new double[13];
                    for (int j = 0; j < 13; j++) {
                        backTempDouble[j] = Math.abs((tan * (13 - j)) + back15Y - drawList.get(dianshuX.get(i) + j + 1));
                    }

                    MaxIndex = 0;
                    max = backTempDouble[0];
                    for (int h = 0; h < backTempDouble.length - 1; h++) {
                        if (max < backTempDouble[h + 1]) {
                            max = backTempDouble[h + 1];
                            MaxIndex = h + 1;
                        }
                    }
                    int backTempXresult = dianshuX.get(i) + MaxIndex;

                    secondIndex = 0;
                    secondMin = Math.abs(drawList.get(backTempXresult + 1) - drawList.get(backTempXresult + 2));
                    for (int j = 0; j < 13 - MaxIndex; j++) {
                        if (secondMin > (Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3)))) {
                            secondIndex = j + 1;
                            secondMin = Math.abs(drawList.get(backTempXresult + j + 2) - drawList.get(backTempXresult + j + 3));
                        }
                    }
                    resultX.add(backTempXresult + secondIndex + 1);
                }
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @OnClick(R.id.commit)
    public void onClick() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MPAndroidActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage("结束此次心电传输，并填写诊断报告吗?");
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Stop = true;
                Resopnse resopnse = new Resopnse();
                resopnse.setContent("1");
                Map<String, Object> map = new HashMap<>();
                map.put("cancle", true);
                resopnse.setExtraMap(map);
                c.sendMessage(resopnse, new MessageSendListener() {
                    @Override
                    public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                        Toast.makeText(MPAndroidActivity.this, "已结束此次传输", Toast.LENGTH_SHORT).show();
                    }
                });
                Intent intent = new Intent(MPAndroidActivity.this, BingLiActivity.class);
                intent.putExtra("objectId", objcetdId);
                intent.putExtra("xinlv", "79");
                intent.putExtra("RR", "0.823");
                intent.putExtra("QRS", "0.009");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MPAndroidActivity.this).toBundle());
                finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }
}
