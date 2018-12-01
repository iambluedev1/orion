package fr.iambluedev.orion.db;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;

import fr.iambluedev.orion.config.Config;
import fr.iambluedev.orion.util.Callback;
import lombok.Getter;

@Getter
public class Rethink {

	private static RethinkDB r = RethinkDB.r;
	private Connection conn;

	public Rethink(Config config) {
		this.conn = r.connection().hostname(config.getDbHost()).port(config.getDbPort()).db(config.getDbName())
				.connect();
	}

	public void get(Callback<Connection, RethinkDB> callback) {
		if (this.conn.isOpen()) {
			callback.call(this.conn, r);
		} else {
			this.conn.reconnect();
			callback.call(this.conn, r);
		}
	}
}
