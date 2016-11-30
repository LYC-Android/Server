package cardmodel.main;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;


import org.litepal.crud.DataSupport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import bean.DatabaseInfo;
import cardmodel.cardutil.HexStrConvertUtil;
import cardmodel.cardutil.StreamUtil;
import util.Constant;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ControlTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "ControlTask";

	private TextView infoTv, mAge, mSex, mRealName, mMedicalNumber;
   private Context mContext;
    private Handler mHandler;
	private Boolean statu = false;
	private String cmd;
	private String err;

	private byte[] read_buff;
	private String readStr;
    private String mObjectId;

	private InputStream inputStream;
	private OutputStream outputStream;
    private UDPService udpService;

    public ControlTask(Context context, TextView infoTv, InputStream inputStream, OutputStream outputStream, String cmd,
                       String err, TextView ageTextview, TextView sexTextview, TextView realNameTextview, TextView medicalTextview, Handler handler) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
        this.mContext=context;
		this.cmd = cmd;
		this.err = err;
		this.infoTv = infoTv;
        this.mAge =ageTextview;
        this.mSex =sexTextview;
        this.mRealName =realNameTextview;
        this.mMedicalNumber =medicalTextview;
        this.mHandler=handler;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		switch (cmd) {
		// 读卡
            case Constant.READ_CARD_CMD:
                try {
                    if (statu) {
                        Constant.CARD_ID = readStr.substring(readStr.length() - 10, readStr.length() - 2);
                        infoTv.setText("卡号:" + Constant.CARD_ID);
                        infoTv.setTextColor(Color.GREEN);
                        List<DatabaseInfo> mList= DataSupport.where("cardNumber = ?", Constant.CARD_ID).find(DatabaseInfo.class);
                       if (mList.size()>0){
                           mRealName.setText("姓名:"+mList.get(0).getRealName());
                           mAge.setText("年龄:"+mList.get(0).getAge());
                           mSex.setText("性别:"+mList.get(0).getSex());
                           mMedicalNumber.setText("病历号:"+mList.get(0).getMedicalNumber());
                           mObjectId=mList.get(0).getObjectId();
                       }else {
                           mRealName.setText("姓名:--");
                           mAge.setText("年龄:--");
                           mSex.setText("性别:--");
                           mMedicalNumber.setText("病历号:--");
                           mObjectId="";
                           Toast.makeText(mContext, "没有此病人的信息", Toast.LENGTH_SHORT).show();
                       }
                        statu = false;
                    } else {
                        infoTv.setText("卡号:异常");
                        infoTv.setTextColor(Color.RED);
                        mRealName.setText("姓名:--");
                        mAge.setText("年龄:--");
                        mSex.setText("性别:--");
                        mMedicalNumber.setText("病历号:--");
                    }
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

	}

	/**
	 * 子线程任务
	 * 
	 * @param params
	 * @return
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try {
            StreamUtil.writeCommand(outputStream, Constant.FIND_CARD_CMD);
			// 发送命令
            Thread.sleep(200);
            StreamUtil.writeCommand(outputStream, cmd);
			Thread.sleep(200);
			read_buff = StreamUtil.readData(inputStream);
			// 读取返回字符串
			readStr = HexStrConvertUtil.bytesToHexString(read_buff);
			// 判断是否正常
			statu = !(readStr.equals(err.replace(" ", "").toLowerCase()));
			// 更新界面
			publishProgress();
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!statu){
            udpService=new UDPService(Constant.CARD_ID,mContext,mObjectId,mHandler);
            udpService.start();
        }
    }

    public UDPService getUdpService() {
        return udpService;
    }

    public Boolean getStatu() {
        return statu;
    }
}
