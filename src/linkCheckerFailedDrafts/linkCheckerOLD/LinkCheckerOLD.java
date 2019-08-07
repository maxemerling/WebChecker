package linkCheckerFailedDrafts.linkCheckerOLD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tools.WebTools.*;

/**
 * NOTE: the current version of this program only checks links starting with https://heroarts.com
 * In other words, pages starting with blog.heroarts.com will not be checked/
 */
public class LinkCheckerOLD {

    private static final String HTML_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
    private static final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
    private static final Pattern PTAG = Pattern.compile(HTML_TAG_PATTERN);
    private static final Pattern PLINK = Pattern.compile(HTML_HREF_TAG_PATTERN);

    /**
     * Gets a list of all hyperlinks in a piece of html source code.
     * @param html the html source code
     * @return a List containing Link objects for each hyperlink in the source code
     */
    public static List<Link> getLinksFromHTML(String html) {
        List<Link> elements = new ArrayList<>();
        Matcher mTag = PTAG.matcher(html);

        while (mTag.find()) {
            String href = mTag.group(1);        //href value
            String linkElem = mTag.group(2);    //text of the link element

            Matcher mLink = PLINK.matcher(href);

            while (mLink.find()) {
                String link = mLink.group(1)
                        .replaceAll("[\"']", "");

                if (link.indexOf('{') == -1) {
                    if (link.charAt(0) == '/') {
                        link = HEROARTS + link;
                        elements.add(new Link(link, linkElem, true));
                    } else if (link.indexOf("http") == 0) {
                        elements.add(new Link(link, linkElem));
                    }
                }
            }
        }

        return elements;
    }

    public static List<Link> getLinks(String url) {
        try {
            return getLinksFromHTML(getHTML(url));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Recursive searcher
     */
    private static void searchAndAddChildren(Tree tree, Tree.Node parent) {
        for (Link link : getLinks(parent.getUrl())) {
            synchronized (tree) {
                if (!tree.contains(link.getAddress())) {
                    parent.addChild(link);
                }
            }
        }

        //If there are no children, or all children are outside heroarts site, method will end
        for (Tree.Node child : parent.getChildren()) {
            System.out.println(child.getTrace());
            if (child.isHeroArts() && child.isValid()) {
                searchAndAddChildren(tree, child);
            }
        }
    }

    private static void mapSite(Tree tree) {
        searchAndAddChildren(tree, tree.getRoot());
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        List<Link> links = new ArrayList<>();
        links = getLinks(HEROARTS);
        links.addAll(getLinks("https://heroarts.com/pages/giving"));
        links.addAll(getLinks("https://heroarts.com/collections/all"));
        links.addAll(getLinks("https://heroarts.com/collections/all/products/pr101-ha-gina-k-friendship-blooms"));
        links.addAll(getLinks("https://heroarts.com/pages/subscribe"));

        List<String> urls = new ArrayList<>();
        for (Link link : links) {
            System.out.println(link);
        }


        /*
        try {
            System.out.println(isValid("https://pinterest.com/pin/create/button/?url=https://heroarts.com/products/di515-color-layering-armadillo-frame-cuts-c&description=DI515"));
            System.out.println(isValid(HEROARTS));
            System.out.println(isValid("https://blog.heroarts.com"));
        } catch (IOException e) {
            e.printStackTrace();
        }
         */

        System.out.println("\n\nTime elapsed: " + (int) ((System.nanoTime() - startTime) * 1E-9) + " seconds.");
    }
}
