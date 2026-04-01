package io.pathops.controlplane.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JSendStatus {
	
	@JsonProperty("success")
	SUCCESS("success"),
	@JsonProperty("fail")
	FAIL("fail"),
	@JsonProperty("error")
	ERROR("error");
	
	private final String status;
	
	private JSendStatus(final String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

}
