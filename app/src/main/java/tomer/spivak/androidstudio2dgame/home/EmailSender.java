package tomer.spivak.androidstudio2dgame.home;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends AsyncTask<Void, Void, Boolean> {

    private final String username; // Sender's email
    private final String password; // Sender's password
    private final String recipient; // Recipient's email
    private final String subject;   // Email subject
    private final String body;     // Email body

    public EmailSender(String username, String password, String recipient, String subject, String body) {
        this.username = username;
        this.password = password;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            // SMTP server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP host for Gmail
            props.put("mail.smtp.port", "587"); // Port for TLS

            // Create a session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create a MimeMessage
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            // Send the email
            Transport.send(message);
            return true; // Email sent successfully
        } catch (MessagingException e) {
            Log.e("EmailSender", "Error sending email", e);
            return false; // Email failed to send
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Log.d("EmailSender", "Email sent successfully!");
        } else {
            Log.e("EmailSender", "Failed to send email.");
        }
    }
}
