package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

/**
 * @author jiashunx
 */
public class UrlPatternPathModel extends UrlPathModel {

    private final boolean placeholder;
    private final String placeholderName;
    private String originPath;
    private String originPathVal;

    public UrlPatternPathModel(String p) {
        super(p);
        placeholder = UrlParaser.isPlaceholderString(pathVal);
        placeholderName = UrlParaser.getPlaceholderName(pathVal);
        if (placeholder) {
            originPath = path;
            originPathVal = pathVal;
            path = "/*";
            pathVal = "*";
        }
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public String getOriginPath() {
        return originPath;
    }

    public String getOriginPathVal() {
        return originPathVal;
    }
}
