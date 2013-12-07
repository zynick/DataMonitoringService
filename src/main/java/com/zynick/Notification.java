package com.zynick;

import java.io.IOException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.joda.time.DateTime;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.zynick.commons.Configuration;
import com.zynick.commons.Email;
import com.zynick.commons.Email.Protocol;
import com.zynick.commons.Logger;

/**
 * Notify us when there is no input into database
 * @author zynick
 */
public class Notification {
    
    // configuration key
    public static final String MONGO_HOST       = "MONGO_HOST";
    public static final String MONGO_PORT       = "MONGO_PORT";
    public static final String MONGO_DB         = "MONGO_DB";
    public static final String MONGO_COLLECTION = "MONGO_COLLECTION";
    public static final String MONGO_DATE_KEY   = "MONGO_DATE_KEY";
    public static final String MAIL_HOST        = "MAIL_HOST";
    public static final String MAIL_PROTOCOL    = "MAIL_PROTOCOL";
    public static final String MAIL_USER        = "MAIL_USER";
    public static final String MAIL_PASS        = "MAIL_PASS";
    public static final String MAIL_RECIPIENT   = "MAIL_RECIPIENT";
    public static final String MAIL_SUBJECT_OFF = "MAIL_SUBJECT_OFF";
    public static final String MAIL_CONTENT_OFF = "MAIL_CONTENT_OFF";
    public static final String MAIL_SUBJECT_ON  = "MAIL_SUBJECT_ON";
    public static final String MAIL_CONTENT_ON  = "MAIL_CONTENT_ON";
    public static final String INTERVAL         = "INTERVAL";
    public static final String RANGE            = "RANGE";
    public static final String EXPECTED_COUNT   = "EXPECTED_COUNT";

    public static final String DATE_FORMAT     = "yyyy-MM-dd HH:mm:ss zzz";

    private Configuration config;

    // Mongo Configuration
    private final DBCollection collection;
    private final String dateKey;

    // Mail Configuration
    private final Email email;
    private final String[] recipients;
    private final String subjectOff;
    private final String contentOff;
    private final String subjectOn;
    private final String contentOn;

    // Notification Configuration
    private final int interval;
    private final int range;
    private final int expected;

    // Trigger On/Off Mode Checking
    private boolean flag;
    
    public Notification(String path) throws IOException, AddressException {
        
        // get configuration from file
        config = new Configuration("config.txt");
        
        // setup mongo
        String dbHost = config.get(MONGO_HOST);
        int port = Integer.parseInt(config.get(MONGO_PORT));
        String db = config.get(MONGO_DB);
        String col = config.get(MONGO_COLLECTION);
        
        MongoClient client = new MongoClient(new ServerAddress(dbHost, port));
        DB database = client.getDB(db);
        collection = database.getCollection(col);
        
        dateKey = config.get(MONGO_DATE_KEY);
        
        // setup mail
        String mHost = config.get(MAIL_HOST);
        Protocol protocol = "TLS".equals(config.get(MAIL_PROTOCOL)) ? Protocol.TLS : Protocol.SSL;
        String user = config.get(MAIL_USER);
        String pass = config.get(MAIL_PASS);
        email = new Email(mHost, protocol, user, pass);
        
        recipients = config.get(MAIL_RECIPIENT).split("\\s*,\\s*");
        subjectOff = config.get(MAIL_SUBJECT_OFF);
        contentOff = config.get(MAIL_CONTENT_OFF);
        subjectOn = config.get(MAIL_SUBJECT_ON);
        contentOn = config.get(MAIL_CONTENT_ON);
        
        // setup notification configuration
        range = Integer.parseInt(config.get(RANGE));
        expected = Integer.parseInt(config.get(EXPECTED_COUNT));
        interval = Integer.parseInt(config.get(INTERVAL));
        
        // setup flag
        flag = false;
    }

    public void execute() {
        
        // infinite loop
        while(true) {
            
            DateTime jodate = new DateTime().minusMinutes(range);
            Date date = new Date(jodate.getMillis());

            // check db for data
            DBCursor cur = collection.find(
                    new BasicDBObject(dateKey, new BasicDBObject("$gte", date)));
            
            // data is on
            if (flag) {
                if (cur.count() < expected) {
                    // switch
                    flag = !flag;
                    
                    // prepare
                    String subject = String.format(subjectOff);
                    String content = String.format(contentOff, jodate.toString(DATE_FORMAT));
                    
                    try {
                        // send mail
                        email.send(subject, content, recipients);
                        Logger.INSTANCE.log("mail sent: " + subject);
                    } catch (MessagingException e) {
                        Logger.INSTANCE.log("can't send mail: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    }
                }
                
            // data is off
            } else {
                if (cur.count() >= expected) {
                    // switch
                    flag = !flag;
                    
                    // prepare
                    String subject = String.format(subjectOn);
                    String content = String.format(contentOn, jodate.toString(DATE_FORMAT));
                    
                    // send
                    try {
                        email.send(subject, content, recipients);
                        Logger.INSTANCE.log("mail sent: " + subject);
                    } catch (MessagingException e) {
                        Logger.INSTANCE.log("can't send mail: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    }
                }
            }
            
            cur.close();
            
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public static void main(String[] args) throws Exception {

        Notification n = new Notification(args[0]);
        
        Logger.INSTANCE.log("process started.");
        try {
            n.execute();
        } finally {
            Logger.INSTANCE.log("process stopped.");
        }
    }
}
