package duplicateChecker;

import static tools.WebTools.HEROARTS;

/**
 * Marks the location of an Item in the sitemap via a page number in the sitemap and an associated url
 * If the Item is a url, then it can be represented and located with this object.
 */
public class Location {
    private String url;
    private int page;

    public Location(String url, int page) {
        this.url = url;
        this.page = page;
    }

    public String getURL() {
        return url;
    }

    public int getPage() {
        return page;
    }

    @Override
    public boolean equals(Object obj) {
        Location loc = (Location) obj;
        return this.page == loc.page && this.url.equals(loc.url);
    }

    @Override
    public String toString() {
        return String.format("%1$3s", page) + " | " + HEROARTS + "/" + url;
    }
}
