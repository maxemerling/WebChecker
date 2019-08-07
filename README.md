# Overview

This is a project dedicated to checking for certain errors on the [HeroArts website](https://heroarts.com).


## Duplicate Checker

One part of the project scans through the [HeroArts Sitemap](https://heroarts.com/tools/sitemap), looking for duplicate products, pages, collections, and blogs.


## Broken Link Checker

The other part of this project scans through the entire HeroArts site, starting from the [home page](https://heroarts.com), looking for any links that return any sort of error.

It functions by running a recursive algorithm in the following order:
  1) Pull all HTTP hyperlinks from the current page by scanning through the HTML source code.
  2) For each link, check to see if it is accessible, and if so, get the HTTP response code.
      a) If the link is accessible AND returns an OK response code, start a new *thread\** that starts over at step (1) with this new link.
      b) If not, flag the link as broken, printing out the page on which is is found and a processed version of its HTML element.
     
\**This algorithm takes advantage of multithreading techniques, searching through multiple pages at the same time, to streamline the process.
