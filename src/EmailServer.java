

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailServer {


    private static final String PROPERTY_FILE_PATH = "/UMARF/safclient/autoUMARF/serviceTools/monitoringTool.properties";
    private static final String MAILSERVER_KEY = "MAILSERVER";
    private static final String USERNAME_KEY = "USERNAME";
    private static final String PASSWORD_KEY = "PASSWORD";
    private static final String SENDER_KEY = "SENDER";
    private static final String SUBJECT_KEY = "SUBJECT";
    private static final String COMMON_RECIPIENT_KEY = "COMMON_RECIPIENT";
    private static final String PREP_RECIPIENT_KEY = "PREP_RECIPIENT";
    private static final String SOIL_RECIPIENT_KEY = "SOIL_RECIPIENT";
    private static final String SNOW_RECIPIENT_KEY = "SNOW_RECIPIENT";
    private String mailServer = "server";
    private String username = "user";
    private String password = "pw";
    private String sender = "sender";
    private String subject = "subject";
    private ArrayList<InternetAddress> commonRecipient = new ArrayList<InternetAddress>();
    private ArrayList<InternetAddress> prepRecipient = new ArrayList<InternetAddress>();
    private ArrayList<InternetAddress> soilRecipient = new ArrayList<InternetAddress>();
    private ArrayList<InternetAddress> snowRecipient = new ArrayList<InternetAddress>();

    public EmailServer() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(PROPERTY_FILE_PATH);
            // Load properties file
            prop.load(input);
            // Read E-mail server configuration
            this.mailServer = prop.getProperty(MAILSERVER_KEY);
            this.username = prop.getProperty(USERNAME_KEY);
            this.password = prop.getProperty(PASSWORD_KEY);
            this.sender = prop.getProperty(SENDER_KEY);
            this.subject = prop.getProperty(SUBJECT_KEY);
            // Read E-mail addresses of common recipients
            String[] commonRecipientAddresses = prop.getProperty(COMMON_RECIPIENT_KEY).split(";");
            for (String address : commonRecipientAddresses) {
                // If condition to manage an empty ("empty" means "") key value
                if (address.contains("@")) {
                    this.commonRecipient.add(new InternetAddress(address.trim()));
                }
            }
            // Read E-mail addresses of precipitation recipients
            String[] prepRecipientAddresses = prop.getProperty(PREP_RECIPIENT_KEY).split(";");
            for (String address : prepRecipientAddresses) {
                // If condition to manage an empty ("empty" means "") key value
                if (address.contains("@")) {
                    this.prepRecipient.add(new InternetAddress(address.trim()));
                }
            }
            // Read E-mail addresses of soil moisture recipients
            String[] soilRecipientAddresses = prop.getProperty(SOIL_RECIPIENT_KEY).split(";");
            for (String address : soilRecipientAddresses) {
                // If condition to manage an empty ("empty" means "") key value
                if (address.contains("@")) {
                    this.soilRecipient.add(new InternetAddress(address.trim()));
                }
            }
            // Read E-mail addresses of soil snow recipients
            String[] snowRecipientAddresses = prop.getProperty(SNOW_RECIPIENT_KEY).split(";");
            for (String address : snowRecipientAddresses) {
                // If condition to manage an empty ("empty" means "") key value
                if (address.contains("@")) {
                    this.snowRecipient.add(new InternetAddress(address.trim()));
                }
            }
        } catch (Throwable ex) {
            System.out.println("Problem in email server construction: " + ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }

    public void sendmailHsaf(String prodId, String msgStr, String cluster) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", this.mailServer);
            props.put("mail.smtp.auth", "true");
            Authenticator authenticator = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            Session session = Session.getInstance(props, authenticator);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(this.sender));
            ArrayList<InternetAddress> addressesTo = new ArrayList<InternetAddress>();
            addressesTo.addAll(this.commonRecipient);
            if (cluster.equals(Main.PREP_cl)) {
                addressesTo.addAll(this.prepRecipient);
            } else if (cluster.equals(Main.SOIL_cl)) {
                addressesTo.addAll(this.soilRecipient);
            } else if (cluster.equals(Main.SNOW_cl)) {
                addressesTo.addAll(this.snowRecipient);
            }
            InternetAddress[] toArray = addressesTo.toArray(new InternetAddress[addressesTo.size()]);
            msg.setRecipients(Message.RecipientType.TO, toArray);
            String mailSubj = String.format("%s (%s)", this.subject.trim(), prodId);
            msg.setSubject(mailSubj);
            msg.setContent(msgStr, "text/plain");
            Transport.send(msg);
        } catch (Exception ex) {
            System.out.println("E-MAIL Server failure: " + ex.toString());
        }
    }

}
