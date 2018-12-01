package fr.iambluedev.orion.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.iambluedev.orion.event.DataCheckEvent;
import fr.iambluedev.orion.event.LinkCheckEvent;
import fr.iambluedev.orion.object.Action;
import fr.iambluedev.orion.object.Data;
import fr.iambluedev.orion.object.Website;
import fr.skybeastmc.events.EventManager;

public class ExtractUtil {

	private final static Logger logger = Logger.getLogger(ExtractUtil.class);

	public static void extract(Website web, Action[] actions, Document doc) {
		Map<String, Object> extractedDatas = new HashMap<String, Object>();
		for (Action action : actions) {
			logger.info("|  Executing action " + action.getName());
			List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
			Elements elements = doc.select(action.getSelector());
			if (elements.size() > 0) {
				logger.info("|  Extracting datas from " + elements.size() + " elements");

				for (Element el : elements) {
					Map<String, Object> datasTmp = new HashMap<String, Object>();

					for (Data data : action.getDatas()) {
						Elements els = el.select(data.getSelector());
						if (els.size() > 1 || els.size() == 0) {
							logger.error("|   The selector '" + data.getSelector() + "' in '" + action.getSelector()
									+ "' return " + els.size() + " elements, please modify this selector");
						} else {
							String format = data.getFormat();
							String type = "";
							String[] formatter = new String[] {};

							Element value = els.get(0);

							if (format.contains(":")) {
								String[] tmp = format.split(":");

								type = tmp[0];
								formatter = Arrays.copyOfRange(tmp, 1, tmp.length);

							} else {
								type = format;
							}

							if (type.equalsIgnoreCase("string")) {
								String tvalue = value.text();
								if (formatter.length > 0) {
									for (String iformatter : formatter) {
										if(iformatter.startsWith("attr[") && iformatter.endsWith("]")) {
											String attr = iformatter.replace("attr[", "").replace("]", "");
											tvalue = value.attr(attr).toLowerCase();
										}
										
										if (iformatter.equalsIgnoreCase("trim")) {
											tvalue = tvalue.trim();
										}
									}
								}
								datasTmp.put(data.getName(), tvalue);
							} else if (type.equalsIgnoreCase("integer")) {
								String tvalue = value.text();
								datasTmp.put(data.getName(), Integer.valueOf(tvalue));
							}
						}
					}
					datas.add(datasTmp);
				}

				extractedDatas.put(action.getName(), datas);
			} else {
				logger.error("|  No element to extract with the selector '" + action.getSelector()
						+ "', perhaps it's invalid");
			}

		}

		EventManager.callEvent(new DataCheckEvent(extractedDatas, web));

		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String tmp = link.absUrl("href");
			if (tmp.endsWith("#")) {
				tmp = tmp.substring(0, tmp.length() - 1);
			}
			if (tmp.contains(web.getUrl().replace("http://", "").replace("https://", ""))) {
				EventManager.callEvent(new LinkCheckEvent(tmp));
			}
		}
	}

	public static String formatUrl(String route, Website web) {
		return route.replace("%{MAIN_URL}", web.getUrl());
	}

}
