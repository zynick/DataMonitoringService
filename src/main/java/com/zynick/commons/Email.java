package com.zynick.commons;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email Sender
 * @author zynick
 */
public class Email {
    
    public static enum Protocol { TLS, SSL }
    
    private InternetAddress userAddress;
    private final Session session;

    /**
     * Email Sender. Configure once and send multiple times.
     * @param host email server
     * @param protocol TLS or SSL. default is SSL
     * @param user username
     * @param pass password
     * @throws AddressException
     */
    public Email(String host, Protocol protocol, String user, String pass) throws AddressException {

        /*
         * https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html
         */
        Properties prop = new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        
        if (protocol == Protocol.TLS) {
            // tls
            prop.put("mail.smtp.port", "587");
        } else {
            // ssl
            prop.put("mail.smtp.port", "465");
            prop.put("mail.smtp.socketFactory.port", "465");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        UserAuthenticator auth = new UserAuthenticator(user, pass);
        
        userAddress = new InternetAddress(user);
        session = Session.getDefaultInstance(prop, auth);
    } 

    public void send(String subject, String body, String... recipients) throws MessagingException {

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(userAddress);
        for (String recipient : recipients)
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        msg.setSubject(subject);
        msg.setText(body);

        Transport trans = session.getTransport("smtp");
        trans.connect();
        trans.sendMessage(msg, msg.getAllRecipients());
        trans.close();
    }
    
    
    
    private class UserAuthenticator extends Authenticator {

        private final PasswordAuthentication auth;

        public UserAuthenticator(String user, String pass) {
            super();
            auth = new PasswordAuthentication(user, pass);
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }
    }
}
