package io.pathops.controlplane.response;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 *
 * http://labs.omniti.com/labs/jsend
 * https://github.com/omniti-labs/jsend
 *
 */

@Component
public class JSendResponseFactory {

	public @NonNull JSendResponse createSuccessMessage(Object data) {
		JSendResponse jSendResponse = new JSendResponse();
		jSendResponse.setStatus(JSendStatus.SUCCESS);
		jSendResponse.setData(data);
		return jSendResponse;
	}

	public JSendResponse createFailMessage(Object data) {
		JSendResponse jSendResponse = new JSendResponse();
		jSendResponse.setStatus(JSendStatus.FAIL);
		jSendResponse.setData(data);
		return jSendResponse;
	}

	public JSendResponse createErrorMessage(String message) {
		JSendResponse jSendResponse = new JSendResponse();
		jSendResponse.setStatus(JSendStatus.ERROR);
		jSendResponse.setMessage(message);
		return jSendResponse;
	}

	public JSendResponse createErrorMessage(String message, String code) {

		JSendResponse jSendResponse = createErrorMessage(message);
		jSendResponse.setCode(code);

		return jSendResponse;
	}

	public JSendResponse createErrorMessage(String message, Object data) {

		JSendResponse jSendResponse = createErrorMessage(message);
		jSendResponse.setData(data);

		return jSendResponse;
	}

	public JSendResponse createErrorMessage(String message, String code, Object data) {

		JSendResponse jSendResponse = createErrorMessage(message);
		jSendResponse.setCode(code);
		jSendResponse.setData(data);

		return jSendResponse;
	}
}
