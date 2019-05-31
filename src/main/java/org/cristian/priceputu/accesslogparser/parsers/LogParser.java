package org.cristian.priceputu.accesslogparser.parsers;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogParser {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogParser.class);
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public Stream<LogEntry> parse(URI path) throws IOException {
		final Reader reader = Files.newBufferedReader(Paths.get(path));
		final CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withDelimiter('|'));
		return StreamSupport.stream(csvParser.spliterator(), false).map(rec -> convertRecordToEntry(rec)).onClose(() -> {
			try {
				csvParser.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	protected LogEntry convertRecordToEntry(CSVRecord rec) {

		final LogEntry entry = new LogEntry();
		entry.setTs(new Date(DATE_FORMATTER.parseMillis(rec.get(0))));
		entry.setIp(rec.get(1));
		entry.setMethod(rec.get(2));
		entry.setResponse(rec.get(3));
		entry.setUserAgent(rec.get(4));

		// LOGGER.info("Entry no: {}", rec.getRecordNumber());
		return entry;
	}
}
