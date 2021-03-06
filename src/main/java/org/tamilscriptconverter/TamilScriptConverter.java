package org.tamilscriptconverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Selvakumar
 * @since 1.0
 */
public class TamilScriptConverter
{
    public static final char VOWEL_SIGN_AA = '\u0bbe';
    public static final char VOWEL_SIGN_I = '\u0bbf';
    public static final char VOWEL_SIGN_II = '\u0bc0';
    public static final char VOWEL_SIGN_U = '\u0bc1';
    public static final char VOWEL_SIGN_UU = '\u0bc2';
    public static final char VOWEL_SIGN_EE = '\u0bc7';
    public static final char VOWEL_SIGN_E = '\u0bc6';
    public static final char VOWEL_SIGN_AI = '\u0bc8';
    public static final char VOWEL_SIGN_O = '\u0bca';
    public static final char VOWEL_SIGN_OO = '\u0bcb';
    public static final char VOWEL_SIGN_AU = '\u0bcc';
    public static final char PULLI = '\u0bcd';
    public static final List<Character> VOWEL_SIGNS = Arrays.asList(VOWEL_SIGN_AA, VOWEL_SIGN_I, VOWEL_SIGN_II,
            VOWEL_SIGN_U, VOWEL_SIGN_UU, VOWEL_SIGN_E, VOWEL_SIGN_EE, VOWEL_SIGN_AI, VOWEL_SIGN_O, VOWEL_SIGN_OO,
            VOWEL_SIGN_AU);
    private static final String STARTS_WITH_NUMBER_REGEX = "^[0-9]{1,2}.+";
    private static final String STARTING_NUMBER_REGEX = "^[0-9]{1,2}.";
    private static Logger logger = LoggerFactory.getLogger(TamilScriptConverter.class);
    private static Map<String, String> charMap = new HashMap<>();

    static {
        //uyir
        charMap.put("அ", "a");
        charMap.put("ஆ", "aa");
        charMap.put("இ", "i");
        charMap.put("ஈ", "ee");
        charMap.put("உ", "u");
        charMap.put("ஊ", "oo");
        charMap.put("எ", "e");
        charMap.put("ஏ", "ae");
        charMap.put("ஐ", "ai");
        charMap.put("ஒ", "o");
        charMap.put("ஓ", "oa");
        //mei
        charMap.put("க்", "k");
        charMap.put("ங்", "ng");
        charMap.put("ச்", "ch");
        charMap.put("ஜ்", "j");
        charMap.put("ஞ்", "nj");
        charMap.put("ட்", "t");
        charMap.put("ண்", "n");
        charMap.put("த்", "th");
        charMap.put("ந்", "n");
        charMap.put("ன்", "n");
        charMap.put("ப்", "p");
        charMap.put("ம்", "m");
        charMap.put("ய்", "y");
        charMap.put("ர்", "r");
        charMap.put("ற்", "tr");
        charMap.put("ல்", "l");
        charMap.put("ள்", "l");
        charMap.put("ழ்", "zh");
        charMap.put("வ்", "v");
        charMap.put("ஷ்", "sh");
        charMap.put("ஸ்", "s");

        //uyirmei
        charMap.put("க", "ka");
        charMap.put("ங", "nga");
        charMap.put("ச", "sa");
        charMap.put("ஞ", "nya");
        charMap.put("ட", "ta");
        charMap.put("ண", "na");
        charMap.put("த", "tha");
        charMap.put("ந", "na");
        charMap.put("ன", "na");
        charMap.put("ப", "pa");
        charMap.put("ம", "ma");
        charMap.put("ய", "ya");
        charMap.put("ர", "ra");
        charMap.put("ற", "ra");
        charMap.put("ல", "la");
        charMap.put("ள", "la");
        charMap.put("ழ", "zha");
        charMap.put("வ", "va");
        charMap.put("ஷ", "sha");
        charMap.put("ஸ", "sa");
        charMap.put("ஜ", "ja");
    }

