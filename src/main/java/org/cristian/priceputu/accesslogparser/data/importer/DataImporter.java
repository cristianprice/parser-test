package org.cristian.priceputu.accesslogparser.data.importer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.cristian.priceputu.accesslogparser.model.LogEntry;
import org.cristian.priceputu.accesslogparser.parsers.LogParser;
import org.cristian.priceputu.accesslogparser.persistence.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataImporter {

	@Autowired
	private LogEntryRepository repo;

	@Autowired
	private LogParser parser;

	public void importUri(URI uri) throws IOException {
		final int chunkSize = 500;
		final Stream<LogEntry> stream = parser.parse(uri);
		final List<LogEntry> batch = new ArrayList<>(chunkSize);

		stream.forEach(e -> {
			if (batch.size() >= chunkSize) {
				repo.save(batch);
				batch.clear();
			}

			batch.add(e);
		});

		if (!batch.isEmpty()) {
			repo.save(batch);
		}
	}
}
