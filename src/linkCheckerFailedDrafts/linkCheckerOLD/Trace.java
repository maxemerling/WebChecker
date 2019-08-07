package linkCheckerFailedDrafts.linkCheckerOLD;

import java.util.ArrayList;

public class Trace extends ArrayList<Tree.Node> {

    @Override
    public String toString() {
        String output = get(0).getLink().toString();
        for (int i = 1; i < size(); i++) {
            output += "\n" + tab(i) + "-->" + get(i).getLink();
        }

        return output;
    }

    public static String tab(int num) {
        String tabs = "";
        for (int i = 0; i < num; i++) {
            tabs += "\t";
        }
        return tabs;
    }
}
