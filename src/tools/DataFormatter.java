package tools;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataFormatter {

    private static final String SEPARATOR = "---------------------------------------------------------------------------------------------------------";

    private static final String FOLDER = "C:\\Users\\maxem\\Documents\\HeroArts\\WebChecker\\output\\";
    private static final String INPUT_FILE = FOLDER + "Unformatted Output\\broken_links.txt";
    private static final String OUTPUT_PATH = FOLDER + "BrokenLinks\\Found Within heroarts.com\\";

    public static final String HEROARTS = "https://heroarts.com/";

    //output categories
    public static final String HEROARTS_MAIN = "heroarts.com", PRODUCTS = "products", COLLECTIONS = "collections",
            BLOGS = "blogs", SHOP = "shop-sidebar", BLOG_SITE = "blog.heroarts.com", OTHER = "other";
    public static void main(String[] args) {
        Object[] input;

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(INPUT_FILE)))) {
            input = reader.lines().toArray();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Total number of broken links: " + input.length);

        Map<String, List<String>> output = new HashMap<>();

        output.put(HEROARTS_MAIN, new ArrayList<>());
        output.put(PRODUCTS, new ArrayList<>());
        output.put(COLLECTIONS, new ArrayList<>());
        output.put(BLOGS, new ArrayList<>());
        output.put(SHOP, new ArrayList<>());
        output.put(BLOG_SITE, new ArrayList<>());
        output.put(OTHER, new ArrayList<>());

        for (Object lineObj : input) {
            String line = preFormat((String) lineObj);

            if (line.indexOf(HEROARTS) == 0) {
                if (line.indexOf(HEROARTS + PRODUCTS) == 0) {
                    output.get(PRODUCTS).add(line);
                } else if (line.indexOf(HEROARTS + COLLECTIONS) == 0) {
                    output.get(COLLECTIONS).add(line);
                } else if (line.indexOf(HEROARTS + BLOGS) == 0) {
                    output.get(BLOGS).add(line);
                } else if (line.indexOf(HEROARTS + SHOP) == 0) {
                    output.get(SHOP).add(line);
                } else {
                    output.get(HEROARTS_MAIN).add(line);
                }
            } else if (line.indexOf("https://" + BLOG_SITE) == 0 || line.indexOf("http://" + BLOG_SITE) == 0) {
                output.get(BLOG_SITE).add(line);
            } else {
                output.get(OTHER).add(line);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\maxem\\Documents\\HeroArts\\WebChecker\\output\\Unformatted Output\\" +
                "external_broken_links.txt")))) {
            input = reader.lines().toArray();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        output.put("external", new ArrayList<>());
        for (Object lineObj : input) {
            String line = (String) lineObj;
            if (line.indexOf('|') > -1) {
                line = preFormat(line);
                if (line.indexOf("https://heroarts.com") == 0 || line.indexOf("http://blog.heroarts.com") == 0 ||
                        line.indexOf("https://blog.heroarts.com") == 0) {
                    line = line.substring(0, line.indexOf('|')) + line.substring(line.lastIndexOf('|'))
                            + line.substring(line.indexOf('|'), line.lastIndexOf('|'));
                    output.get("external").add(line);
                }
            }
        }

        List<String> lines = new ArrayList<>();

        Map<String, String> fileNames = Map.of
                (
                        PRODUCTS, OUTPUT_PATH + "heroarts.com(" + PRODUCTS + ')',
                        COLLECTIONS, OUTPUT_PATH + "heroarts.com(" + COLLECTIONS + ')',
                        SHOP, OUTPUT_PATH + "heroarts.com(" + SHOP + ')',
                        BLOGS, OUTPUT_PATH + "heroarts.com(" + BLOGS + ')',
                        HEROARTS_MAIN, OUTPUT_PATH + "heroarts.com(" + OTHER + ')',
                        OTHER, OUTPUT_PATH + OTHER,
                        BLOG_SITE, OUTPUT_PATH + BLOG_SITE,
                        "external", "C:\\Users\\maxem\\Documents\\HeroArts\\WebChecker\\output\\BrokenLinks\\Found Outside heroarts.com\\"
                                + "external"
                );

        for (String name : fileNames.keySet()) {
            try (BufferedWriter writer = new BufferedWriter((new FileWriter(new File(fileNames.get(name) + ".md"))))) {
                int i = 1;
                for (String line : output.get(name)) {
                    int marker;
                    writer.write("#### Link " + i++ + ':');
                    writer.newLine();
                    writer.write(line.substring(0, marker = line.indexOf('|')));
                    writer.newLine();
                    writer.write("- LOCATION: " + line.substring(marker + 1, marker = line.lastIndexOf('|')));
                    writer.newLine();

                    String element = line.substring(marker + 1).replace("IMAGE:", "IMAGE: ");
                    if (element.indexOf("IMAGE:") > -1) {
                        element = element.substring(element.indexOf("IMAGE:") + "IMAGE:".length());
                        if (element.indexOf("cdn.shopify.com") > -1) {
                            element = "[*image*](" + element + ')';
                        } else {
                            element = "*image*";
                        }
                    }
                    writer.write("- ELEMENT: " + element);
                    writer.newLine();
                    writer.write(SEPARATOR);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String preFormat(String s) {
        return s.substring(6).replaceAll(" ", "").replaceAll("PARENT:", "")
                .replaceAll("ELEMENT:", "");
    }
}