    public static void convertFiles(File source) throws IOException
    {
        if (source.exists()) {
            if (source.isFile()) {
                convertFile(source);
            } else {
                File[] files = source.listFiles();
                logger.info("Preparing to convert the files in {}", files.length, source);
                for (File file : files) {
                    convertFile(file);
                }
                logger.info("Finished converting the files!", files.length);
            }
        } else {
            logger.error("File {} doesn't exist!", source);
        }
    }

    static void convertFile(File source) throws IOException
    {
        convertFile(source, getTargetFile(source));
    }

    static void convertFile(File source, File target) throws IOException
    {
        if (source != null && source.exists() && source.isFile()) {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            logger.info("Preparing to convert Tamil script in the source {} to {}...", source.getName(), target);
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(source));
                writer = new BufferedWriter(new FileWriter(target));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(formatTamilText(line) + "\r\n");
                    if (StringUtils.isNotBlank(line)) {
                        writer.write(formatConvertedText(convert(line)) + "\r\n");
                    }
                }
                logger.info("Finished converting {}", source);
            } catch (IOException ex) {
                logger.error("Error occurred while reading source file", ex);
            } finally {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
        } else {
            logger.error("File {} doesn't exist!", source);
        }
    }

    static String formatTamilText(String text)
    {
        if (StringUtils.isNotBlank(text)) {
            if(isTextStartsWithNumber(text)){
                return getVerseTag((text)) + "\r\n" + getTextWithFormattingTag(text);
            }else {
               return getTextWithFormattingTag(text);
            }
        }
        return text;
    }

    static String getTextWithFormattingTag(String text)
    {
        return "{y}" + text + "{/y}";
    }

    static String getVerseTag(String text)
    {
        return "---[Verse:" + getVerseNumber(text) + "]---";
    }

    static String getVerseNumber(String text)
    {
        Pattern pattern = Pattern.compile(STARTING_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()){
            return StringUtils.removeEnd(matcher.group(0), ".").trim();
        }
        return "";
    }

    static String formatConvertedText(String text)
    {
        String textToConvert = text;
        if (isTextStartsWithNumber(text)) {
            textToConvert = StringUtils.removePattern(text, STARTING_NUMBER_REGEX);
        }
        return StringUtils.capitalize(textToConvert.trim());
    }

    static boolean isTextStartsWithNumber(String text)
    {
        return text.matches(STARTS_WITH_NUMBER_REGEX);
    }

    public static String convert(String text)
    {
        StringBuilder convertedWord = new StringBuilder();
        List<String> unicodeChars = splitUnicodeChars(text);
        for (int i = 0; i < unicodeChars.size(); i++) {
            String unicodeChar = unicodeChars.get(i);
            logger.trace("Unicode char: {}", unicodeChar);
            String previousChar = i > 0 ? unicodeChars.get(i - 1) : "  ";
            if (endsWithVowelSign(unicodeChar)) {
                convertedWord.append(convertCharWithVowelSign(unicodeChar, previousChar));
            } else {
                convertedWord.append(convertChar(unicodeChar));
            }
        }
        return convertedWord.toString();
    }

    public static List<String> splitUnicodeChars(String input)
    {
        logger.trace("Input string: {}", input);
        List<String> unicodeChars = new ArrayList<>();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int nextCharIndex = i + 1;
            if (nextCharIndex < chars.length) {
                logger.trace("Preparing to add the char: {}", chars[i]);
                if (isSignAfterChar(chars[nextCharIndex])) {
                    unicodeChars.add(chars[i] + "" + chars[nextCharIndex]);
                } else if (!isSignAfterChar(chars[i])) {
                    unicodeChars.add(chars[i] + "");
                }
            } else if (!isSignAfterChar(chars[i])) {
                unicodeChars.add(chars[i] + "");
            }
        }
        logger.trace("Unicode chars: {}", unicodeChars);
        return trimUnicodeChars(unicodeChars);
    }

    public static String convertChar(String charToBeConverted)
    {
        logger.trace("Converting the tamil char: {}", charToBeConverted);
        String convertedString = charMap.get(charToBeConverted);
        return convertedString != null ? convertedString : charToBeConverted;
    }

    public static String convertChar(String charToBeConverted, String previousChar)
    {
        logger.trace("Converting the tamil char \"{}\" whose previous char is \"{}\"", charToBeConverted, previousChar);
        String convertedString = "";
        switch (previousChar) {
            case "ஞ்":
                if (charToBeConverted.equals("ச")) {
                    convertedString = "";
                } else {
                    charMap.get(charToBeConverted);
                }
                break;
            default:
                charMap.get(charToBeConverted);
        }
        logger.trace("Converted string: {}", convertedString);
        return StringUtils.isBlank(convertedString) ? convertedString : charToBeConverted;
    }

    static String convertCharWithVowelSign(String unicodeChar, String previousChar)
    {
        if (unicodeChar.length() > 1) {
            logger.trace("Unicode char: {}, previous char: {}", unicodeChar, previousChar);
            String convertedChar = "";
            char[] chars = unicodeChar.toCharArray();
            char[] previousChars = previousChar.toCharArray();
            char firstCharPart = chars[0];
            char secondCharPart = chars[1];
            logger.trace("First char part: {}, second char part: {}", firstCharPart, secondCharPart);
            switch (secondCharPart) {
                case VOWEL_SIGN_AA:
                    return convertChar(firstCharPart + "") + "a";
                case VOWEL_SIGN_I:
                    if (firstCharPart == 'ற') {
                        return "ri";
                    }
                    return convertChar(firstCharPart + "" + PULLI) + "i";
                case VOWEL_SIGN_II:
                    return convertChar(firstCharPart + "" + PULLI) + "ee";
                case VOWEL_SIGN_U:
                    if (previousChars[0] != 'ச' && firstCharPart == 'ச') {
                        return "su";
                    } else if (firstCharPart == 'ற') {
                        return "ru";
                    }
                    return convertChar(firstCharPart + "" + PULLI) + "u";
                case VOWEL_SIGN_UU:
                    return convertChar(firstCharPart + "" + PULLI) + "oo";
                case VOWEL_SIGN_E:
                    return convertChar(firstCharPart + "" + PULLI) + "e";
                case VOWEL_SIGN_EE:
                    logger.trace("Character has vowel sign {}", VOWEL_SIGN_EE);
                    if (StringUtils.isBlank(previousChar) && firstCharPart == 'ச') {
                        return "sae";
                    }
                    return convertChar(firstCharPart + "" + PULLI) + "ae";
                case VOWEL_SIGN_AI:
                    return convertChar(firstCharPart + "" + PULLI) + "ai";
                case VOWEL_SIGN_O:
                    return convertChar(firstCharPart + "" + PULLI) + "o";
                case VOWEL_SIGN_OO:
                    return convertChar(firstCharPart + "" + PULLI) + "oa";
            }
        }
        return unicodeChar;
    }

    static boolean isSignAfterChar(char tamilChar)
    {
        return PULLI == tamilChar || VOWEL_SIGNS.contains(tamilChar);
    }

    static boolean isVowelSign(char tamilChar)
    {
        return VOWEL_SIGNS.contains(tamilChar);
    }

    static boolean endsWithVowelSign(String unicodeChar)
    {
        return unicodeChar.length() > 1 && isVowelSign(unicodeChar.toCharArray()[unicodeChar.length() - 1]);
    }

    public static List<String> trimUnicodeChars(List<String> unicodeChars)
    {
        if (unicodeChars.size() > 2) {
            logger.trace("Preparing to trim unicode chars...");
            logger.trace("Char at index 0: {}, char at index 1: {}", unicodeChars.get(0), unicodeChars.get(1));
            if (unicodeChars.get(0).equals("இ")) {
                String secondChar = unicodeChars.get(1);
                switch (secondChar) {
                    case "யே":
                    case "ர":
                        unicodeChars.remove(0);
                }
            } else {
                logger.trace("Nothing to trim");
            }
        }
        return unicodeChars;
    }

    public static File getTargetFile(File source)
    {
        return new File(source.getParent() + File.separator + "converted", source.getName());
    }
}