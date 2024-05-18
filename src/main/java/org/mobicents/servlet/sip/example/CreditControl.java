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
    this.credit = 1000;
    this.user = user;
    this.date_off = date;//new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(date);
    this.callSessionMap = new HashMap<>();
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
   * Objective 2: Online taxation
   * Manages the billing session for a call (online)
   */
  public boolean startBillingSession(String callID, String to) {
    logger.info("==============> RM T2 logger: startBillingSession");
    // Check if theres enough credit to start a call
    if (this.credit < 40) {
      logger.info("==============> RM T2 logger: insufficient credit to start a call");
      return false;
    }

    // Creates and adds a CallSession to the HashMap
    CallSession callSession = new CallSession(callID, this.user, to, this.credit, this);
    this.callSessionMap.put(callID, callSession);

    logger.info("==============> RM T2 logger: Created CallSession and added to HashMap");

    // Reserve initial credit for the call - 20 + 20
    this.subCredit(40);

    logger.info("==============> RM T2 logger: reserved initial credit for the call, total credit: " + this.credit);
    
    // Start timer
    callSession.startTimer();

    return true;
  }


  public void stopBillingSession(String callID) {
    // Get the CallSession from the HashMap
    CallSession callSession = this.callSessionMap.get(callID);

    if (callSession != null) {
      // Stop the timer
      callSession.stopTimer();
    }
  }


  public CallSession getCallSession(String callID) {
    return this.callSessionMap.get(callID);
  }
  
} //class ends

