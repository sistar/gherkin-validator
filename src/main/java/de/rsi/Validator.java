package de.rsi;

import gherkin.formatter.JSONFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	private static boolean hasErrors = false;

	public static void main(String[] args) throws Exception {
		final File path;
		if (args.length > 0) {
			path = new File(args[0]);
		} else {
			String userHome = System.getProperty("user.home");
			File conf = new File(new File(userHome), ".lhotse-config.properties");
			if (conf.isFile()) {
				Properties prop = new Properties();
				InputStream input = null;

				try {
					input = new FileInputStream(conf);
					prop.load(input);
					// get the property value and print it out
					String ltngDir = prop.getProperty("lhotse-tracking-nextgen-dir");
					System.out.println(ltngDir);
					path = new File(ltngDir, "ts-processor/src/test/resources/cucumber");
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				throw new RuntimeException("no file/folder as argument and no conf at" +conf.getAbsolutePath());
			}

		}
		if (path.isDirectory()) {
			for (final File file : path.listFiles(new FileFilter() {
				@Override
				public boolean accept(final File pathname) {
					return pathname.getAbsolutePath().endsWith(".feature");
				}
			})) {
				parseFeature(file);

			}
		} else {
			if (path.isFile()) {
				parseFeature(path);
			} else {
				System.exit(1);
			}
		}
		if (hasErrors) {
			System.out.println("*** GHERKIN SYNTAX ERRORS *** DO NOT PUSH ***");
			System.exit(1);
		}   else {
			System.out.println("*** OK ***");
		}
	}

	private static void parseFeature(File f) throws FileNotFoundException, UnsupportedEncodingException {

		String gherkin = FixJava.readReader(new InputStreamReader(
				new FileInputStream(f), "UTF-8"));


		StringBuilder json = new StringBuilder();

		JSONFormatter formatter = new JSONFormatter(json);
		try {
			Parser parser = new Parser(formatter);
			parser.parse(gherkin, f.getAbsolutePath(), 0);
			System.out.println("parsing: " + f.getAbsolutePath());

		} catch (Exception e) {
			final String message = e.getMessage();
			if (message.startsWith("Lexing error")) {
				Pattern pattern = Pattern.compile("Lexing error on line (\\d*): (.*)");
				Matcher match = pattern.matcher(message);
				if (match.find()) {
					System.out.printf("File \"%s\", line %s Lexing error: %s\n",
							f.getAbsolutePath(), match.group(1), match.group(2));
					hasErrors = true;
				}
			}
		}
		formatter.done();
		formatter.close();
	}
}