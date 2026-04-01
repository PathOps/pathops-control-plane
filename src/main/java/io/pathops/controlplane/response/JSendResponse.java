package io.pathops.controlplane.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
public class JSendResponse {

	private JSendStatus status;

	@JsonInclude(Include.NON_NULL)
	private Object data;
	
	@JsonInclude(Include.NON_NULL)
	private String code;
	
	@JsonInclude(Include.NON_NULL)
	private String message;
	
}
