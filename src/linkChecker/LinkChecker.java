package linkChecker;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.WebTools.HEROARTS;

public class LinkChecker extends Thread {

    private Set<String> visited;
    private LinkMap broken;

    private Joiner joiner;

     class Joiner extends Thread {

        @Override
        public void run() {
            join(LinkChecker.this);
        }

        void join(Thread t) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private LinkChecker() {
        super();
        visited = new HashSet<>();
        broken = new LinkMap();
        joiner = new Joiner();
    }

    private static final Pattern PTAG = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
    private static final Pattern PLINK = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
    private void search(String url, String html) {
        Map<Link, String> nextPages = new HashMap<>();

        //PARSE HTML FOR LINKS
        Matcher mTag = PTAG.matcher(html);
        while (mTag.find()) {
            String href = mTag.group(1);        //href value

            Matcher mLink = PLINK.matcher(href);
            while (mLink.find()) {
                String address = mLink.group(1)
                        .replaceAll("[\"']", "");

                if (!(address.indexOf('{') > -1 || address.charAt(0) == '#' || address.indexOf("mailto:") == 0)) {
                    address = address.replaceAll(" ", "-");

                    if (!isHTTP(address)) {
                        if (address.indexOf("//") == 0) {
                            address = "https:" + address;
                        } else if (address.charAt(0) == '/') {
                            address = HEROARTS + address;
                        } else {
                            address = "http://" + address;
                        }
                    }

                    synchronized (this) {
                        if (!visited.contains(address)) {
                            Link link = new Link(address, mTag.group(2), url);
                            try {
                                HttpURLConnection con = getCon(address);
                                int code;
                                if (isHeroArts(address)) {
                                    if (isValid(code = con.getResponseCode())) {
                                        String newHtml = getHtml(con);
                                        visited.add(address);
                                        nextPages.put(link, newHtml);
                                    } else {
                                        broken.add(link, code);
                                    }
                                } else {
                                    con.setRequestMethod("HEAD");
                                    if (isValid(code = con.getResponseCode())) {
                                        visited.add(address);
                                    } else {
                                        broken.add(link, code);
                                    }
                                }
                            } catch (IOException e) {
                                broken.add(link, "ERR");
                            }
                        }
                    }
                }
            }
        }

        //go through nextPages
        for (Link link : nextPages.keySet()) {
            Thread newThread = new Thread(() -> search(link.address, nextPages.get(link)));
            newThread.start();
            joiner.join(newThread);
        }
    }



    private String getHtml(HttpURLConnection con) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }
            return builder.toString();
        }
    }

    @Override
    public void run() {
        try {
            joiner.start();
            String html = getHtml(getCon(HEROARTS));
            search(HEROARTS, html);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValid(int code) {
        return code / 100 < 4;
    }

    private static HttpURLConnection getCon(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        return con;
    }

    private static boolean isHeroArts(String url) {
        return url.indexOf(HEROARTS) == 0;
    }

    private static boolean isHTTP(String url) {
        return url.indexOf("http") == 0;
    }

    public static void main(String[] args) {
        LinkChecker linkChecker = new LinkChecker();

        Thread updater = new Thread() {

            private JLabel label = new JLabel();
            private JFrame frame = new JFrame();
            private long start;

            @Override
            public void run() {
                frame.setPreferredSize(new Dimension(200, 50));
                frame.getContentPane().add(label);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

                start = System.nanoTime();
                while (linkChecker.joiner.isAlive()) {
                    loop();
                }

                interrupt();
            }

            @Override
            public void interrupt() {
                if (!isInterrupted()) {
                    System.out.println("Time elapsed: " + (System.nanoTime() - start) / 60.0 * 1E-9 + " minutes.");
                    super.interrupt();
                }
            }

            private void loop() {
                label.setText("SEARCHED " + (linkChecker.visited.size() + linkChecker.broken.size) + " LINKS." +
                        " Total Threads: " + Thread.activeCount());
                frame.repaint();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    interrupt();
                }
            }
        };

        linkChecker.start();
        updater.start();

        ///for some reason, after "Time elapsed" printed, the program was still running
    }
}
