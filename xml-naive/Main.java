public class Main
{
    public static String test = "<foo bar=\"qux\">baz</foo>";

    public static String transformXml(final String input) {
        String result = "";

        Boolean isInNode = false;
        Boolean isInOpeningTag = false;
        Boolean isPastOpeningTag = false;
        Boolean isMaybeClosingTag = false;
        Boolean isInClosingTag = false;
        Boolean isInTagName = false;
        Boolean isInAttribute = false;
        Boolean isInAttributeKey = false;
        Boolean isInAttributeValue = false;
        Boolean isInTextNode = false;

        for (char ch: input.toCharArray()) {
            switch(ch) {
            case '<':
                isInNode = true;
                if (isPastOpeningTag) {
                    isMaybeClosingTag = true;
                } else {
                    isInOpeningTag = true;
                }
            }
            break;

            // And so on and so forth
        }

        return result;
    }

    public static void main (String[] args)
    {
        // Output the transformed string
        System.out.println(transformXml(test));
    }
}
