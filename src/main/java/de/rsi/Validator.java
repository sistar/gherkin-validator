package de.rsi;

import gherkin.formatter.JSONFormatter;
import gherkin.parser.Parser;
import gherkin.util.FixJava;

import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Validator {
	public static void main(String[] args) throws Exception {

		String path = args[0];
		String gherkin = FixJava.readReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8"));
		System.out.println("gherkin...\n" + gherkin);

		StringBuilder json = new StringBuilder();

		System.out.println("json: '" + json + "'");

		JSONFormatter formatter = new JSONFormatter(json);

		System.out.println("formatter: " + formatter.toString());

		Parser parser = new Parser(formatter);

		System.out.println("parser: " + parser.toString());
		parser.parse(gherkin, path, 0);
		formatter.done();
		formatter.close();
		System.out.println("json: '" + json + "'"); // Gherkin source as JSON
	}

}
