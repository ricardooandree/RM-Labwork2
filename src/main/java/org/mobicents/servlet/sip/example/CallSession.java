package org.mobicents.servlet.sip.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

public class CallSession {
    private String callID;
    private String from;
    private String to;
    private float credit;

    private int duration;
    private int timerRounds;
    private Date startTime;
    private Date endTime;
    private Timer timer;

    public CallSession(String callID, String from, String to, float credit) {
        this.callID = callID;
        this.from = from;
        this.to = to;
        this.credit = credit;

        this.duration = 0;
        this.timerRounds = 0;
        this.startTime = null;
        this.endTime = null;
        this.timer = new Timer();
    }

    public void startTimer() {
        // Get current time (call start time)
        if (this.startTime == null) {
            this.startTime = new Date();
        }

        // Start timer
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Increment timer rounds
                timerRounds++;

                // FIXME: Instead of using timerRounds, we should take the credits directly for each timer cycle 
                //if (CreditControl.this.getCredit() < 0.0) {
                    //DiameterOpenIMSSipServlet.sendSIPMessage(this.from, "Credit is over");
                //}
                //CreditControl.this.subCredit(20);

                // Restart timer
                startTimer();
            }
        }, 120000); // 2 minutes in milliseconds
    }

    public void stopTimer() {
        // Stop timer
        this.timer.cancel();

        // Get current time (call end time)
        this.endTime = new Date();

        // Calculate call duration
        this.duration = (int) (this.endTime.getTime() - this.startTime.getTime());
    }

    public int getDuration() {
        return this.duration;
    }

    public int getTimerRounds() {
        return this.timerRounds;
    }
}
