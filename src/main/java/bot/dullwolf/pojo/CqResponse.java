package bot.dullwolf.pojo;

import java.io.Serializable;

//调用HTTPAPI发送消息之后的返回体
public class CqResponse<T> implements Serializable {

    private String status;
    private int retCode;
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}