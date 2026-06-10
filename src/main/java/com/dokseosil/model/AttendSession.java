package com.dokseosil.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AttendSession {
    private int sessionId;
    private int memberId;
    private int seatNo;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    // 화면용 조인 필드
    private String memberName;
    private String memberPhone;

    public int getSessionId() { return sessionId; }
    public void setSessionId(int v) { this.sessionId = v; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int v) { this.memberId = v; }

    public int getSeatNo() { return seatNo; }
    public void setSeatNo(int v) { this.seatNo = v; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime v) { this.checkIn = v; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime v) { this.checkOut = v; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String v) { this.memberName = v; }

    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String v) { this.memberPhone = v; }

    public boolean isActive() { return checkOut == null; }

    public String getDurationStr() {
        LocalDateTime end = checkOut != null ? checkOut : LocalDateTime.now();
        Duration d = Duration.between(checkIn, end);
        long h = d.toHours();
        long m = d.toMinutes() % 60;
        return h + "시간 " + m + "분";
    }

    public long getDurationMinutes() {
        LocalDateTime end = checkOut != null ? checkOut : LocalDateTime.now();
        return Duration.between(checkIn, end).toMinutes();
    }

    /* JSP에서 EL로 바로 쓰는 포맷 헬퍼 ($s.checkInDate 등) */
    private static final DateTimeFormatter D  = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter T  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String getCheckInDate()     { return checkIn  == null ? "" : checkIn.format(D); }
    public String getCheckInTime()     { return checkIn  == null ? "" : checkIn.format(T); }
    public String getCheckInDateTime() { return checkIn  == null ? "" : checkIn.format(DT); }
    public String getCheckOutTime()    { return checkOut == null ? "" : checkOut.format(T); }
    public String getCheckOutDateTime(){ return checkOut == null ? "" : checkOut.format(DT); }
}
