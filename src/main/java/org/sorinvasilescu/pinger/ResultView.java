package org.sorinvasilescu.pinger;

public class ResultView {

    private String host;
    private Result icmp_ping;
    private Result tcp_ping;
    private Result trace;

    public ResultView() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Result getIcmp_ping() {
        return icmp_ping;
    }

    public void setIcmp_ping(Result icmp_ping) {
        this.icmp_ping = icmp_ping;
    }

    public Result getTcp_ping() {
        return tcp_ping;
    }

    public void setTcp_ping(Result tcp_ping) {
        this.tcp_ping = tcp_ping;
    }

    public Result getTrace() {
        return trace;
    }

    public void setTrace(Result trace) {
        this.trace = trace;
    }
}
