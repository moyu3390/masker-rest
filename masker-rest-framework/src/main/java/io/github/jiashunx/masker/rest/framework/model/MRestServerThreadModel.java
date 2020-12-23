package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestServerThreadModel {

    private MRestContext restContext;
    private MRestRequest restRequest;
    private MRestResponse restResponse;

    public MRestServerThreadModel assertNotNull() {
        Objects.requireNonNull(restContext);
        Objects.requireNonNull(restRequest);
        Objects.requireNonNull(restResponse);
        return this;
    }

    public MRestContext getRestContext() {
        return restContext;
    }

    public void setRestContext(MRestContext restContext) {
        this.restContext = restContext;
    }

    public MRestRequest getRestRequest() {
        return restRequest;
    }

    public void setRestRequest(MRestRequest restRequest) {
        this.restRequest = restRequest;
    }

    public MRestResponse getRestResponse() {
        return restResponse;
    }

    public void setRestResponse(MRestResponse restResponse) {
        this.restResponse = restResponse;
    }
}
