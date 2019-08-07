package tools;

import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public interface WebTools {

    /**
     * The url to the HeroArts home page
     */
    public static final String HEROARTS = "https://heroarts.com";

    /**
     * Gets the HTML source code for the webpage.
     * @param url the url of the webpage
     * @return a String containing the HTML source code of the webpage
     * @throws IOException
     */
    public static String getHTML(String url) throws IOException {
        URL urlObj = new URL(url);
        URLConnection con = urlObj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        /*
         * Try-with-resources statement: automatically closes all resources declared in parentheses
         * Any object that implements java.lang.AutoCloseable can be a resource
         */
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }
            return builder.toString();
        }
    }

    public static boolean isValid(String link) {
        try {
            return getResponse(link) / 100 > 3;
        } catch (IOException e) {
            return false;
        }
    }

    public static HttpURLConnection getCon(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
        //pretend to be a browser
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        //
        con.setRequestMethod("HEAD");

        return con;
    }

    public static int getResponse(String url) throws IOException {
        try {
            return getCon(url).getResponseCode();
        } catch (SSLHandshakeException e) {
            return 404;
        }
    }

    public static boolean isHeroArts(String link) {
    	return link.indexOf(HEROARTS) == 0;
    }
}
