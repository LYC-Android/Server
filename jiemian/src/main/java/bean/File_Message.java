package bean;

import org.litepal.crud.DataSupport;

/**
 * Created by mr.cheng on 2016/9/12.
 */
public class File_Message extends DataSupport {
    private String filePath;
    private String fileUrl;
    private String number;
    private String name;
    private int id;
    private boolean haveReport;
    private String ReportId;
    private String userObjectId;

    public String getUserObjectId() {
        return userObjectId;
    }

    public void setUserObjectId(String userObjectId) {
        this.userObjectId = userObjectId;
    }

    public boolean isHaveReport() {
        return haveReport;
    }

    public void setHaveReport(boolean haveReport) {
        this.haveReport = haveReport;
    }

    public String getReportId() {
        return ReportId;
    }

    public void setReportId(String reportId) {
        ReportId = reportId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
