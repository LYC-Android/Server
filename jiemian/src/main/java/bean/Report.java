package bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by mr.cheng on 2016/12/13.
 */
public class Report  extends BmobObject{
    private String username;
    private String createTime;
    private String resultTime;
    private String xinlv;
    private String RR;
    private String QRS;
    private String suggest;
    private String message;
private String author;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getQRS() {
        return QRS;
    }

    public void setQRS(String QRS) {
        this.QRS = QRS;
    }

    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public String getRR() {
        return RR;
    }

    public void setRR(String RR) {
        this.RR = RR;
    }

    public String getSuggest() {
        return suggest;
    }

    public void setSuggest(String suggest) {
        this.suggest = suggest;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getXinlv() {
        return xinlv;
    }

    public void setXinlv(String xinlv) {
        this.xinlv = xinlv;
    }
}
