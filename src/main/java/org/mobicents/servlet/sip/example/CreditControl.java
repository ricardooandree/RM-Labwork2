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

  
  public CreditControl(String user, Date date)
  {
    this.is_registered = true;
    this.credit = 1000;
    this.user = user;
    this.date_off = date;//new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(date);
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

    float price = calculateTaxOffline(minutes); // Price per minute (seconds-testing)
    this.subCredit(price);
    this.is_registered = true;
  } else {

  }
}

public static float calculateTaxOffline(int minutes) {
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

public static float calculateTaxOnline(int minutes) {
    float price = 20.0f;

    price += minutes * 10;

    return price;
}

public static float sessionTimer() {
    try{
        Thread.sleep(2000);
    }
    catch{

    }
}

}//class

