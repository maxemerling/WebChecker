package duplicateChecker;

import java.util.ArrayList;

public class Locations extends ArrayList<Location> {
    public Location get(String url) {
        for (Location l : this) {
            if (l.getURL().equals(url)) {
                return l;
            }
        }

        return null;
    }

    @Override
    public boolean contains(Object obj) {
        Location loc = (Location) obj;

        for (Location l : this) {
            if (l.equals(loc)) {
                return true;
            }
        }

        return false;
    }
}
