package com.wine.to.up.simple.parser.service.logging;

import com.wine.to.up.commonlib.logging.NotableEvent;

public enum SimpleParserNotableEvents implements NotableEvent {
    I_KAFKA_SEND_MESSAGE_SUCCESS("Kafka send message: {}"),
    I_CONTROLLER_RECEIVED_MESSAGE("Message: {}"),
    I_WINES_PAGE_PARSED("Wines parsed from catalog page with URL: {}"),
    I_WINE_DETAILS_PARSED("Wine details parsed from URL: {}"),

    W_SOME_WARN_EVENT("Warn situation. Description: {}"),
    W_WINE_PAGE_PARSING_FAILED("Can't parse wine catalog page with URL: {}"),
    W_WINE_DETAILS_PARSING_FAILED("Can't parse wine details from URL: {}"),
    W_WINE_ATTRIBUTE_ABSENT("Can't get attribute with name: {} of wine with URL: {}");

    private final String template;

    SimpleParserNotableEvents(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return name();
    }

}
