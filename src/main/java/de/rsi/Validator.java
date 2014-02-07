package de.rsi;

import gherkin.formatter.JSONFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	private static boolean hasErrors = false;

	public static void main(String[] args) throws Exception {

		final File path = new File(args[0]);
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
		if (hasErrors) System.exit(1);
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