package fi.gtrxac.bluewap;

import java.util.*;

public class URL {
    public String protocol;
    public String domain;
    public Vector path;
    public String card;

    public URL(String url, URL baseUrl) throws Exception {
        // has protocol -> treat as absolute URL
        if (url.indexOf("://") != -1) {
            parseAbsoluteUrl(url);
        } else {
            parseRelativeUrl(url, baseUrl);
        }
    }

    public URL(String url) throws Exception {
        parseAbsoluteUrl(url);
    }

    private void parseRelativeUrl(String url, URL baseUrl) throws Exception {
        url = url.trim();

        // protocol will always be the same
        protocol = baseUrl.protocol;

        if (url.startsWith("//")) {
            // absolute url only sharing the protocol
            parseAbsoluteUrl(protocol + ":" + url);
            return;
        }

        domain = baseUrl.domain;

        // separate card reference
        int pathEndIndex = url.lastIndexOf('#');

        if (pathEndIndex != -1) {
            card = url.substring(pathEndIndex + 1);
            url = url.substring(0, pathEndIndex);
        }

        // if there was only a card reference, fill in the rest from the current page
        if (url.equals("")) {
            path = baseUrl.path;
            return;
        }

        path = Util.splitVec(url, "/");

        // starts with '/' -> absolute path on the same domain
        if (url.startsWith("/")) {
            path.removeElementAt(0);
            return;
        }

        // else it's relative to the current page, prepend the current page's path except for the last segment
        for (int i = 0; i < baseUrl.path.size() - 1; i++) {
            path.insertElementAt(baseUrl.path.elementAt(i), i);
        }

        // remove "." part to point to the same dir
        // remove ".." and preceding part to go back a dir
        for (int i = 0; i < path.size(); ) {
            String part = (String) path.elementAt(i);

            if (part.equals(".")) {
                path.removeElementAt(i);
            }
            else if (part.equals("..") && i != 0) {
                path.removeElementAt(i);
                path.removeElementAt(i - 1);
            }
            else i++;
        }
    }

    private void parseAbsoluteUrl(String url) throws Exception {
        url = url.trim();

        if (url.startsWith("#")) {
            throw new Exception("full URL required");
        }

        int protocolEndIndex = url.indexOf("://");

        if (protocolEndIndex != -1) {
            protocol = url.substring(0, protocolEndIndex);
            url = url.substring(protocolEndIndex + 3);
        } else {
            protocol = "http";
        }

        int pathEndIndex = url.lastIndexOf('#');

        if (pathEndIndex != -1) {
            card = url.substring(pathEndIndex + 1);
            url = url.substring(0, pathEndIndex);
        }

        path = Util.splitVec(url, "/");

        if (protocol.equals("file") || protocol.equals("jar")) {
            domain = "";
            if (url.startsWith("/")) {
                path.removeElementAt(0);
            }
        } else {
            if (path.size() == 0) {
                if (protocol.equals("warnings")) {
                    domain = "";
                    return;
                } else {
                    throw new Exception("URL does not have a domain");
                }
            }
            domain = (String) path.elementAt(0);
            path.removeElementAt(0);
        }
    }

    public String toString(boolean withCard) {
        StringBuffer result = new StringBuffer();

        result.append(protocol)
            .append("://")
            .append(domain)
            .append("/")
            .append(getPath());

        if (withCard && card != null) result.append("#").append(card);

        return result.toString();
    }

    public String toDebugString() {
        StringBuffer result = new StringBuffer();

        result.append("[").append(protocol).append("]")
            .append(" [").append(domain).append("]");

        for (int i = 0; i < path.size(); i++) {
            String part = (String) path.elementAt(i);

            result.append(" [").append(part).append("]");
        }

        result.append(" [").append(card).append("]");

        return result.toString();
    }

    public String getPath() {
        StringBuffer result = new StringBuffer();

        int i = 0;
        while (i < path.size()) {
            String part = (String) path.elementAt(i);
            result.append(part);
            i++;
            if (i >= path.size()) break;
            result.append("/");
        }

        return result.toString();
    }

    public boolean isSamePage(URL other) {
        return other.domain.equals(domain) &&
            other.protocol.equals(protocol) &&
            other.getPath().equals(getPath());
    }
}