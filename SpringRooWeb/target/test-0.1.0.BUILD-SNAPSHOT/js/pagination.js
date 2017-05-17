function fillPagination(pageNo, pageTotal) {
	// important! cast to int first
	pageNo = parseInt(pageNo);
	pageTotal = parseInt(pageTotal);

	// 不足5页，全部列出
	if (pageTotal <= 5) {
		// console.log("pageTotal <= 5");
		for (var i = 1; i <= pageTotal; i++) {
			var pageLink = $("<a>", {
				text : i,
				href : "javascript:void(0)",
				class : "toPage",
			});

			if (i == pageNo) {
				pageLink.css("background-color", "rgb(191, 178, 243)");
			}

			$("<li>").append(pageLink).addClass("pageBox").insertBefore("#next");
		}
		// 超出5页，列出相邻2页页码，其余用...代替
	} else {
		// console.log("pageTotal > 5");
		var pageLink, ellipsisLink;

		// first page link
		pageLink = $("<a>", {
			text : 1,
			href : "javascript:void(0)",
			class : "toPage",
		})
		if (1 == pageNo) {
			pageLink.css("background-color", "rgb(191, 178, 243)");
		}
		$("<li>").append(pageLink).addClass("pageBox").insertBefore("#next");

		// ellipse link
		if (pageNo >= 5) {
			ellipsisLink = $("<a>", {
				text : "...",
				href : "javascript:void(0)",
			});
			$("<li>").append(ellipsisLink).addClass("pageBox").insertBefore("#next");
		}

		// no more than 5 consecutive page links
		var start = pageNo - 2, end = pageNo + 2;
		// console.log(start + " to " + end);
		if (start < 2) {
			start = 2;
		}
		if (end >= pageTotal) {
			end = pageTotal - 1;
		}
		// console.log(start + " to " + end);
		for (var i = start; i <= end; i++) {
			pageLink = $("<a>", {
				text : i,
				href : "javascript:void(0)",
				class : "toPage",
			})
			if (i == pageNo) {
				pageLink.css("background-color", "rgb(191, 178, 243)");
			}
			$("<li>").append(pageLink).addClass("pageBox").insertBefore("#next");
		}

		// ellipse link
		if (pageNo + 3 < pageTotal) {
			ellipsisLink = $("<a>", {
				text : "...",
				href : "javascript:void(0)",
			});
			$("<li>").append(ellipsisLink).addClass("pageBox").insertBefore("#next");
		}

		// last page link
		pageLink = $("<a>", {
			text : pageTotal,
			href : "javascript:void(0)",
			class : "toPage",
		})
		if (pageTotal == pageNo) {
			pageLink.css("background-color", "rgb(191, 178, 243)");
		}
		$("<li>").append(pageLink).addClass("pageBox").insertBefore("#next");
	}

	// if (1 == pageNo) {
	// $("#previous > a").prop("disabled", true);
	// }
	//	
	// if (pageTotal == pageNo) {
	// $("#next > a").prop("disabled", true);
	// }

	return;
}

function showPagination(paginationUlId, pageNo, pageTotal, pageLinkHandler) {
	$("#" + paginationUlId + " .page-number-box").remove();

	// !!! remove handlers before bind again
	// console.log($("#" + paginationUlId + " li:first").text());
	// console.log($("#" + paginationUlId + " li:last").text());
	$("#" + paginationUlId + " li:first").off();
	$("#" + paginationUlId + " li:last").off();

	// important! cast to int first
	pageNo = parseInt(pageNo);
	pageTotal = parseInt(pageTotal);

	// 不足5页，全部列出
	if (pageTotal <= 5) {
		// console.log("pageTotal <= 5");
		for (var i = 1; i <= pageTotal; i++) {
			var pageLink = $("<a>", {
				text : i,
				href : "javascript:void(0)",
				class : "page-number",
			});

			if (i == pageNo) {
				pageLink.css("background-color", "rgb(191, 178, 243)");
			}

			$("<li>").append(pageLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");
		}
		// 超出5页，列出相邻2页页码，其余用...代替
	} else {
		// console.log("pageTotal > 5");
		var pageLink, ellipsisLink;

		// first page link
		pageLink = $("<a>", {
			text : 1,
			href : "javascript:void(0)",
			class : "page-number",
		})
		if (1 == pageNo) {
			pageLink.css("background-color", "rgb(191, 178, 243)");
		}
		$("<li>").append(pageLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");

		// ellipse link
		if (pageNo >= 5) {
			ellipsisLink = $("<a>", {
				text : "...",
				href : "javascript:void(0)",
			});
			$("<li>").append(ellipsisLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");
		}

		// no more than 5 consecutive page links
		var start = pageNo - 2, end = pageNo + 2;
		// console.log(start + " to " + end);
		if (start < 2) {
			start = 2;
		}
		if (end >= pageTotal) {
			end = pageTotal - 1;
		}
		// console.log(start + " to " + end);
		for (var i = start; i <= end; i++) {
			pageLink = $("<a>", {
				text : i,
				href : "javascript:void(0)",
				class : "page-number",
			})
			if (i == pageNo) {
				pageLink.css("background-color", "rgb(191, 178, 243)");
			}
			$("<li>").append(pageLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");
		}

		// ellipse link
		if (pageNo + 3 < pageTotal) {
			ellipsisLink = $("<a>", {
				text : "...",
				href : "javascript:void(0)",
			});
			$("<li>").append(ellipsisLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");
		}

		// last page link
		pageLink = $("<a>", {
			text : pageTotal,
			href : "javascript:void(0)",
			class : "page-number",
		})
		if (pageTotal == pageNo) {
			pageLink.css("background-color", "rgb(191, 178, 243)");
		}
		$("<li>").append(pageLink).addClass("page-number-box").insertBefore("#" + paginationUlId + " li:last");
	}

	// if (1 == pageNo) {
	// $("#previous > a").prop("disabled", true);
	// }
	//	
	// if (pageTotal == pageNo) {
	// $("#next > a").prop("disabled", true);
	// }

	$("#" + paginationUlId + " .page-number").each(function() {
		$(this).on("click", function() {
			pageLinkHandler($(this).text());
		});
	});

	$("#" + paginationUlId + " li:first").on("click", function() {
		if (pageNo == 1) {
			return false;
		} else {
			pageLinkHandler(pageNo - 1);
		}
	});

	$("#" + paginationUlId + " li:last").click(function() {
		if (pageNo == pageTotal) {
			return false;
		} else {
			pageLinkHandler(pageNo + 1);
		}
	});

	return;
}