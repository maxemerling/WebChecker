This is a mini-project dedicated to finding duplicate collections, products, pages, and blogs on the heroarts.com website.

This is done by scanning through the sitemap and noting when a certain hyperlinks, name, or product number occurs more than once.
The current version ignores cross-section repeats (i. e. if a certain name occurs in both "collections" and in "pages," and only
focuses on if each section contains repeated listings.

All pages of the "products" section are considered, and the program will return which pages the repeated listings are on for easy
manual find/replace verification.

NOTE: this program may return some false positives (see "drawbacks"), so manually checking the output to make sure the repeats actually exist is
recommended.



Drawbacks of the current version:

The few products that have IDs longer than 5 characters won't be parsed correctly (the program only reads the first 5 digits of the ID),
so these could yield false positives.

The program returns repeated hyperlinks, product IDs, and listing names separately. This allows it to be clear when, for example,
a product ID is listed twice with two different names; however, it can cause fully duplicated products to be returned multiple times.
