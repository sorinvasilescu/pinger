package org.sorinvasilescu.pinger;

public class Result {

    private String result;
    private Boolean success;

    public Result() {}

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "Result{" +
                "result='" + result + '\'' +
                ", success=" + success +
                '}';
    }
}
