package fr.iambluedev.orion.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import fr.iambluedev.orion.Orion;
import fr.iambluedev.orion.event.DataCheckEvent;
import fr.iambluedev.orion.util.Callback;
import fr.skybeastmc.events.EventHandler;
import fr.skybeastmc.events.Listener;

public class DataListener implements Listener {

	private final static Logger logger = Logger.getLogger(DataListener.class);

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onData(DataCheckEvent event) {
		for (String action : event.getExtractedDatas().keySet()) {
			List<Map<String, Object>> datas = (List<Map<String, Object>>) event.getExtractedDatas().get(action);
			Orion.getInstance().getDb().get(new Callback<Connection, RethinkDB>() {
				@Override
				public void call(Connection c, RethinkDB r) {
					List<Map<String, Object>> newDatas = new ArrayList<Map<String, Object>>();
					String table = event.getWebsite().getName().toLowerCase() + "_" + action;

					for (Map<String, Object> data : datas) {
						Cursor<?> cursor = r.table(table).filter(data).run(c);
						if (!cursor.hasNext()) {
							data.put("timestamp", (int) (System.currentTimeMillis() / 1000));
							newDatas.add(data);
						}
					}

					if (newDatas.size() > 0) {
						logger.debug("|  (" + action + ") Inserting " + newDatas.size() + " new elements");
						r.table(table).insert(newDatas).run(c);
					} 
				}
			});
		}
	}

}
