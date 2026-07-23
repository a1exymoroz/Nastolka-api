package com.nastolka.integration.bgg;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BggXmlParser {

    private static final String PLACEHOLDER_PHOTO = "https://via.placeholder.com/300x300?text=No+Image";

    public List<BggSearchItem> parseSearchResults(String xml) {
        List<BggSearchItem> results = new ArrayList<>();
        Document document = parse(xml);
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String type = item.getAttribute("type");
            if (!"boardgame".equals(type) && !"boardgameexpansion".equals(type)) {
                continue;
            }

            Long bggId = Long.parseLong(item.getAttribute("id"));
            String name = getChildAttribute(item, "name", "value").orElse("Unknown");
            Integer year = getChildAttribute(item, "yearpublished", "value")
                    .flatMap(this::parseInteger)
                    .orElse(null);

            results.add(new BggSearchItem(bggId, name, year, "boardgameexpansion".equals(type)));
        }

        return results;
    }

    public BggGameDetails parseGameDetails(String xml, Long bggId) {
        Document document = parse(xml);
        Element item = (Element) document.getElementsByTagName("item").item(0);
        if (item == null) {
            throw new IllegalArgumentException("Game not found on BoardGameGeek");
        }

        String name = getPrimaryName(item).orElse("Unknown");
        String description = getTextContent(item, "description").orElse("");
        String photo = getTextContent(item, "image")
                .or(() -> getTextContent(item, "thumbnail"))
                .orElse(PLACEHOLDER_PHOTO);

        Integer minPlayers = getPositiveInt(item, "minplayers");
        Integer maxPlayers = getPositiveInt(item, "maxplayers");
        Integer minPlayTime = getPositiveInt(item, "minplaytime");
        Integer maxPlayTime = getPositiveInt(item, "maxplaytime");

        return new BggGameDetails(bggId, name, description, photo, minPlayers, maxPlayers, minPlayTime, maxPlayTime);
    }

    private Integer getPositiveInt(Element item, String tag) {
        return getChildAttribute(item, tag, "value")
                .flatMap(this::parseInteger)
                .filter(value -> value > 0)
                .orElse(null);
    }

    private Document parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            return factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse BGG XML response", e);
        }
    }

    private Optional<String> getPrimaryName(Element item) {
        NodeList names = item.getElementsByTagName("name");
        for (int i = 0; i < names.getLength(); i++) {
            Element nameElement = (Element) names.item(i);
            if ("primary".equals(nameElement.getAttribute("type"))) {
                return Optional.ofNullable(nameElement.getAttribute("value")).filter(v -> !v.isBlank());
            }
        }
        if (names.getLength() > 0) {
            Element first = (Element) names.item(0);
            return Optional.ofNullable(first.getAttribute("value")).filter(v -> !v.isBlank());
        }
        return Optional.empty();
    }

    private Optional<String> getChildAttribute(Element parent, String tag, String attribute) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return Optional.empty();
        }
        Element element = (Element) nodes.item(0);
        return Optional.ofNullable(element.getAttribute(attribute)).filter(v -> !v.isBlank());
    }

    private Optional<String> getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return Optional.empty();
        }
        Node node = nodes.item(0);
        return Optional.ofNullable(node.getTextContent()).map(String::trim).filter(v -> !v.isBlank());
    }

    private Optional<Integer> parseInteger(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
