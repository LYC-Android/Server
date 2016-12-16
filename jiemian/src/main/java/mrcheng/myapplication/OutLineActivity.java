package mrcheng.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

import org.litepal.crud.DataSupport;

import java.util.List;

import bean.DatabaseInfo;
import bean.File_Message;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import util.MyXFormatter;
import util.ReadFile;
import util.ValueObject;

//病人的objcetId
public class OutLineActivity extends Activity {

    @InjectView(R.id.chart1)
    LineChart mChart;
    @InjectView(R.id.realName)
    TextView mRealName;
    @InjectView(R.id.age)
    TextView mAge;
    @InjectView(R.id.xinlv)
    TextView mXinlv;
    private XAxis xl;
    private Thread drawThread;
    private ReadFile readFileThread;
    private static final String TAG = "OutLineActivity";
    private int index;
    private int counter;
    private int DrawCounter;
    private String lock = new String("");
    private int[] xinlv;
    private String objcetdId;
    private int avaverageXinLv;//平均心率
    private int minXinLv;
    private long minXinLvTime;
    private int MaxXinLvTime;
    private int MaxRR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_line);
        ButterKnife.inject(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ValueObject.value = "";

        readFileThread = new ReadFile(OutLineActivity.this, getIntent().getStringExtra("path"), lock);
        readFileThread.start();
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
        initView();
    }

    private void initView() {
        // enable description text
        mChart.getDescription().setEnabled(false);


        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);//#########这里background

        // if disabled, scaling can be done on x- and y-axis separately
        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);


        LineData data = new LineData();
//        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
        mChart.setExtraRightOffset(50);
        //还有轴没有设置
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
        //反转轴值
        leftAxis.setInverted(true);
        leftAxis.setEnabled(false);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(752f);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        feedMultiple();


    }

    private void feedMultiple() {
        if (drawThread != null)
            drawThread.interrupt();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();

            }
        };
        //这里设置了画3次之后停止画图
        drawThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (!readFileThread.isFinish()) {
                        synchronized (lock) {
                            if (ValueObject.value.equals("")) {
                                lock.wait();
                            }
                            index = 0;
                            counter = 0;
                            DrawCounter++;
                            xinlv = readFileThread.getXinlvdatas();
                            for (int i = 0; i < readFileThread.getResult().size(); i++) {
                                runOnUiThread(runnable);
                                try {
                                    Thread.sleep(8000 / 1200);
                                } catch (InterruptedException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            //计算平均心率
                            int avaverage = 0;
                            for (int i = 0; i <xinlv.length ; i++) {
                                avaverage = avaverage + xinlv[i];
                            }
                            avaverageXinLv = (avaverageXinLv + avaverage / xinlv.length) / DrawCounter;

                            //计算最小心率
//                            int min = xinlv[0];
//                            for (int i = 1; i < xinlv.length; i++) {
//                                if (min > xinlv[i]) {
//                                    min = xinlv[i];
//                                }
//                            }
//                            if (DrawCounter == 1) {
//                                minXinLv = min;
//                                minXinLvTime=
//                            } else {
//
//                            }
                            //计算最大RR间期
                            //......
                            ValueObject.value = "";
                            lock.notify();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OutLineActivity.this, "结束了", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        xl.setValueFormatter(new MyXFormatter(0));

        drawThread.start();
    }

    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            if (set1 == null) {
                set1 = createSe1t();
                data.addDataSet(set1);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (readFileThread.getResult().get(index) + 0f)), 0);
            if (counter < readFileThread.getResultX().size()) {
                if (set.getEntryCount() == (readFileThread.getResultX().get(counter) + (DrawCounter - 1) * 1200)) {
                    data.addEntry(new Entry(set.getEntryCount(), (float) (readFileThread.getResultY().get(counter) + 0f)), 1);
                    counter++;
                }
            }

            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(1200);
            mChart.setVisibleXRangeMinimum(1200);
            mChart.moveViewToX(data.getEntryCount());
            index++;
            if (index % (1200 / xinlv.length) == 0) {
                mXinlv.setText("心率:" + xinlv[(index / (1200 / xinlv.length)) - 1] + "/min");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (drawThread != null) {
            drawThread.interrupt();
        }
        if (readFileThread != null) {
            readFileThread.interrupt();
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

    @OnClick(R.id.commit)
    public void onClick() {
        if (readFileThread.isFinish()) {
            List<File_Message> databaseInfos = DataSupport.select("haveReport", "ReportId")
                    .where("filePath = ?", getIntent().getStringExtra("path")).find(File_Message.class);
            if (databaseInfos.size() > 0) {
                if (databaseInfos.get(0).isHaveReport()) {
                    haveBeenReport(databaseInfos.get(0).getReportId());
                } else {
                    DonHaveReport();
                }
            } else {
                Toast.makeText(OutLineActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(OutLineActivity.this, "请耐心观看", Toast.LENGTH_SHORT).show();
        }
    }

    private void DonHaveReport() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OutLineActivity.this);
        dialog.setTitle("提示:");
        dialog.setMessage("关看完毕,写心电报告?");
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(OutLineActivity.this, BingLiActivity.class);
                intent.putExtra("objectId", objcetdId);
                intent.putExtra("RR", "0.871");
                intent.putExtra("QRS", "0.23");
                intent.putExtra("xinlv", avaverageXinLv+"");
                String temp = getIntent().getStringExtra("time");
                String[] arr = temp.split("\\s+");
                intent.putExtra("time", arr[0]);
                intent.putExtra("path", getIntent().getStringExtra("path"));
                startActivity(intent);
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

    private void haveBeenReport(final String objectId) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OutLineActivity.this);
        dialog.setTitle("提示:");
        dialog.setMessage("此心电已有诊断报告，是否查看?");
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(OutLineActivity.this, History_DetailActivity.class);
                intent.putExtra("objectId", objectId);
                startActivity(intent);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }
}
