package org.kroky.html.extractor.gamebanshee;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * Hello world!
 *
 */
public class Main {
    private static final String TITLE_SELECTOR = "div.tcat_tl";
    private static final String CONTENT_SELECTOR = "div.gb13";
    private static final String DID_WE_MISS_ANYTHING_SELECTOR = "span.gb10";

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new RuntimeException(
                    "Program needs a URL as an argument. E.g. \"java -jar html-extractor-gamebanshee-1.0.jar http://www.gamebanshee.com/masseffect/walkthrough.php\"");
        }
        String url = args[0];
        Document doc = Jsoup.connect(url).get();
        Element rightcolumn = doc.getElementById("rightcolumn");
        Elements aElements = rightcolumn.select("a");

        List<String> extracts = aElements.stream().map(a -> a.attr("abs:href")).map(href -> getContentForLink(href))
                .collect(Collectors.toList());
        Files.write(Paths.get("output.html"), extracts);
    }

    private static String getContentForLink(String url) {
        Document doc;
        System.out.println(String.format("Processing URL: %s", url));
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("Could not get contents from %s", url);
        }
        Element titleElement = doc.selectFirst(TITLE_SELECTOR);
        Element extractRoot = doc.selectFirst(CONTENT_SELECTOR);
        Element didWeMissAnythingElement = extractRoot.selectFirst(DID_WE_MISS_ANYTHING_SELECTOR);
        while (didWeMissAnythingElement != null && didWeMissAnythingElement.parent() != null
                && !didWeMissAnythingElement.parent().equals(extractRoot)) {
            didWeMissAnythingElement = didWeMissAnythingElement.parent();
        }
        removeElement(didWeMissAnythingElement, true);

        // remove advertisement section
        removeAdSection(extractRoot);

        // prepend the title to the very beginning of this extract
        extractRoot.childNodes().get(0).before(titleElement.tagName("h2"));

        // make all urls absolute (i.e. http://<domain_name>/...)
        urlToAbsolute(extractRoot);

        return extractRoot.toString();
    }

    private static void urlToAbsolute(Element extractRoot) {
        Elements elements = extractRoot.select("img");
        elements.forEach(el -> el.attr("src", el.attr("abs:src")));
        elements = extractRoot.select("a");
        elements.forEach(el -> el.attr("href", el.attr("abs:href")));
    }

    private static void removeAdSection(Element extractRoot) {
        // everything between <!-- begin 300 x 250 advertisement --> ... <!-- end 300 x 250 advertisement -->

        Node adCommentNode = extractRoot.childNodes().stream().filter(node -> "#comment".equals(node.nodeName())
                && node.outerHtml().contains("begin 300 x 250 advertisement")).findFirst().orElse(null);
        if (adCommentNode != null) {
            Node sibling = adCommentNode.nextSibling();
            while (sibling != null && !sibling.outerHtml().contains("end 300 x 250 advertisement")) {
                sibling.remove();
                sibling = adCommentNode.nextSibling();
            }
        }
    }

    private static void removeElement(Element element, boolean inclTrailingBrTags) {
        if (element == null) {
            return;
        }
        if (inclTrailingBrTags) {
            Element nextElement;
            while ((nextElement = element.nextElementSibling()) != null
                    && nextElement.tagName().equalsIgnoreCase("br")) {
                nextElement.remove();
            }
        }
        element.remove();
    }
}
