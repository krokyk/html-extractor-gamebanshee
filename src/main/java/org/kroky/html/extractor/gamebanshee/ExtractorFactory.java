package org.kroky.html.extractor.gamebanshee;

public final class ExtractorFactory {

    private static final ExtractorFactory instance = new ExtractorFactory();

    private ExtractorFactory() {
    }

    public static ExtractorFactory getInstance() {
        return instance;
    }

    public Extractor newExtractor(String url) {
        if (url.contains("www.gamebanshee.com/masseffect/")) {
            return new ME1Extractor();
        } else if (url.contains("www.gamebanshee.com/masseffect2/")) {
            return new ME2Extractor();
        } else {
            throw new RuntimeException(String.format("Extractor not implemented for URL: %s", url));
        }
    }
}
