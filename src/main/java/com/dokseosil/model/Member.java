package com.dokseosil.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Member {
    private int memberId;
    private String name;
    private String phone;
    private String pin;
    private String memo;
    private LocalDateTime createdAt;

    // 화면용 부가 정보 (현재 입실 좌석. 비입실이면 0)
    private int currentSeatNo;

    public int getMemberId() { return memberId; }
    public void setMemberId(int v) { this.memberId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }

    public String getPin() { return pin; }
    public void setPin(String v) { this.pin = v; }

    public String getMemo() { return memo; }
    public void setMemo(String v) { this.memo = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public int getCurrentSeatNo() { return currentSeatNo; }
    public void setCurrentSeatNo(int v) { this.currentSeatNo = v; }

    public boolean isActive() { return currentSeatNo > 0; }

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public String getCreatedDate() { return createdAt == null ? "" : createdAt.format(D); }
}
