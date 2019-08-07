package linkCheckerFailedDrafts.linkCheckerOLD;

import tools.WebTools;

public class Link {
    private String address, element;
    private boolean isHeroArts;

    //constants for formatting link
    private static final char POUND = '#';


    //constants for formatting element
    private static final String IMG_INDICATOR = "<img";
    private static final String IMG_START = "src=\"//";
    private static final char IMG_END = '"';
    private static final String SPAN_START = "<span>", SPAN_END = "</span>";
    private static final String ICLASS = "</i>";
    private static final String SPAN_CLASS_START = "<span class=\"", SPAN_CLASS_END = "></span>";

    private void initVars(String address, String element) {
        this.address = address;
        this.element = formatElement(element);
    }

    public Link(String address, String element) {
        initVars(address, element);
        isHeroArts = WebTools.isHeroArts(address);
    }

    public Link(String address, String element, boolean isHeroArts) {
        initVars(address, element);
        this.isHeroArts = isHeroArts;
    }
    
    public String getAddress() {
    	return address;
    }
    
    public String getElement() {
    	return element;
    }

    private static String formatElement(String element) {
        element = element.trim();

        int marker;
        if ((marker = element.indexOf(IMG_INDICATOR)) > -1) { //contains an image
            element = "IMAGE: http://" +
                    element.substring(marker = element.indexOf(IMG_START, marker) + IMG_START.length(), element.indexOf(IMG_END, marker));
        } else if ((marker = element.indexOf(SPAN_START)) > -1) {
            element = element.substring(marker += SPAN_START.length(), element.indexOf(SPAN_END, marker));
        } else if ((marker = element.indexOf(ICLASS)) > -1) {
            element = element.substring(marker + ICLASS.length());
        } else if ((marker = element.indexOf(SPAN_CLASS_START)) == 0) {
            element = element.substring(marker += SPAN_CLASS_START.length(), element.indexOf('"', marker));
        } else if ((marker = element.indexOf("<center><")) > -1) {
            element = element.substring(marker = element.indexOf('>', marker), element.indexOf('<', marker));
        } else if ((marker = element.indexOf("  ")) > -1) {
            element = element.substring(0, marker);
        }

        return element;
    }

    public boolean isHeroArts() {
        return isHeroArts;
    }

    @Override
    public String toString() {
        return address + " | " + element;
    }
}
