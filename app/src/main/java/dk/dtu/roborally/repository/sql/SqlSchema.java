package dk.dtu.roborally.repository.sql;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Creates the Postgres tables used to resume a game after a restart.
 */
public final class SqlSchema {

	private static final String[] STATEMENTS = {"""
			CREATE TABLE IF NOT EXISTS users (
			    id VARCHAR(64) PRIMARY KEY,
			    username VARCHAR(32) NOT NULL UNIQUE,
			    password_hash VARCHAR(60)
			)
			""",
			// databases created before passwords were added
			"""
					ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(60)
					""",
			"""
					CREATE TABLE IF NOT EXISTS lobbies (
					    id VARCHAR(64) PRIMARY KEY,
					    name VARCHAR(32) NOT NULL,
					    owner_id VARCHAR(64) NOT NULL REFERENCES users (id),
					    level_path VARCHAR(255) NOT NULL,
					    game_id VARCHAR(64)
					)
					""",
			"""
					CREATE TABLE IF NOT EXISTS lobby_members (
					    lobby_id VARCHAR(64) NOT NULL REFERENCES lobbies (id) ON DELETE CASCADE,
					    user_id VARCHAR(64) NOT NULL REFERENCES users (id),
					    color VARCHAR(32) NOT NULL,
					    ready BOOLEAN NOT NULL,
					    PRIMARY KEY (lobby_id, user_id)
					)
					""",
			"""
					CREATE TABLE IF NOT EXISTS games (
					    id VARCHAR(64) PRIMARY KEY,
					    status VARCHAR(32) NOT NULL,
					    phase VARCHAR(32) NOT NULL,
					    state_json JSONB NOT NULL
					)
					"""};

	private SqlSchema() {
	}

	public static synchronized void ensure(DataSource dataSource) {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		for (String statement : STATEMENTS) {
			jdbc.execute(statement);
		}
	}
}
