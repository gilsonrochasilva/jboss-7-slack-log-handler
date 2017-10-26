package br.com.grsilva.slack;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class JBossLogSlackHandler extends Handler {

    private String token;
    private String asUser;
    private String username;
    private String channel;

    private static final Formatter FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord r) {
            Throwable thrown = r.getThrown();
            if (thrown != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                sw.write(r.getMessage());
                thrown.printStackTrace(pw);
                pw.flush();
                return sw.toString();
            } else {
                return r.getMessage();
            }
        }
    };

    @Override
    public void publish(LogRecord record) {
        try {
            sendMessage(getFormatter().format(record));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }

    private void sendMessage(String message) throws IOException {
        StringBuffer buildMessage = buildMessage(message);

        byte[] out = buildMessage.toString().getBytes(Charset.forName("UTF-8"));
        int length = out.length;

        HttpURLConnection httpURLConnection = buildConnection(length);

        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write(out);
        outputStream.flush();
        outputStream.close();

        httpURLConnection.disconnect();
    }

    private HttpURLConnection buildConnection(int length) throws IOException {
        URL url = new URL("https://slack.com/api/chat.postMessage");
        URLConnection urlConnection = url.openConnection();

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setFixedLengthStreamingMode(length);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        httpURLConnection.connect();

        return httpURLConnection;
    }

    private StringBuffer buildMessage(String message) throws UnsupportedEncodingException {
        Map<String,String> arguments = new HashMap<String, String>();
        arguments.put("token", token);
        arguments.put("channel", channel == null | channel.isEmpty() ? System.getenv("jboss.server.name") :  channel);
        arguments.put("text", message);
        arguments.put("as_user", asUser);
        arguments.put("username", username);

        StringBuffer buildMessage = new StringBuffer();
        for(Map.Entry<String,String> entry : arguments.entrySet()) {
            if (buildMessage.length() > 0) buildMessage.append("&");

            buildMessage.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            buildMessage.append("=");
            buildMessage.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return buildMessage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAsUser() {
        return asUser;
    }

    public void setAsUser(String asUser) {
        this.asUser = asUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}