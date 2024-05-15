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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

/**
 *
 * This is the SIP Servlet for OpenIMS Integration example.
 * 
 */
public class DiameterOpenIMSSipServlet extends SipServlet {

  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(DiameterOpenIMSSipServlet.class);

  DiameterShClient diameterShClient = null;

  private static SipFactory sipFactory;

  //Data structure to control the credit of each user
  public static HashMap<String, CreditControl> usersCreditDB = new HashMap<String, CreditControl>();

  /**
   * Default constructor.
   */
  public DiameterOpenIMSSipServlet() {}

  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {


      logger.info("==============================================================================");
      logger.info("==============>                                              =================");
      logger.info("==============>    RM  2023/2024                             =================");
      logger.info("==============>         Trab 2 Charging                      =================");
      logger.info("==============>                                              =================");
      logger.info("==============>   Students:                                  =================");
      logger.info("==============>          Matilde, 58608                       =================");
      logger.info("==============>          Ricardo, 56128                       =================");
      logger.info("==============>                                              =================");
      logger.info("==============================================================================");
  
    super.init(servletConfig);

    // Get the SIP Factory
    sipFactory = (SipFactory)servletConfig.getServletContext().getAttribute(SIP_FACTORY);

    // Initialize Diameter Sh Client
    try
    {
      // Get our Diameter Sh Client instance
      this.diameterShClient = new DiameterShClient();

      logger.info("==============> RM T2 logger: Diameter OpenIMS SIP Servlet : Sh-Client Initialized successfuly!");
    }
    catch ( Exception e ) {
      logger.error( "==============> RM T2 logger: Diameter OpenIMS SIP Servlet : Sh-Client Failed to Initialize.", e );
    }   
  }












  @Override
  protected void doInvite(SipServletRequest request) throws ServletException, IOException
  {
    try
    {
    logger.info("==============> RM T2 logger: Proccessing INVITE (" + request.getFrom() + " -> " + request.getTo() +") Request...");
    logger.info("==============> RM T2 logger: please complete doInvite ...");

    // rado's comments
    // sends the Invite back - see page 42 of spec.book ("sipservlet-1.0-fcs.pdf") in in "rm_biblio" Desktop folder

    String from = request.getFrom().getURI().toString();
    logger.info("==============> RM T2 logger: Proccessing INVITE: From " + from);
    logger.info("==============> RM T2 logger: Proccessing INVITE: CallID " + request.getCallId());

    if(request.isInitial())
    {
      Proxy proxy = request.getProxy();
      if(request.getSession().getAttribute( "firstInvite") == null)
      {
        request.getSession().setAttribute( "firstInvite", true );
        proxy.setRecordRoute(true);
        proxy.setSupervised(true);
        proxy.proxyTo( request.getRequestURI() );
      }
      else
      {
        proxy.proxyTo( request.getRequestURI() );
      }
    }
    }
    catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doInvite method.", e );
    }
  }




    @Override
    protected void doAck(SipServletRequest request) throws ServletException, IOException
    {
      // process ACK
      try
      {
        logger.info("==============> RM T2 logger: Proccessing ACK (" + request.getFrom() + " -> " + request.getTo() +") Request...");
        logger.info("==============> RM T2 logger: please complete doACK ...");
      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doACK method.", e );
      }
    }



    @Override
    protected void doBye(SipServletRequest request) throws ServletException, IOException
    {
      // process Bye
      try
      {
        logger.info("==============> RM T2 logger: Proccessing BYE (" + request.getFrom() + " -> " + request.getTo() +") Request...");
        logger.info("==============> RM T2 logger: please complete doBye ...");
      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doBye method.", e );
      }
    }



    @Override
    protected void doSuccessResponse(SipServletResponse response) throws ServletException, IOException
    {
      // process 2xx response codes
      try
      {
           logger.info("==============> RM T2 logger: Proccessing doSuccessResponse  STATUS(" + response.getStatus() + " from " + response.getFrom().getURI().toString() + ")...");
           logger.info("==============> RM T2 logger: please complete doSuccessResponse ...");
           
      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doSuccessResponse method.", e );
      }
    }











  @Override
  protected void doErrorResponse(SipServletResponse response ) throws ServletException, IOException
  {
    try
    {
    logger.info("==============> RM T2 logger: Proccessing Error Response (" + response.getStatus() + ")...");
    logger.info("==============> RM T2 logger: please complete doErrorResponse ...");

    if(response.getStatus() == 404) //404 - not found; User not found;
    {
      // Let's see from whom to whom
      String to =  response.getTo().getDisplayName() == null ? response.getTo().getURI().toString() : response.getTo().getDisplayName() + " <" + response.getTo().getURI() + ">";
      String from = response.getFrom().getDisplayName() == null ? response.getFrom().getURI().toString() : response.getFrom().getDisplayName() + " <" + response.getFrom().getURI() + ">";

      String toAddress = response.getTo().getURI().toString();

      
    }
    else
    {
      logger.info( "==============> RM T2 logger: Got error response (" + response.getStatus() + "). Not processing further." );
    }
    }
    catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doErrorResponse method.", e );
    }
  }
















  //////////////////////////////////////////////////////////////////////////////
  //
  // sends the final message to the user - SMS
  //
  //////////////////////////////////////////////////////////////////////////////
    public static void sendSIPMessage(String toAddressString, String message)
  {
    try
    {
      logger.info( "==============> RM T2 logger: Sending SIP Message [" + message + "] to [" + toAddressString + "]" );

      SipApplicationSession appSession = sipFactory.createApplicationSession();
      Address from = sipFactory.createAddress("RM_T2 <sip:rm_t2@open-ims.test>");
      Address to = sipFactory.createAddress(toAddressString);
      SipServletRequest request = sipFactory.createRequest(appSession, "MESSAGE", from, to);
      request.setContent(message, "text/html");

      request.send(); 
    }
    catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure creating/sending SIP Message notification.", e );
    }

  }
}
