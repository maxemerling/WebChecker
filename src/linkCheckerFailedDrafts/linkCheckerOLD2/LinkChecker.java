package linkCheckerFailedDrafts.linkCheckerOLD2;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.WebTools.HEROARTS;

public class LinkChecker extends Thread {

    private Set<String> visited;
    private LinkMap broken;
    private Object lock;

    static class LinkMap extends HashMap<String, List<String>> {
        private int size;

        public synchronized void add(Link link, String errorCode) {
            size++;
            if (!containsKey(link.address)) {
                List<String> list = new ArrayList<>();
                list.add(link.parent);
                put(link.address, list);
                System.out.println(errorCode + " | " + link);/**/
            } else {
                get(link.address).add(link.parent);
            }
        }

        @Override
        public int size() {
            return size;
        }
    }

    public LinkChecker() {
        visited = new HashSet<>();
        broken = new LinkMap();
        lock = new Object();
    }

    private static final Pattern PTAG = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
    private static final Pattern PLINK = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
    private void search(String html, String url, Searcher currSearcher) {
        //PARSE HTML FOR LINKS
        Matcher mTag = PTAG.matcher(html);
        while (mTag.find()) {
            String href = mTag.group(1);        //href value

            Matcher mLink = PLINK.matcher(href);
            while (mLink.find()) {
                String address = mLink.group(1)
                        .replaceAll("[\"']", "");

                if (address.indexOf('{') == -1) {
                    address = address.replaceAll(" ", "-");

                    boolean isHeroArts, isHTTP = true;
                    if (address.indexOf("//") == 0) {
                        address = "https:" + address;
                        isHeroArts = isHeroArts(address);
                    } else if (address.charAt(0) == '/') {
                        address = HEROARTS + address;
                        isHeroArts = true;
                    } else if (!(isHeroArts = isHeroArts(address))) {
                        isHTTP = isHTTP(address);
                    }

                    if (isHTTP) {
                        synchronized (this) {
                            if (!visited.contains(address)) {
                                startNewSearcher(new Link(address, url, isHeroArts), currSearcher);
                            }
                        }
                    }
                }
            }
        }
    }

    private void startNewSearcher(Link link, Searcher parent) {
        Searcher searcher = new Searcher(link, parent);
        searcher.start();
    }

    @Override
    public void run() {
        startNewSearcher(new Link(HEROARTS, null, true), null);
    }

    private static boolean isHTTP(String url) {
        return url.indexOf("http") == 0;
    }

    private static boolean isHeroArts(String url) {
        return url.indexOf(HEROARTS) == 0;
    }

    class Link {
        String address, parent;
        private boolean isHeroArts;

        public Link(String address, String parent, boolean isHeroArts) {
            this.address = address;
            this.parent = parent;
            this.isHeroArts = isHeroArts;
        }

        @Override
        public String toString() {
            return address + " | PARENT: " + parent;
        }
    }

    class Code {
        private int code;
        private boolean isValid;

        public Code(HttpURLConnection con) throws IOException {
            try {
                synchronized (lock) {
                    code = con.getResponseCode();
                }
            } catch (UnknownHostException e) {
                code = 500;
            } catch (SSLHandshakeException e) {
                code = 500;
            }
            isValid = code / 100 < 4;
        }

        public boolean isValid() {
            return isValid;
        }

        @Override
        public String toString() {
            return "" + code;
        }
    }

    class Searcher extends Thread {

        private Link link;
        private Searcher parent;

        public Searcher(Link link, Searcher parent) {
            super();
            this.link = link;
            this.parent = parent;
        }

        @Override
        public void run() {
            if (parent != null) {
                try {
                    parent.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            HttpURLConnection con = null;
            Code code;/**/
            try {
                synchronized (lock) {
                    //open connection
                    con = (HttpURLConnection) new URL(link.address).openConnection();
                    //disguise as a browser to bypass barriers against programmatic access
                    con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
                }
                if (link.isHeroArts) {
                    if ((code = new Code(con)).isValid()) {
                        //GET HTML SOURCE CODE AND PASS INTO getLinks() METHOD
                        /*
                         * Try-with-resources statement: automatically closes all resources declared in parentheses
                         * Any object that implements java.lang.AutoCloseable can be a resource
                         */
                        synchronized (lock) {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                                String inputLine;
                                StringBuilder builder = new StringBuilder();
                                while ((inputLine = reader.readLine()) != null) {
                                    builder.append(inputLine);
                                }
                                visited.add(link.address);
                                search(builder.toString(), link.address, this);
                            }
                        }
                    } else {
                        broken.add(link, code.toString());
                    }
                } else {
                    con.setRequestMethod("HEAD");
                    if ((code = new Code(con)).isValid()) {
                        visited.add(link.address);
                    } else {
                        broken.add(link, code.toString());
                    }
                }
            } catch (IOException e) {
                broken.add(link, "ERR");
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
    }

    public static void main(String[] args) {
        long start = System.nanoTime();


        LinkChecker linkChecker = new LinkChecker();

        Thread updater = new Thread() {

            JLabel label = new JLabel();
            JFrame frame = new JFrame();
            long start;

            @Override
            public void run() {
                frame.setPreferredSize(new Dimension(200, 50));
                frame.getContentPane().add(label);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.show();
                start = System.nanoTime();
                try {
                    loop();
                } catch (InterruptedException e) {
                    interrupt();
                }
            }

            @Override
            public void interrupt() {
                System.out.println("Time elapsed: " + (System.nanoTime() - start) / 60.0 * 1E-9 + " minutes.");
                super.interrupt();
            }

            private void loop() throws InterruptedException {
                label.setText("SEARCHED " + (linkChecker.visited.size() + linkChecker.broken.size) + " LINKS.");
                frame.repaint();
                sleep(1000);
                loop();
            }
        };
        updater.setDaemon(true);

        updater.start();
        linkChecker.start();
        //System.out.println(Runtime.getRuntime().availableProcessors());

        try {
            linkChecker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
