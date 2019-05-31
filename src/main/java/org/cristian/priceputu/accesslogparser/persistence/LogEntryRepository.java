package org.cristian.priceputu.accesslogparser.persistence;

import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.cristian.priceputu.accesslogparser.parsers.LogEntry;
import org.cristian.priceputu.accesslogparser.utils.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LogEntryRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryRepository.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PostConstruct
	public void init() {
		LOGGER.info("Started repository ...");
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public void save(Collection<LogEntry> entries) {
		jdbcTemplate.batchUpdate("INSERT INTO entries(ts,ip,method,response,user_agent) values (?,?,?,?,?)",
				entries.stream().map(e -> convert(e)).collect(toList()));
		LOGGER.info("Persisted {} entries.", entries.size());
	}

	protected Object[] convert(LogEntry e) {
		return new Object[] { new Timestamp(e.getTs().getTime()), e.getIp(), e.getMethod(), e.getResponse(), e.getUserAgent() };
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public List<?> checkHourly(Date startWhen, int threshold) {
		DateTime dt = new DateTime(startWhen.getTime());
		dt = dt.plusHours(1);

		return checkAndInsert(startWhen.getTime(), dt.getMillis(), threshold);
	}

	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public List<?> checkDaily(Date startWhen, int threshold) {
		DateTime dt = new DateTime(startWhen.getTime());
		dt = dt.plusDays(1);

		return checkAndInsert(startWhen.getTime(), dt.getMillis(), threshold);
	}

	/**
	 * create table reports ( query_run_at timestamp not null, query_id varchar(32),
	 * ip varchar(20) not null, req_count int not null, hourly boolean, comments
	 * varchar(250) );
	 * 
	 * @param startsWhen
	 * @param endsWhen
	 * @param threshold
	 * @return
	 */
	protected List<?> checkAndInsert(long startsWhen, long endsWhen, int threshold) {

		boolean hourly = (endsWhen - startsWhen) == 3600000;
		final String queryScan = "INSERT INTO reports(query_run_at, query_id, ip, req_count, hourly, comments) "
				+ " SELECT now(), ?, ip, count(ip), ?, ? "
				+ " FROM "
				+ " entries WHERE ts BETWEEN ? AND ? "
				+ " GROUP BY ip "
				+ " HAVING count(ip) >= ?";

		final String qId = UUID.randomUUID().toString();
		final String comment = String.format("User ip went over threshold: %s on a duration: %s", threshold, hourly ? "HOURLY" : "DAILY");

		jdbcTemplate.update(queryScan, qId, hourly, comment, new Timestamp(startsWhen), new Timestamp(endsWhen), threshold);
		return jdbcTemplate.query("SELECT ip, comments FROM reports WHERE query_id = ?", new Object[] { qId }, (rs, idx) -> {
			return new Pair<String, String>(rs.getString("ip"), rs.getString("comments"));
		});
	}
}
