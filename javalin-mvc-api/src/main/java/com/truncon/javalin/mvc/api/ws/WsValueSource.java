package com.truncon.javalin.mvc.api.ws;

/**
 * Specifies where a value should be sourced by model binding.
 */
public enum WsValueSource {
    /**
     * The value can be retrieved from any value source.
     */
    Any,
    /**
     * The value must be retrieved from the request header.
     */
    Header,
    /**
     * The value must be retrieved from a cookie.
     */
    Cookie,
    /**
     * The value must be retrieved from the URL path.
     */
    Path,
    /**
     * The value must be retrieved from the query string.
     */
    QueryString,
    /**
     * The value must be retrieved from the WebSocket message.
     */
    Message
}
