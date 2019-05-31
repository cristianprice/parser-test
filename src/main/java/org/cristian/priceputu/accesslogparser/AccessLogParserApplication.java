package org.cristian.priceputu.accesslogparser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cristian.priceputu.accesslogparser.data.importer.DataImporter;
import org.cristian.priceputu.accesslogparser.persistence.LogEntryRepository;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

@SpringBootApplication
public class AccessLogParserApplication implements ApplicationRunner {

	public static final String DAILY = "daily";
	public static final String HOURLY = "hourly";
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd.HH:mm:ss");

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogParserApplication.class);

	@Autowired
	private DataImporter importer;

	@Autowired
	private LogEntryRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(AccessLogParserApplication.class, args);
	}

	public void performImport(String file) {
		try {
			importer.importUri(new File(file).toURI());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void performCheck() {

	}

	public void parseArguments(String[] args) {
		final ArgumentParser parser = ArgumentParsers.newFor("Parser").build().defaultHelp(true)
				.description("Imports a log file to db or checks for access limits.");
		parser.addArgument("--startDate").type(String.class).help("Starting date of scanning.");
		parser.addArgument("--file").help("File to be imported.").type(String.class);
		parser.addArgument("--threshold").help("File to be imported.").type(Integer.class);
		parser.addArgument("--duration").choices(HOURLY, DAILY).type(String.class).help("File to be imported.");

		try {
			final Namespace ns = parser.parseArgs(args);

			final String file = ns.get("file");
			if (file != null) {
				performImport(file);
				System.exit(0);
			} else {

				final String startDate = ns.getString("startDate");
				final String duration = ns.getString("duration");

				if (startDate == null || duration == null || ns.getInt("threshold") == null) {
					throw new ArgumentParserException("Failed to parse startDate, threshold or duration.", parser);
				}

				final int threshold = ns.getInt("threshold");
				final DateTime startDt = DATE_FORMATTER.parseDateTime(startDate);

				List<?> showList;
				if (DAILY.equals(duration)) {
					showList = repository.checkDaily(startDt.toDate(), threshold);
				} else {
					showList = repository.checkHourly(startDt.toDate(), threshold);
				}

				for (Object o : showList) {
					LOGGER.info("{}", o);
				}
			}

		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		parseArguments(args.getSourceArgs());
	}

}
