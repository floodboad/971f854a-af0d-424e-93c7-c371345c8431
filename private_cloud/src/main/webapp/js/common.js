/*
 * pUrl : the request url
 * pType : the request method: GET/POST/PUT/DELETE
 * pAsync : true for async, false for sync, must be true when show ajaxWaitingDialog
 * pData : the request data
 * pWaitingMessage : message for ajaxWaitingDialog, allow null to not show waiting dialog
 * pSuccessHandler : success handler
 * pErrorHandler : error handler, allow null to show error message in errorDialog
 * loginPage : redirect page when session time out
 * 
 * need ajaxWaitingDialog and errorDialog elements in HTML.
 */
function callAjax(pUrl, pType, pAsync, pData, pWaitingMessage, pSuccessHandler, pErrorHandler, pRedirectAfterSessionTimedout) {
	// console.log(pWaitingMessage);
	// console.log(pErrorHandler);
	// console.log(pData);
	// var pRedirectAfterSessionTimedout = "/login";
	
	var deferredObject;
	if (null != pWaitingMessage) {
		// console.log("show waiting");
		deferredObject = $.ajax({
			url : pUrl,
			type : pType,
			async : pAsync,
			data : pData,
			// contentType : "application/json",
			beforeSend : function() {
				$("#ajaxWaitingMessageDiv").text(pWaitingMessage);
				$("#ajaxWaitingDialog").modal("show");
			},
			complete : function() {
				// called when the request finishes (after success and error callbacks are executed)
				$("#ajaxWaitingDialog").modal("hide");
			},
			success : function(data, textStatus) {
				pSuccessHandler(data, textStatus);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.log(XMLHttpRequest);
				console.log(textStatus);
				console.log(errorThrown);

				if (null == pErrorHandler) {
					$("#errorDialogMessageDiv").text("访问WEB服务发生异常");
					$("#errorDialog").modal("show");
				} else {
					pErrorHandler(XMLHttpRequest, textStatus, errorThrown);
				}
			},
			statusCode : {
				901 : function () {
					location.href = pRedirectAfterSessionTimedout;
				}
			}
		});
	} else {
		// console.log("not show waiting");
		deferredObject = $.ajax({
			url : pUrl,
			type : pType,
			async : pAsync,
			data : pData,
			// contentType : "application/json",
			success : function(data, textStatus) {
				pSuccessHandler(data, textStatus);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.log(XMLHttpRequest);
				console.log(textStatus);
				console.log(errorThrown);

				if (null == pErrorHandler) {
					$("#errorDialogMessageDiv").text("访问WEB服务发生异常");
					$("#errorDialog").modal("show");
				} else {
					pErrorHandler(XMLHttpRequest, textStatus, errorThrown);
				}
			},
			statusCode : {
				901 : function () {
					location.href = pRedirectAfterSessionTimedout;
				}
			}
		});
	}
	
	return deferredObject;
}