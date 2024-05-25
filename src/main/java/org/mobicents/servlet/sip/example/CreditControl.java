/*
 * RM - segundo trabalho
 *
 *      Charging in IMS
 *
 *  Rodolfo Oliveira
 *   rado@fct.unl.pt
 *
 */
package org.mobicents.servlet.sip.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * CreditControl.java
 *
 *
 */
public class CreditControl
{

  private String user;    // identifies the user
  private Date date_off;  // date when a given user is unregistered
  private float credit;  // amount of credit
  private boolean is_registered; // controls if user is registered
  private HashMap<String, CallSession> callSessionMap;
  private static final Logger logger = Logger.getLogger(CreditControl.class);


  public CreditControl(String user, Date date)
  {
    this.is_registered = true;
    this.credit = 50;
    this.user = user;
    this.date_off = date;//new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(date);
    this.callSessionMap = new HashMap<String, CallSession>();
  }


  public String getNotification()
  {
    return "Dear " + this.user + ", your credit is " + this.credit + ".";
  }


  @Override
  public int hashCode()
  {
    //return (callee + date).hashCode();
      return (user).hashCode();
  }


  @Override
  public boolean equals( Object obj )
  {
    if(obj != null && obj instanceof CreditControl)
    {
      CreditControl other = (CreditControl)obj;
      //return this.callee.equals(other.callee) && this.date.equals(other.date);
      return this.user.equals(other.user);
    }
    return false;
  }


  public float getCredit()
  {
    return credit;
  }


  public float subCredit(float value)
  {
    credit = credit-value;
    return credit;
  }


  public float addCredit(float value)
  {
    credit = credit+value;
    return credit;
  }


  public String getUser()
  {
    return this.user;
  }


  // update credit when the user does the DEregister
  public void setDate_off(Date d)
  {
    this.date_off = d;
    this.is_registered = false;
  }


  // update credit when the user does the register
  public void update_register()
  {
    if (!this.is_registered)
    {
      Date now = new Date();

      long diff = now.getTime() - date_off.getTime();   // Difference in miliseconds

      int minutes = (int) (diff / (1000 * 60));   // Convert to mins

      float price = calculateOfflineTax(minutes); // Price per minute (seconds-testing)
      this.subCredit(price);
      this.is_registered = true;
    }
  }

  /*
   * Objective 1: Offline taxation
   * Calculate the tax based on the time the user is unregistered (offline)
   */
  public static float calculateOfflineTax(int minutes) {
    float price = 0.0f;
    int round = 1;

    while (minutes > 0) {
        if (minutes > 60) {
            price += round * 60;
            minutes -= 60;
        } else {
            price += minutes * round;
            break;
        }
        round++;
    }
    return price;
  }


  /*
   * Objective 2/3: Online taxation
   * Manages the billing session for a call (online)
   */
  public boolean startBillingSession(String callID, boolean flag) {
    logger.info("==============> RM T2 logger: startBillingSession");

    if (flag == true) {
      // Reserves inital credit for the call - 20+20
      this.subCredit(40);
    } 
    
    // Creates and adds a CallSession to the HashMap
    CallSession callSession = new CallSession(callID, this.credit, this, flag);
    this.callSessionMap.put(callID, callSession);

    logger.info("==============> RM T2 logger: Created CallSession and added to HashMap");

    // Start timer
    callSession.startTimer();

    return true;
  }


  public void stopBillingSession(String callID) {
    logger.info("==============> RM T2 logger: stopBillingSession");

    // Get the CallSession from the HashMap
    CallSession callSession = this.callSessionMap.get(callID);

    if (callSession != null) {
      logger.info("==============> RM T2 logger: calling stopTimer");

      // Stop the timer
      callSession.stopTimer();

      // Remove the CallSession from the HashMap
      this.callSessionMap.remove(callID);

    } else {
      logger.info("==============> RM T2 logger: call session is null = failed to call stopTimer because calllID is null");
    }
  }


  public boolean existCallSession(String callID) {
    CallSession callSession = this.callSessionMap.get(callID);

    if (callSession != null) {
        logger.info("==============> RM T2 logger: call session exists for call ID: " + callID);
        return true;
    }

    logger.info("==============> RM T2 logger: call session does not exist for call ID: " + callID);
    return false;
  }


  public CallSession getCallSession(String callID) {
    CallSession callSession = this.callSessionMap.get(callID);

    if (callSession != null) {
        return callSession;
    }

    return null;
  }


} //class ends


