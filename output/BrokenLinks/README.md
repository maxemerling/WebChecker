# Overview

This folder contains the output data from the LinkChecker program, a script that recursively checked all the links on the heroarts website, starting from the homepage (https://heroarts.com/), looking for broken or unresponsive links.


## Format

Each broken link entry contains the link itself, the page on which it was found, and the link element. The link element is the text, image, or other piece of HTML displayed to the user that, when clicked, tries to access the broken link.

The LinkChecker script tries to parse the html for the link element, so that if it is simple text on the page, it tries to give just that text. However, it sometimes fails to properly do this depending on the html statement used, and the text it gives for the "element" part of the entry may be pure html or half-parsed html.

For some links, the element is an image. In this case, the program displays the text *image* for the element. If it was able to successfully find a link to that image, the text *image* is hyperlinked to the image that represents the broken link's element on the page.


## Locating Broken Links on Webpage

If the ELEMENT appears to be useful (i. e. it gives clear, coherent text, or provides a link to an image), it can be used to quickly find where the broken hyperlink lies on the PARENT page. However, if the element displays unhelpful text (such as unparsed html), or no text at all, it is still relatively straightforward to "inspect element" or "view page source" on the PARENT page, and use the Cnrl+F or Cmd+F function to search for the broken url. In "inspect element," most browsers will highlight the location of the broken link on the page if you hover over it with your mouse, or click on it, in the html source code.


### Found Within heroarts.com

This folder organizes the broken links found on the heroarts website into various categories (listed below). The other folder, "Found Outside heroarts.com," is explained at the bottom of the README.md file.

It took the LinkChecker program exactly 5 hours to find this data, starting at 5:55 PM on 7/29/2019 and finishing at 10:55 PM. The program searched through 59,120 links total, and found 2,130 broken links.


#### heroarts.com(products).md

This file contains all broken links under the directory https://heroarts.com/products/.


#### heroarts.com(shop-sidebar).md

This file contains all broken links under the directory https://heroarts.com/shop-sidebar/.


#### heroarts.com(collections).md

This file contains all broken links under the directory https://heroarts.com/collections/.


#### heroarts.com(blogs).md

This file contains all broken links under the directory https://heroarts.com/blogs/.


#### heroarts.com(other).md

This file contains all other broken links under the directory https://heroarts.com/, not fitting into any of the above-listed categories.


#### blogs.heroarts.com.md

This file contains all broken hyperlinks that link to the blog.heroaerts.com site.


#### other.md

This file contains all broken hyperlinks found on the heroarts website that link to sites other than those covered in the above category (for instance, this might include old facebook pages that no longer exist).


### Found Outside heroarts.com

While testing an earlier version of the LinkChecker script, I had accidentally forgotten to account for the fact that the program should avoid going into and checking through pages not on the heroarts site. As a result, it was trying to check the entire internet for broken links before I stopped it and fixed the error. Interestingly, it found some broken references to the heroarts site (i. e. on facebook and twitter) that I decided to include in this folder.
