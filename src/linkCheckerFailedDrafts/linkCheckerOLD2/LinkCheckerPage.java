package linkCheckerFailedDrafts.linkCheckerOLD2;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.WebTools.HEROARTS;

public class LinkCheckerPage extends Thread {

    private Set<String> visited;
    private LinkMap broken;

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

    public LinkCheckerPage() {
        visited = new HashSet<>();
        broken = new LinkMap();
    }

    private static final Pattern PTAG = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
    private static final Pattern PLINK = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
    private void search(String html, String url) {
        Map<String, String> nextPages = new HashMap<>();

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
                        if (!(visited.contains(address) || nextPages.containsKey(address))) {
                            try {
                                HttpURLConnection con = getCon(address);
                                int code;
                                if (isHeroArts(address)) {
                                    if (isValidCode(code = con.getResponseCode())) {
                                        String currHtml = getHTML(con);
                                        nextPages.put(address, html);
                                    } else {
                                        broken.add(new Link(address, url),"" + code);
                                    }
                                } else {
                                    con.setRequestMethod("HEAD");
                                    if (isValidCode(code = con.getResponseCode())) {
                                        visited.add(address);
                                    } else {
                                        broken.add(new Link(address, url), "" + code);
                                    }
                                }
                            } catch (IOException e) {
                                broken.add(new Link(address, url), "ERR");
                            }
                        }

                        for (String page : nextPages.keySet()) {
                            visited.add(page);
                            if (isHeroArts(page)) {
                                search(page, nextPages.get(page));
                            }
                        }

                    }
                }
            }
        }
    }

    public void search(String url) {
        try {
            search(getHTML(getCon(url)), url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpURLConnection getCon(String url) throws IOException {
        //open connection
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        //disguise as a browser to bypass barriers against programmatic access
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        //set request method to head to speed up connection
        return con;
    }

    public static String getHTML(HttpURLConnection con) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }
            return builder.toString();
        }
    }

    public static boolean isValidCode(int code) {
        return code / 100 < 4;
    }

    @Override
    public void run() {
        search(HEROARTS);
    }

    private static boolean isHTTP(String url) {
        return url.indexOf("http") == 0;
    }

    private static boolean isHeroArts(String url) {
        return url.indexOf(HEROARTS) == 0;
    }

    class Link {
        private String address, parent;

        public Link(String address, String parent) {
            this.address = address;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return address + " | PARENT: " + parent;
        }
    }

    public static void main(String[] args) {
        long start = System.nanoTime();


        LinkCheckerPage linkChecker = new LinkCheckerPage();

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
                super.interrupt();
                System.out.println("Time elapsed: " + (System.nanoTime() - start) / 60.0 * 1E-9 + " minutes.");
            }

            private void loop() throws InterruptedException {
                label.setText("SEARCHED " + (linkChecker.visited.size() + linkChecker.broken.size) + " LINKS.");
                frame.repaint();
                Thread.sleep(1000);
                loop();
            }
        };
        updater.setDaemon(true);

        updater.start();
        linkChecker.start();
        try {
            linkChecker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
