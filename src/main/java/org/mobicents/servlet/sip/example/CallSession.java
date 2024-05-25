/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.servlet.sip.example;

/**
 *
 * @author root
 */

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class CallSession {
    private String callID;
    private float credit;
    private boolean flag;

    private int duration;
    private int timerRounds;
    private Date startTime;
    private Date endTime;
    private Timer timer;
    private CreditControl creditControl;
    private static final Logger logger = Logger.getLogger(CallSession.class);

    public CallSession(String callID, float credit, CreditControl creditControl, boolean flag) {
        this.callID = callID;
        this.credit = credit;
        this.creditControl = creditControl;
        this.flag = flag;

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

        logger.info("==============> RM T2 logger: started timer for callID: " + this.callID + " credit: " + creditControl.getCredit() + " startTime: " + this.startTime);

        // Start timer
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Increment timer rounds
                CallSession.this.timerRounds++;

                if (CallSession.this.flag == true) {
                    // Deduct credit - user: from
                    creditControl.subCredit(20);

                } else {
                    // Deduct credit - user: to
                    creditControl.subCredit(10);
                }
                
                if (creditControl.getCredit() < 0.0) {
                    DiameterOpenIMSSipServlet.sendSIPMessage(creditControl.getUser(), "Credit is over");

                    logger.info("==============> RM T2 logger: credit is over total credit: " + creditControl.getCredit());
                }

                logger.info("==============> RM T2 logger: about to restart timer for callID: " + callID + " credit: " + creditControl.getCredit() + " timerRounds: " + CallSession.this.timerRounds);

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
        int durationInSeconds = this.duration / 1000;

        int remainingSeconds = durationInSeconds % 120;

        float unusedCredit;

        if (this.flag == true) {
            unusedCredit = (120 - remainingSeconds) * (20.0f / 120.0f);

        } else {
            unusedCredit = (120 - remainingSeconds) * (10.0f / 120.0f);
        }

        logger.info("==============> RM T2 logger: timer stopped credits before giving back: " + creditControl.getCredit());

        creditControl.addCredit(unusedCredit);

        logger.info("==============> RM T2 logger: timer stopped for callID: " + this.callID + " start date: " + startTime + " end date: " + endTime + " credit: " + creditControl.getCredit() + " duration: " + this.duration + " timerRounds: " + CallSession.this.timerRounds + " unusedCredit: " + unusedCredit);
    }

    public int getDuration() {
        return this.duration;
    }

    public int getTimerRounds() {
        return this.timerRounds;
    }

    public boolean getFlag(CallSession callSession) {
        return this.flag;
    }
}
