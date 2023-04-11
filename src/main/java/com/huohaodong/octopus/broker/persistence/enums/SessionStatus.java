package com.huohaodong.octopus.broker.persistence.enums;

public enum SessionStatus {
    OFFLINE("OFFLINE"), ONLINE("ONLINE");

    private String status;

    SessionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
