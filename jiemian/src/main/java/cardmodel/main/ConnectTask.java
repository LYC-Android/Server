package cardmodel.main;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import mrcheng.myapplication.R;
import util.Constant;

/**
 * Created by Jorble on 2016/3/4.
 */
public class ConnectTask extends AsyncTask<Void, Void, Void> {

	private Boolean statu;

	private byte[] read_buff;

	private TextView error_tv;

	private static Socket mSocket;
	private SocketAddress mSocketAddress;
	private static InputStream inputStream;
	private static OutputStream outputStream;

	private Boolean STATU = false;

	public ConnectTask( TextView error_tv) {
		this.error_tv = error_tv;
	}

	/**
	 * 更新界面
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		if (STATU) {
			error_tv.setTextColor(Color.GREEN);
			error_tv.setText("连接状态:成功");
		} else {
			error_tv.setTextColor(Color.RED);
			error_tv.setText("连接状态:失败");
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
		mSocket = new Socket();
		mSocketAddress = new InetSocketAddress(Constant.IP, Constant.port);
		try {
			// socket连接
			mSocket.connect(mSocketAddress, 3000);// 设置连接超时时间为3秒
			if (mSocket.isConnected()) {
				setSTATU(true);
				inputStream = mSocket.getInputStream();// 得到输入流
				outputStream = mSocket.getOutputStream();// 得到输出流
			} else {
				setSTATU(false);
			}
			// 更新界面
			publishProgress();
			Thread.sleep(200);

		} catch (IOException e) {
			setSTATU(false);
			publishProgress();
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断socket是否还在连接
	 * 
	 * @return
	 */
	public Boolean isSuccess() {
		return mSocket.isConnected();
	}

	/**
	 * 获取socket
	 * 
	 * @return
	 */
	public Socket getmSocket() {
		return mSocket;
	}

	/**
	 * 获取输入流
	 * 
	 * @return
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * 获取输出流
	 * 
	 * @return
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	public Boolean getSTATU() {
		return STATU;
	}

	public void setSTATU(Boolean sTATU) {
		STATU = sTATU;
	}

}
