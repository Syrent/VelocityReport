package ir.sayandevelopment.sayanreport;

import java.time.LocalDateTime;

public class Staff {

    private String username;
    private LocalDateTime joinDate;
    private LocalDateTime leaveDate;

    public Staff(String username, LocalDateTime joinDate, LocalDateTime leaveDate) {
        this.username = username;
        this.joinDate = joinDate;
        this.leaveDate = leaveDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public LocalDateTime getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDateTime leaveDate) {
        this.leaveDate = leaveDate;
    }
}
