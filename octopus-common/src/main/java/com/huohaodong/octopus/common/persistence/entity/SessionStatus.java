package com.huohaodong.octopus.common.persistence.entity;

public enum SessionStatus {
    OFFLINE("OFFLINE"), ONLINE("ONLINE");

    private final String status;

    SessionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
