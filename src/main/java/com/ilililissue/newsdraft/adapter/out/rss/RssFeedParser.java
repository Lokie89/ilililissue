package com.ilililissue.newsdraft.adapter.out.rss;

import com.ilililissue.newsdraft.domain.NewsArticle;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * RSS 2.0·RSS 1.0(RDF)·Atom 피드에서 기사 목록을 읽는다.
 * <ul>
 * <li>발행 시각: {@code pubDate}(RFC 1123), {@code dc:date}·{@code published}·{@code updated}(ISO 8601).
 * 항목에 없으면 피드의 {@code lastBuildDate}를 쓴다(한겨레처럼 항목 시각이 없는 피드).</li>
 * <li>요약문은 태그를 걷어내고 앞 {@value #DESCRIPTION_MAX_LENGTH}자만 쓴다. 본문 전체({@code content:encoded})는 읽지 않는다.</li>
 * <li>DOCTYPE을 허용하지 않는다(XXE 방지). 봇 차단 페이지 같은 HTML 응답도 여기서 실패한다.</li>
 * </ul>
 */
final class RssFeedParser {

    static final int DESCRIPTION_MAX_LENGTH = 200;

    private static final Pattern TAG = Pattern.compile("<[^>]+>");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final Pattern TRACKING_PARAM = Pattern.compile("(?<=[?&])utm_[^&#]*(&|$)");
    private static final DateTimeFormatter RFC_1123_COLON_OFFSET =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss XXX", Locale.ENGLISH);

    private RssFeedParser() {
    }

    static List<NewsArticle> parse(byte[] xml, String publisher, String section) {
        Document document = read(xml);
        OffsetDateTime feedDate = parseDate(firstText(document.getDocumentElement(), "lastBuildDate", "updated", "date"));

        List<NewsArticle> articles = new ArrayList<>();
        for (Element item : elements(document, "item", "entry")) {
            String title = plainText(childText(item, "title"));
            String url = link(item);
            OffsetDateTime publishedAt = parseDate(childText(item, "pubDate", "date", "published", "updated"));
            if (publishedAt == null) {
                publishedAt = feedDate;
            }
            if (title.isEmpty() || url == null || publishedAt == null) {
                continue;
            }
            String description = plainText(childText(item, "description", "summary"));
            if (description.length() > DESCRIPTION_MAX_LENGTH) {
                description = description.substring(0, DESCRIPTION_MAX_LENGTH) + "…";
            }
            articles.add(new NewsArticle(publisher, section, title, url, description, publishedAt));
        }
        return articles;
    }

    static OffsetDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.strip();
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.RFC_1123_DATE_TIME, RFC_1123_COLON_OFFSET,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME)) {
            try {
                return OffsetDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // 다음 형식으로 시도한다.
            }
        }
        return null;
    }

    static String plainText(String html) {
        if (html == null) {
            return "";
        }
        // 엔티티로 감싼 태그(&lt;b&gt;)도 있어 풀고 나서 한 번 더 걷어낸다.
        String text = TAG.matcher(html).replaceAll(" ");
        text = TAG.matcher(HtmlUtils.htmlUnescape(text)).replaceAll(" ");
        return SPACES.matcher(text).replaceAll(" ").strip();
    }

    /**
     * 광고 추적용 utm_* 파라미터를 뗀 원문 링크.
     */
    static String cleanUrl(String url) {
        String cleaned = TRACKING_PARAM.matcher(url.strip()).replaceAll("");
        return cleaned.replaceAll("[?&]$", "");
    }

    private static Document read(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(null);
            return builder.parse(new ByteArrayInputStream(xml));
        } catch (Exception e) {
            throw new IllegalStateException("RSS 피드를 읽을 수 없습니다: " + e.getMessage(), e);
        }
    }

    private static String link(Element item) {
        for (Element link : children(item, "link")) {
            String href = link.getAttribute("href");
            String url = href.isBlank() ? link.getTextContent() : href;
            if (url != null && url.strip().matches("^https?://\\S+$")) {
                return cleanUrl(url);
            }
        }
        String guid = childText(item, "guid");
        return guid != null && guid.strip().matches("^https?://\\S+$") ? cleanUrl(guid) : null;
    }

    private static List<Element> elements(Document document, String... localNames) {
        List<Element> result = new ArrayList<>();
        for (String localName : localNames) {
            NodeList nodes = document.getElementsByTagNameNS("*", localName);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add((Element) nodes.item(i));
            }
        }
        return result;
    }

    private static List<Element> children(Element parent, String localName) {
        List<Element> result = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element element && localName.equals(element.getLocalName())) {
                result.add(element);
            }
        }
        return result;
    }

    private static String childText(Element parent, String... localNames) {
        for (String localName : localNames) {
            List<Element> found = children(parent, localName);
            if (!found.isEmpty() && !found.get(0).getTextContent().isBlank()) {
                return found.get(0).getTextContent();
            }
        }
        return null;
    }

    /**
     * 채널(RSS) 또는 피드(Atom) 수준의 값. 항목 안의 같은 이름 요소는 보지 않는다.
     */
    private static String firstText(Element root, String... localNames) {
        String direct = childText(root, localNames);
        if (direct != null) {
            return direct;
        }
        for (Element channel : children(root, "channel")) {
            String value = childText(channel, localNames);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
