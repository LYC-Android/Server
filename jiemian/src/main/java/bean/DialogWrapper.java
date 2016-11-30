package bean;

import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;

/**
 * Created by mr.cheng on 2016/10/9.
 */

public class DialogWrapper {
    private BmobIMMessage msg;
    private BmobIMUserInfo info;

    public DialogWrapper(BmobIMUserInfo info, BmobIMMessage msg) {
        this.info = info;
        this.msg = msg;
    }

    public BmobIMUserInfo getInfo() {
        return info;
    }

    public void setInfo(BmobIMUserInfo info) {
        this.info = info;
    }

    public BmobIMMessage getMsg() {
        return msg;
    }

    public void setMsg(BmobIMMessage msg) {
        this.msg = msg;
    }
}
