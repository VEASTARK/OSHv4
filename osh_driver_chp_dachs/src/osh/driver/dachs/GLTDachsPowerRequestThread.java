package osh.driver.dachs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import osh.core.logging.IGlobalLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
@SuppressWarnings("deprecation")
public class GLTDachsPowerRequestThread implements Runnable {

    private final IGlobalLogger logger;

    private final boolean setOn;

    private final String dachsURL;
    private final String username;
    private final String password;


    /**
     * CONSTRUCTOR
     */
    public GLTDachsPowerRequestThread(
            IGlobalLogger logger,
            boolean setOn,
            String dachsURL,
            String username,
            String password) {
        this.logger = logger;
        this.setOn = setOn;
        this.dachsURL = dachsURL;
        this.username = username;
        this.password = password;
    }


    @Override
    public void run() {
        try {
            this.doPowerRequest();
        } catch (Exception e) {
            if (this.logger != null) {
                this.logger.logError("FIXME: ", e);
            }

            throw new RuntimeException(e); //re-throw
        }
    }

    private void doPowerRequest() {

        if (this.logger != null) {
            this.logger.logDebug("doPowerRequest()");
        }

        // Construct data
        List<NameValuePair> datalist = new ArrayList<>();
        if (this.setOn) {
            datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "1"));
        } else {
            datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "0"));
        }
        datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAnzahlModule", "1"));
        UrlEncodedFormEntity data;
        try {
            data = new UrlEncodedFormEntity(datalist, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            if (this.logger != null) {
                this.logger.logError("internal exception", e);
            }

            return;
        }

        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpPost httppost = new HttpPost(this.dachsURL + "setKeys");
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(
                            httppost.getURI().getHost(),
                            httppost.getURI().getPort()),
                    new UsernamePasswordCredentials(this.username, this.password));

            AuthCache authCache = new BasicAuthCache();
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(
                    new HttpHost(
                            httppost.getURI().getHost(),
                            httppost.getURI().getPort(),
                            httppost.getURI().getScheme()),
                    basicAuth);
            BasicHttpContext localcontext = new BasicHttpContext();
            localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

            httppost.setEntity(data);

            HttpResponse response = client.execute(httppost, localcontext);

            if (this.logger == null) {
                System.out.println("" + response);
            }

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        } catch (IOException ex) {
            if (this.logger != null) {
                this.logger.logError("changing dachs status failed", ex);
            }
        } finally {
            client.getConnectionManager().shutdown();
            client.close();
        }
    }

}
