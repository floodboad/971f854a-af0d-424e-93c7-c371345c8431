function drawPartitionedResourceUsageChart(resourceUsage, chartDom, chartTitle, dataUnit) {
	var resourceData = new Array();
	
	var usedData = new Object();
	usedData.name = "已用";
	usedData.value = resourceUsage.used;
	resourceData.push(usedData);

	var unusedData = new Object();
	unusedData.name = "未用";
	unusedData.value = resourceUsage.unused;
	resourceData.push(unusedData);
	
	var unallocatedData = new Object();
	unallocatedData.name = "未分配";
	unallocatedData.value = resourceUsage.unallocated;
	resourceData.push(unallocatedData);
	
	createOverviewPie(chartDom, chartTitle, resourceData, dataUnit);
}

function createOverviewPie(chartDom, chartTitle, chartData, dataUnit) {
	var myChart = echarts.init(document.getElementById(chartDom));

	var option = {
		// title : {
		// text : chartTitle + '总计' + (chartData[0].value + chartData[1].value + chartData[2].value) + dataUnit + '：'
		// + chartData[0].name + chartData[0].value + dataUnit + "，" + chartData[1].name + chartData[1].value + dataUnit
		// + "，"
		// + chartData[2].name + chartData[2].value + dataUnit,
		// x : 'center',
		// textStyle : {
		// fontWeight : 'normal',
		// fontSize : '12',
		// fontFamily : 'Microsoft Yahei'
		// }
		// },

		tooltip : {
			trigger : 'item',
			formatter : "{a} <br/>{b} : {c} ({d}%)"
		},

		calculable : false,

		color : [ '#ff7f50', '#32cd32', '#999999', ],

		series : [ {
			type : 'pie',
			radius : [ '40%', '70%' ],			
			clockWise : true,
			itemStyle : {
				normal : {
					label : {
						position : 'inner'
					},
					labelLine : {
						show : false
					}
				}
			},
			/*
			 * itemStyle : { normal : { label : { show : false }, labelLine : { show : false } }, emphasis : { label : {
			 * show : true, position : 'center', textStyle : { fontSize : '30', fontWeight : 'bold' } } } },
			 */
			data : [ {
				value : chartData[0].value,
				name : chartData[0].name
			}, {
				value : chartData[1].value,
				name : chartData[1].name
			}, {
				value : chartData[2].value,
				name : chartData[2].name
			}, ]
		} ]
	};

	// console.log("echarts: " + option.series.length);
	myChart.setOption(option);
}

function createDomainOverallResourceUsageBar(yAxisData, resourceUsage, chartDom) {
	var myChart = echarts.init(document.getElementById(chartDom));

	var lengendData = new Array();
	for (var i = 0; i < resourceUsage.length; i++) {
		lengendData.push(resourceUsage[i].legend);
	}

	var option = {
		tooltip : {
			trigger : 'axis',
			axisPointer : { // 坐标轴指示器，坐标轴触发有效
				type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
			}
		},
		
		legend : {
			data : lengendData
		},
		
		// toolbox : {
		// show : true,
		// feature : {
		// mark : {
		// show : true
		// },
		// dataView : {
		// show : true,
		// readOnly : false
		// },
		// magicType : {
		// show : true,
		// type : [ 'line', 'bar', 'stack', 'tiled' ]
		// },
		// restore : {
		// show : true
		// },
		// saveAsImage : {
		// show : true
		// }
		// }
		// },
		// calculable : true,

		color : [ '#ff7f50', '#32cd32', '#999999', ],
		
		xAxis : [ {
			type : 'value'
		} ],
		
		yAxis : [ {
			type : 'category',
			data : yAxisData,
		} ],
		
		series : []
	};

	for (var i = 0; i < resourceUsage.length; i++) {
		var neweries = new Object();
		neweries.name = resourceUsage[i].legend;
		neweries.type = 'bar';
		neweries.barWidth = 15;
		neweries.stack = '总量';

		// "{ normal: {label : {show: true, position: 'insideRight'}}}"
		var itemStyle = new Object();
		var label = new Object();
		label.show = true;
		label.position = 'insideRight';
		itemStyle.normal = label;
		neweries.itemStyle = itemStyle;

		neweries.data = resourceUsage[i].data;
		option.series.push(neweries);
	}

	// console.log(option);

	myChart.setOption(option);
}

function createLine(echartsPath, chartDom, chartTitle, xAxisData, yAxisData, yDataUnit) {
	require.config({
		paths : {
			echarts : echartsPath
		}
	});

	require([ "echarts", "echarts/chart/line" ], function(ec) {
		var myChart = ec.init(document.getElementById(chartDom));

		var option = {
			title : {
				text : chartTitle,
				textStyle : {
					fontSize : 12,
				}
			},
			tooltip : {
				trigger : "axis"
			},
			calculable : true,
			xAxis : [ {
				type : "category",
				boundaryGap : false,
				data : xAxisData
			} ],
			yAxis : [ {
				type : "value",
				axisLabel : {
					formatter : "{value} " + yDataUnit
				}
			} ],
			series : [ {
				name : chartTitle,
				type : "line",
				data : yAxisData
			} ]
		}

		myChart.setOption(option);
	});
}

// 创建饼图 ==标准饼图==
function createPie2(t_title, t_data, t_dom, unit) {
	// console.log("draw chart in pie2");

	var m_dom = "";
	var myChart;
	if (!t_dom.jquery) {
		if (t_dom.indexOf("#") >= 0) {
			m_dom = t_dom.substring(1, t_dom.length);
		} else {
			m_dom = t_dom;
		}
		myChart = echarts.init(document.getElementById(m_dom));
	} else {
		myChart = echarts.init(t_dom[0]);
	}

	// var arrLegend = [];
	// $.each(t_data, function(index, content) {
	// arrLegend.push(content["name"]);
	// })

	var option = {
		title : {
			text : t_title + '配额' + (t_data[0].value + t_data[1].value) + unit + '：' + t_data[0].name + t_data[0].value
					+ unit + "，" + t_data[1].name + t_data[1].value + unit,
			x : 'center',
			textStyle : {
				fontWeight : 'normal',
				fontSize : '12',
				fontFamily : 'Microsoft Yahei'
			}
		},
		tooltip : {
			trigger : 'item',
			formatter : "{a} <br/>{b} : {c} ({d}%)"
		},

		color : [ '#ff7f50', '#32cd32', ],
		calculable : false,
		series : [ {
			type : 'pie',
			radius : '70%',
			center : [ '50%', '50%' ],
			itemStyle : {
				normal : {
					label : {
						position : 'inner'
					},
					labelLine : {
						show : false
					}
				}
			},
			data : t_data
		} ]
	};
	myChart.setOption(option);
}