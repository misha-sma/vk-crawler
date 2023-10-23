package crawler.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	private static final int PHONE_LENGTH = 11;
	private static final Pattern PHONE_PATTERN = Pattern.compile("[\\d \\-\\(\\)]{11,}");

	private static final String CAR_NUMBER_LETTERS_RU = "АВЕКМНОРСТУХ";
	private static final String CAR_NUMBER_LETTERS_EN = "ABEKMHOPCTYX";
	private static final String CAR_NUMBER_LETTERS_ALL = CAR_NUMBER_LETTERS_RU + CAR_NUMBER_LETTERS_EN;

	private static final Pattern CAR_NUMBER_PATTERN = Pattern
			.compile("[" + CAR_NUMBER_LETTERS_ALL + "]{1}\\d{3}[" + CAR_NUMBER_LETTERS_ALL + "]{2} ?(\\d{2,3})");

	private static final Map<Character, Character> EN_RU_CHARS_MAP = new HashMap<Character, Character>();

	static {
		EN_RU_CHARS_MAP.put('A', 'А');
		EN_RU_CHARS_MAP.put('B', 'В');
		EN_RU_CHARS_MAP.put('E', 'Е');
		EN_RU_CHARS_MAP.put('K', 'К');
		EN_RU_CHARS_MAP.put('M', 'М');
		EN_RU_CHARS_MAP.put('H', 'Н');
		EN_RU_CHARS_MAP.put('O', 'О');
		EN_RU_CHARS_MAP.put('P', 'Р');
		EN_RU_CHARS_MAP.put('C', 'С');
		EN_RU_CHARS_MAP.put('T', 'Т');
		EN_RU_CHARS_MAP.put('Y', 'У');
		EN_RU_CHARS_MAP.put('X', 'Х');
	}

	private Util() {
	}

	public static List<String> getPhones(String text) {
		List<String> phones = new ArrayList<String>();
		Matcher m = PHONE_PATTERN.matcher(text);
		while (m.find()) {
			String group = m.group();
			group = group.replaceAll("[ \\-\\(\\)]", "");
			if (group.length() == PHONE_LENGTH) {
				phones.add(group);
			}
		}
		return phones;
	}

	public static List<String> getCarNumbers(String text) {
		List<String> carNumbers = new ArrayList<String>();
		text = text.toUpperCase();
		Matcher m = CAR_NUMBER_PATTERN.matcher(text);
		while (m.find()) {
			String region = m.group(1);
			if (region.length() == 3) {
				char firstRegionDigit = region.charAt(0);
				if (!(firstRegionDigit == '1' || firstRegionDigit == '7')) {
					continue;
				}
			}
			String group = m.group();
			group = group.replace(" ", "");
			for (Character c : EN_RU_CHARS_MAP.keySet()) {
				group = group.replace(c, EN_RU_CHARS_MAP.get(c));
			}
			carNumbers.add(group);
		}
		return carNumbers;
	}

	public static String concatStrings(List<String> list) {
		if (list == null || list.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); ++i) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(list.get(i));
		}
		return builder.toString();
	}

}
