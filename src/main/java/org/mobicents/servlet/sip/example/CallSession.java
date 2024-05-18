package org.mobicents.servlet.sip.example;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(CallSession.class);

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

        logger.info("==============> RM T2 logger: started timer for callID: " + this.callID + " from: " + this.from + " to: " + this.to + " credit: " + creditControl.getCredit() + " startTime: " + this.startTime);

        // Start timer
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Increment timer rounds
                this.timerRounds++;

                if (creditControl.getCredit() < 0.0) {
                    DiameterOpenIMSSipServlet.sendSIPMessage(from, "Credit is over");

                    logger.info("==============> RM T2 logger: credit is over total credit: " + creditControl.getCredit());
                }
                
                // Deduct credit
                creditControl.subCredit(20);

                logger.info("==============> RM T2 logger: about to restart timer for callID: " + callID + " from: " + from + " to: " + to + " credit: " + creditControl.getCredit() + " timerRounds: " + timerRounds);

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

        logger.info("==============> RM T2 logger: timer stopped credits before giving back: " + creditControl.getCredit());

        creditControl.addCredit(unusedCredit);

        logger.info("==============> RM T2 logger: timer stopped for callID: " + this.callID + " from: " + this.from + " to: " + this.to + " start date: " + startTime + " end date: " + endTime + " credit: " + creditControl.getCredit() + " duration: " + this.duration + " timerRounds: " + this.timerRounds + " unusedCredit: " + unusedCredit);
    }

    public int getDuration() {
        return this.duration;
    }

    public int getTimerRounds() {
        return this.timerRounds;
    }
}
