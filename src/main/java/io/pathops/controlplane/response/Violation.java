package io.pathops.controlplane.response;

import lombok.Data;

@Data
public class Violation {

	private String fieldName;
	private String message;
}