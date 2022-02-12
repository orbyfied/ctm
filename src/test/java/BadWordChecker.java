public class BadWordChecker {

    static final String[] badWords = new String[] {
            "fuck", "shit"
    };

    // how many characters off to be considered ok
    static final int errorThreshold = 2;

    public static boolean checkWord(String toCheck) {

        boolean hasBadWord = false;

        // get words in string by splitting it by space and iterate
        String[] split = toCheck.split(" ");
        for (String wordToCheck : split) {
            // iterate over bad words
            for (String badWord : badWords) {

                int error = 0;
                // get smallest length
                int len = Math.min(badWord.length(), wordToCheck.length());
                // count error (sadly in position cuz im not smart enough to make it complex)
                for (int i = 0; i < len; i++) {
                    if (badWord.charAt(i) != wordToCheck.charAt(i))
                        error++;
                }

                // check if it is bad
                if (error <= errorThreshold) {
                    hasBadWord = true;
                    break;
                }

            }
        }

        // return
        return hasBadWord;
    }

    ////////////////////////////////////////

    public static void main(String[] args) {
        System.out.println(checkWord("f&-k this"));
    }
}
