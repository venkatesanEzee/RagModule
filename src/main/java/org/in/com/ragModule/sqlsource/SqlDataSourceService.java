package org.in.com.ragModule.sqlsource;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SqlDataSourceService {

	private final JdbcTemplate jdbcTemplate;

	public SqlDataSourceService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.jdbcTemplate.setFetchSize(500);
	}

	/**
	 * Streams rows from the table one at a time and invokes the callback for each,
	 * converted to a natural-language sentence. Constant memory regardless of table
	 * size.
	 */
	public void streamTableAsSentences(String tableName, Consumer<String> onSentence) {
		jdbcTemplate.query("SELECT * FROM " + tableName, rs -> {
			onSentence.accept(rowToSentence(tableName, rs));
		});
	}

	private String rowToSentence(String tableName, ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		StringBuilder sentence = new StringBuilder("Record from ").append(tableName).append(": ");

		for (int i = 1; i <= meta.getColumnCount(); i++) {
			if (i > 1)
				sentence.append(", ");
			sentence.append(meta.getColumnName(i)).append(" is ").append(rs.getObject(i));
		}
		sentence.append(".");
		return sentence.toString();
	}
}
