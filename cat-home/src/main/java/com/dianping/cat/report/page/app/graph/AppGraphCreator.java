package com.dianping.cat.report.page.app.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataGroupByField;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;

public class AppGraphCreator extends AbstractGraphCreator {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppConfigManager m_manager;

	public LineChart buildLineChart(QueryEntity queryEntity1, QueryEntity queryEntity2, String type) {
		List<Double[]> datas = new LinkedList<Double[]>();

		if (queryEntity1 != null) {
			Double[] data1 = prepareQueryData(queryEntity1, type);
			datas.add(data1);
		}

		if (queryEntity2 != null) {
			Double[] values2 = prepareQueryData(queryEntity2, type);
			datas.add(values2);
		}
		return buildChartData(datas, type);
	}

	private Double[] prepareQueryData(QueryEntity queryEntity, String type) {
		Double[] value = m_appDataService.queryValue(queryEntity, type);

		return value;
	}

	private String queryType(String type) {
		if (AppDataService.SUCCESS.equals(type)) {
			return "成功率（%/5分钟）";
		} else if (AppDataService.REQUEST.equals(type)) {
			return "请求数（个/5分钟）";
		} else if (AppDataService.DELAY.equals(type)) {
			return "延时平均值（毫秒/5分钟）";
		} else {
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	public LineChart buildChartData(final List<Double[]> datas, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(queryType(type));

		if (AppDataService.SUCCESS.equals(type)) {
			lineChart.setMinYlable(95D);
			lineChart.setMaxYlabel(100D);
		}

		for (int i = 0; i < datas.size(); i++) {
			Double[] data = datas.get(i);

			lineChart.add("查询" + (i + 1), data);
		}
		return lineChart;
	}

	@Override
	protected Map<Long, Double> convertToMap(double[] data, Date start, int step) {
		Map<Long, Double> map = new LinkedHashMap<Long, Double>();
		int length = data.length;
		long startTime = start.getTime();
		long time = startTime;

		for (int i = 0; i < length; i++) {
			time += step * TimeUtil.ONE_MINUTE;
			map.put(time, data[i]);
		}
		return map;
	}

	private Map<String, Double> buildPercentMap(List<Item> items) {
		Map<String, Double> percents = new HashMap<String, Double>();
		double sum = 0;

		for (Item item : items) {
			sum += item.getNumber();
		}

		if (sum > 0) {
			for (Item item : items) {
				percents.put(item.getTitle(), item.getNumber() / sum);
			}
		}
		return percents;
	}

	public Pair<PieChart, Map<String, Double>> buildPieChart(QueryEntity entity, AppDataGroupByField field) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<AppDataCommand> datas = m_appDataService.queryAppDataCommandsByField(entity, field);

		for (AppDataCommand data : datas) {
			Item item = buildPieChartItem(entity.getCommand(), data, field);

			items.add(item);
		}
		pieChart.setTitle(field.getName() + "访问情况");
		pieChart.addItems(items);

		return new Pair<PieChart, Map<String, Double>>(pieChart, buildPercentMap(items));
	}

	private String buildPieChartFieldTitlePair(int command, AppDataCommand data, AppDataGroupByField field) {
		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> operators = m_manager
			      .queryConfigItem(AppConfigManager.OPERATOR);
			com.dianping.cat.configuration.app.entity.Item operator = null;
			int operatorValue = data.getOperator();

			if (operators != null && (operator = operators.get(operatorValue)) != null) {
				return operator.getName();
			} else {
				throw new RuntimeException("Unrecognized operator configuration in app config, operator id: "
				      + operatorValue);
			}
		case APP_VERSION:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> appVersions = m_manager
			      .queryConfigItem(AppConfigManager.VERSION);
			com.dianping.cat.configuration.app.entity.Item appVersion = null;
			int appVersionValue = data.getAppVersion();

			if (appVersions != null && (appVersion = appVersions.get(appVersionValue)) != null) {
				return appVersion.getName();
			} else {
				throw new RuntimeException("Unrecognized app-version configuration in app config, operator id: "
				      + appVersionValue);
			}
		case CITY:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> cities = m_manager
			      .queryConfigItem(AppConfigManager.CITY);
			com.dianping.cat.configuration.app.entity.Item city = null;
			int cityValue = data.getCity();

			if (cities != null && (city = cities.get(cityValue)) != null) {
				return city.getName();
			} else {
				throw new RuntimeException("Unrecognized city configuration in app config, operator id: " + cityValue);
			}
		case CONNECT_TYPE:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> connectTypes = m_manager
			      .queryConfigItem(AppConfigManager.CONNECT_TYPE);
			com.dianping.cat.configuration.app.entity.Item connectType = null;
			int connectTypeValue = data.getConnnectType();

			if (connectTypes != null && (connectType = connectTypes.get(connectTypeValue)) != null) {
				return connectType.getName();
			} else {
				throw new RuntimeException("Unrecognized connect-type configuration in app config, operator id: "
				      + connectTypeValue);
			}
		case NETWORK:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> networks = m_manager
			      .queryConfigItem(AppConfigManager.NETWORK);
			com.dianping.cat.configuration.app.entity.Item network = null;
			int networkValue = data.getNetwork();

			if (networks != null && (network = networks.get(networkValue)) != null) {
				return network.getName();
			} else {
				throw new RuntimeException("Unrecognized network configuration in app config, operator id: " + networkValue);
			}
		case PLATFORM:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> platforms = m_manager
			      .queryConfigItem(AppConfigManager.PLATFORM);
			com.dianping.cat.configuration.app.entity.Item platform = null;
			int platformValue = data.getPlatform();

			if (platforms != null && (platform = platforms.get(platformValue)) != null) {
				return platform.getName();
			} else {
				throw new RuntimeException("Unrecognized platform configuration in app config, operator id: "
				      + platformValue);
			}
		case CODE:
			Map<Integer, Code> codes = m_manager.queryCodeByCommand(command);
			Code code = null;
			int codeValue = data.getCode();

			if (codes != null && (code = codes.get(codeValue)) != null) {
				return code.getName();
			} else {
				throw new RuntimeException("Unrecognized code configuration in app config, operator id: " + codeValue);
			}
		default:
			throw new RuntimeException("Unrecognized groupby field: " + field);
		}
	}

	private Item buildPieChartItem(int command, AppDataCommand data, AppDataGroupByField field) {
		Item item = new Item();
		item.setNumber(data.getAccessNumberSum());
		String title = buildPieChartFieldTitlePair(command, data, field);

		item.setTitle(title);
		return item;
	}
}
