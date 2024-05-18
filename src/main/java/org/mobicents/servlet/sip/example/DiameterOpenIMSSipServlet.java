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

  // Data structure to control the credit of each user
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

    /*
     * TODO: Process INVITE requests
     */

    try
    {
    logger.info("==============> RM T2 logger: Proccessing INVITE (" + request.getFrom() + " -> " + request.getTo() +") Request...");
    logger.info("==============> RM T2 logger: please complete doInvite ...");

    // rado's comments
    // sends the Invite back - see page 42 of spec.book ("sipservlet-1.0-fcs.pdf") in in "rm_biblio" Desktop folder

    String from = request.getFrom().getURI().toString();
    logger.info("==============> RM T2 logger: Proccessing INVITE: From " + from);
    logger.info("==============> RM T2 logger: Proccessing INVITE: CallID " + request.getCallId());

      // TODO: SEND ERROR MESSAGE 

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
      /*
       * TODO: Process ACK requests
       */

      try
      {
        logger.info("==============> RM T2 logger: Proccessing ACK (" + request.getFrom() + " -> " + request.getTo() +") Request...");
      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doACK method.", e );
      }
    }


    @Override
    protected void doBye(SipServletRequest request) throws ServletException, IOException
    {
      /*
       * TODO: Process BYE requests
       * RECEBE BYE
       * CANCELA TIMER
       * RESTITUI CREDITO RESERVADO COM BASE NO TEMPO NAO CONSUMIDO PELO TIMER
       */

      try {
        logger.info("==============> RM T2 logger: Proccessing BYE (" + request.getFrom() + " -> " + request.getTo() +") Request...");

        // Get the from and to address/name
        String from = request.getFrom().getURI().toString();
        String to = request.getTo().getURI().toString();

        // Get the callId
        String callId = request.getCallId();

        // Declare user credit control object
        CreditControl userCreditControl = null;

        // Check if the from or to address is in the usersCreditDB - find the real client (from)
        if (usersCreditDB.containsKey(from)) {
          // Get the caller credit control
          userCreditControl = usersCreditDB.get(from);

          if ( userCreditControl.existCallSession(callId) ) {
            // Call stop billing session
            userCreditControl.stopBillingSession(callId);
          } else {
            // Get the caller credit control
            userCreditControl = usersCreditDB.get(to);

            if ( userCreditControl.existCallSession(callId) ) {
              // Call stop billing session
              userCreditControl.stopBillingSession(callId);
          }
        } else {
          // FIXME: THERE'S NO CREDIT CONTROL OBJECT FOR THIS CALL
        }

        // Get callSession object NOTE: Not needed
        // CallSession callSession = userCreditControl.getCallSession(callID);

      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doBye method.", e );
      }
    }



    @Override
    protected void doSuccessResponse(SipServletResponse response) throws ServletException, IOException
    {
      /*
       * TODO: Process 200 OK responses
       * RECEBE 200 OK 
       * VERIFICA SE É DE UM INVITE
       * RESERVA 40 CREDITOS
       * INICIA TIMER (2 MINS)
       * SE TIMER EXPIRAR, RENOVA TIMER
       * SE CREDITO FICAR NEGATIVO, TRANSMITIR MENSAGEM DE ALERTA
       */

      try {
        logger.info("==============> RM T2 logger: Proccessing doSuccessResponse  STATUS(" + response.getStatus() + " from " + response.getFrom().getURI().toString() + ")...");

        
        // Check if the response (200 OK) is from an INVITE
        if (response.getMethod().equals("INVITE")) {

          // Get the from and to address/name
          String from = response.getFrom().getURI().toString();
          String to = response.getTo().getURI().toString();

          // Get the callId
          String callID = response.getCallId();

          // Check if the user credit control exists in the database (HashMap)
          if (usersCreditDB.containsKey(from)) {
            // Get the caller credit control
            CreditControl userCreditControl = usersCreditDB.get(from);

            boolean callStarted = userCreditControl.startBillingSession(callID, to);

            // Check if the call was started - startBillingSession returns true if the call was started
            if (!callStarted) {
              // Failed to start a call due to lack of available credits

              // FIXME: Alert message might not be correctly implemented
              // sendSIPMessage(from, "Not enough credit to start a call!");
              // criar objeto response e fazer response.send response. create/make response
              // NAO É SENDSIPMESSAGE PQ TEM DE INTERROMPER O ESTABELECIMENTO DE LIGACAO - SE O CREDITO ACABAR DURANTE CHAMADA É SÓ O POPUP - SENDSIPMESSAGE

              response.createResponse(SipServletResponse.SC_PAYMENT_REQUIRED, "Not enough credit to start a call!").send();
            } 

          } else {
            // FIXME: CREATE A NEW CREDIT CONTROL OR JUST IGNORE?

            // response.createResponse(SipServletResponse.SC_NOT_FOUND, "User not found!").send();s
          }
        }
      }
      catch (Exception e) {
      logger.error( "==============> RM T2 logger: Failure in doSuccessResponse method.", e );
      }
    }


  @Override
  protected void doErrorResponse(SipServletResponse response ) throws ServletException, IOException
  {

    /*
     * TODO: Process Error Responses
     * VERIFICA O TIPO DE ERRO (404, 402)
     * 
     */

    try
    {
    logger.info("==============> RM T2 logger: Proccessing Error Response (" + response.getStatus() + ")...");

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
