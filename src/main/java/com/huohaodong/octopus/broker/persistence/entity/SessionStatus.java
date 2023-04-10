package com.huohaodong.octopus.broker.persistence.entity;

public enum SessionStatus {

    OFFLINE("OFFLINE"),
    ONLINE("ONLINE");

    public String getStatus;
    private String status;

    SessionStatus(String status) {
        this.status = status;
    }

}
