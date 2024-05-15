public class CallSession {
    private String callID;
    private String from;
    private String to;
    private int duration;
    private boolean isTimerActive;
    private Date startTime;
    private Date endTime;

    public CallSession(String callID, String from, String to) {
        this.callID = callID;
        this.from = from;
        this.to = to;
        
        this.duration = 0;
        this.isTimerActive = false;
    }

    public void startTimer() {
        // start timer
    }

    public void stopTimer() {
        // stop timer
    }

    public void renewTimer() {
        // renew timer
    }

    public int getDuration() {
        // get duration
        return 0;
    }
}
