package org.mobicents.servlet.sip.example;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
    private CreditControl creditControl;

    public CallSession(String callID, String from, String to, float credit, CreditControl creditControl) {
        this.callID = callID;
        this.from = from;
        this.to = to;
        this.credit = credit;
        this.creditControl = creditControl;

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
                this.timerRounds++;

                if (creditControl.getCredit() < 0.0) {
                    DiameterOpenIMSSipServlet.sendSIPMessage(from, "Credit is over");
                }
                
                // Deduct credit
                creditControl.subCredit(20);

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

        // Give back the reserved credit
        // NOTE: TEST THE DURATION TIME - IS IT MILISECONDS?
        float unusedCredit = (float) this.duration / 1000.0f - (this.timerRounds - 1) * 120.0f;

        creditControl.addCredit(unusedCredit);
    }

    public int getDuration() {
        return this.duration;
    }

    public int getTimerRounds() {
        return this.timerRounds;
    }
}
