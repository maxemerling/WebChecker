package linkChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LinkMap extends HashMap<String, List<String>> {
    int size;

    synchronized void add(Link link, String errorCode) {
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

    synchronized void add(Link link, int errorCode) {
        add(link, "" + errorCode);
    }

    @Override
    public int size() {
        return size;
    }
}