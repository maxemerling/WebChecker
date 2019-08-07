package duplicateChecker;

import static tools.WebTools.HEROARTS;
import static tools.WebTools.getHTML;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans through the Hero Arts site map (<a href="https://heroarts.com/tools/sitemap">https://heroarts.com/tools/sitemap</a>) and checks for duplicate collections, products, pages, and blogs.
 * @author Max Emerling
 */
public class DuplicateChecker {
	
	private static final String URL_BASE = HEROARTS + "/tools/sitemap?page=";
	
	private static final String SECTION_HEADER_BASE = "list-row type_";
	private static final String COLLECTIONS = "collections", PRODUCTS = "products", PAGES = "pages", BLOGS = "blogs";
	private static final String[] TYPES = new String[] {COLLECTIONS, PAGES, BLOGS, PRODUCTS};
	private static final String SECTION_END = "</ul>";
	
	private static final String ITEM_START = " <a href=\"/";
	private static final char LINK_END = '"';
	private static final char NAME_START = '>';
	private static final String ITEM_END = "/a>";
	private static final int PRODUCT_NUM_LEN = 6;
	
	private static final int NUM_PAGES = 15;



	private static String getPage(int i) {
		try {
			return getHTML(URL_BASE + i);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map getDuplicates() {
		String html = getPage(1);

		Map<String, Location> names = new HashMap<>();
		Locations urls = new Locations();

		//for products only
		Map<String, Location> numbers = new HashMap<>();

		Map<String, ArrayList<String>> duplicates = new HashMap<>();

		for (String type : TYPES) {

			duplicates.put(type, new ArrayList<String>());

			for (Item i : (type.equals(PRODUCTS) ? getProducts() : getItems(type, html))) {
				Locations locs = new Locations();
				int myPage = i.getPage();
				String url = i.getURL();
				Location loc = new Location(url, myPage);
				locs.add(loc);

				String dupStr = "";

				if (type.equals(PRODUCTS)) {
					String number = ((Product) i).getNumber();

					if (!number.isEmpty()) {
						Location numLoc = numbers.get(number);
						if (numLoc != null) {
							dupStr += number;
							if (!locs.contains(numLoc)) {
								locs.add(numLoc);
							}
						} else {
							numbers.put(number, loc);
						}
					}
				}

				String name = i.getName();

				Location nameLoc = names.get(name);
				if (nameLoc != null) {
					dupStr += (dupStr.isEmpty() ? "" : " ") + name;
					int namePage;
					if (!locs.contains(nameLoc)) {
						locs.add(nameLoc);
					}
				} else {
					names.put(name, loc);
				}

				Location duplicateLoc = urls.get(url);
				if (duplicateLoc != null) {
					dupStr += (dupStr.isEmpty() ? "" : " ") + url;
					int urlPage;
					if (!locs.contains(duplicateLoc)) {
						locs.add(duplicateLoc);
					}
				} else {
					urls.add(loc);
				}

				if (!dupStr.isEmpty()) {
					for (Location l : locs) dupStr += "\n\t" + l;

					duplicates.get(type).add(dupStr);
				}
			}

			names.clear();
			urls.clear();

		}

		return duplicates;
	}

	private static ArrayList<Item> getItems(String type, String html) {
		ArrayList<Item> items = new ArrayList<Item>();

		for (String line : getSectionLines(type, html)) {
			items.add(getItem(line));
		}

		return items;
	}

	private static ArrayList<Item> getProducts() {

		ArrayList<Item> products = new ArrayList<Item>();

		//get the rest of the products
		for (int i = 1; i <= NUM_PAGES; i++) {
			for (String line : getSectionLines(PRODUCTS, getPage(i))) {
				products.add(getProduct(line, i));
			}
		}

		return products;
	}

	private static String getSection(String type, String html) {
		html = html.substring(html.indexOf(SECTION_HEADER_BASE + type));
		html = html.substring(0, html.indexOf(SECTION_END));

		return html;
	}

	private static ArrayList<String> getSectionLines(String type, String html) {
		String section = getSection(type, html);

		ArrayList<String> lines = new ArrayList<>();

		int nextRowIdx;
		while ((nextRowIdx = section.indexOf(ITEM_START)) != -1) {
			int rowEndIdx = section.indexOf(ITEM_END, nextRowIdx) - 1;
			lines.add(section.substring(nextRowIdx + ITEM_START.length(), rowEndIdx));
			section = section.substring(rowEndIdx);
		}
		
		return lines;
	}

	private static Item getItem(String line) {
		return new Item(line.substring(0, line.indexOf(LINK_END)), line.substring(line.indexOf(NAME_START) + 1));
	}

	private static Product getProduct(String line, int page) {
		int marker;

		String number = line.substring((marker = line.indexOf(NAME_START) + 1), marker = marker + PRODUCT_NUM_LEN).trim();
		String name = line.substring(marker);

		if (!number.replaceAll("[A-Z0-9]", "").isEmpty()) {
			name = number + name;
			number = "";
		}

		return new Product(
				line.substring(0, line.indexOf(LINK_END)),
				number,
				name,
				page);
	}

	public static void main(String[] args) {
		long startTime = System.nanoTime();

		Map duplicates = getDuplicates();

		for (String type : TYPES) {
			System.out.println(type.toUpperCase() + ":\n");

			for (String str : (List<String>) duplicates.get(type)) {
				System.out.println(str);
				System.out.println("---------------------------------------------------------------------------------");
			}

			System.out.println("\n");
		}

		System.out.println("Time elapsed: " + (int) ((System.nanoTime() - startTime) * 1E-9) + " seconds.");
	}
}
