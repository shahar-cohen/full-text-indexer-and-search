
package processing.parsingRules;

import processing.textStructure.Block;
import processing.textStructure.WordResult;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses a Entry-File to blocks and wraps + prints WordResults with their data
 * In owr opinion the parsers should have been Singletons inorder to save Regex-Pattern compile time.
 * (If they where Singletons, then only if a parsing object was asked for we would have created the
 *  patterns and only once ) */
public class STmovieParsingRule implements IparsingRule, Serializable {

    public static final long serialVersionUID = 1L;
    private static final String END_LINE = "\n";

/////////////////////////////             Constants                            /////////////////////////

    // Scene Title Pattern
    private final String SCENE_NUMBER = "sceneNumber";
    private final String SCENE_NAME = "sceneName";
    private final String SCENE_TITLE_REGEX =
            "(^[ ]*)(?<" + SCENE_NUMBER + ">[0-9]+[A-Z]*)(?<" + SCENE_NAME + ">.*)(\\2)";
    /**
     * A scene title can start with spaces then the scene number that might include capital letters
     * then the scene title and ends with the scene number.
     */
    private final Pattern SCENE_TITLE_PATTERN = Pattern.compile(SCENE_TITLE_REGEX);
    private final int RESULT_SCENE_TITLE_LINE = 0;

    // Credit Pattern

    /**
     * The supported credits
     */
    enum Credit {SCREENPLAY, STORY, WRITTEN}
    private final String CREDIT_REASON = "CREDIT_REASON";
    /**
     * A credit starts with the reason in one line and can be with capital letters,
     * the credited creators are in the next line.
     */
    private final String CREDIT_REGEX = "(?i)("  + CREDIT_REASON + " by: ?)([\\s]*)(.*)";


    // Actor Name Pattern
    private final String CHARACTER_NAME = "character";
    /**Character names are indented by 43 spaces */
    private final String PARTICIPATING_CHARACTER_REGEX = "(^[ ]{43})(?<" + CHARACTER_NAME + ">\\S+)([ ]*)";
    private final Pattern PARTICIPATING_CHARACTER_PATTERN = Pattern.compile(PARTICIPATING_CHARACTER_REGEX);

    // Result printing constants
    private final String RESULT_TITLE = "The result: \n";
    private final String RESULT_SCENE_TITLE = "\nWas found in scene number %s, titled \"%s\".";
    private final String RESULT_CHARACTER_PREFIX = "\nWith participating characters: ";
    private final String RESULT_ENTRY_PREFIX = "\nFound in entry: ";
    private final String RESULT_CREDITS_PREFIX = "\nThat was ";
    private final String FILE_SEPARATOR = System.getProperty("file.separator");

    ///////////////////// Parsing To Blocks ///////////////////////////////////////////////////////////


    /**
     Not used explained in Interface
     */
    @Override
    public Block parseRawBlock(RandomAccessFile inputFile, long startPos, long endPos) {

        return null;
    }

    /** Parses the input Entry file in to blocks and injects the entry meta-data in to each block.
     * @param inputFile The RandomAccessFile from which we are reading.
     * @return An ArrayList <Block> of the blocks in the entry
     * @throws IOException If file is invalid or does not fit the supported pattern
     */
    @Override
    public List<Block> parsedFile(RandomAccessFile inputFile) throws IOException {


        // parseRawBlocks
        ArrayList<Block> blocksList = this.parseToBlocks(inputFile, SCENE_TITLE_PATTERN);

        //inject the credits into the blocks.
        ArrayList<String> entryMetaData = new ArrayList<>();

        //get the credits
        Block creditsBlock = blocksList.remove(0);
        for (Credit credit : Credit.values()) {

            Pattern creditPattern = Pattern.compile(CREDIT_REGEX.replace("CREDIT_REASON", credit.name()));

            String match = this.stringByPattern(creditsBlock, creditPattern);
            if (match != null) {
                entryMetaData.add(match);
            }
        }
        // inject them to the blocks
        for (Block block : blocksList) {
            block.setMetadata(entryMetaData);
        }

        return blocksList;
    }

    ////////////////////// Printing A Result ///////////////////////////////////////////////

    /**
     * Prints the result in the following format:
     * <p>
     * The result:
     * *Result                ( *Result is defined in WordResult )
     * <p>
     * was found in scene number *Number titled *SceneTitle.
     * The actors that participated in the scene were: Actor1, Actor2,...
     * in the movie *Movie       ( *Movie is the name of the file/entry where the result was found )
     * that was                     ( Optional might be a few  )
     * CreditReason by CreditedCreatorsName
     *
     * @param wordResult The WordResult to be printed
     * @throws IOException
     */
    @Override
    public void printResult(WordResult wordResult) throws IOException {

        String[] sceneBlock = wordResult.getBlock().toString().split(END_LINE);
        StringBuilder result = new StringBuilder();

        // The SceneTitle
        Matcher m = SCENE_TITLE_PATTERN.matcher(sceneBlock[RESULT_SCENE_TITLE_LINE]);

        if (m.find()) {
            result.append(String.format(RESULT_SCENE_TITLE,
                                        m.group(SCENE_NUMBER), m.group(SCENE_NAME).trim()));
        }
        // The participating actors
        result.append(RESULT_CHARACTER_PREFIX);

        m = PARTICIPATING_CHARACTER_PATTERN.matcher("");

        StringBuilder characters = new StringBuilder();
        for (String line : sceneBlock) {
            m.reset(line);

            if (m.find()){
                if (! characters.toString().equals("")) { characters.append(", "); }
                String name = m.group(CHARACTER_NAME);
                characters.append(name.split("'")[0]);
            }
        }

        result.append(characters.toString());

        //The movie title
        result.append(RESULT_ENTRY_PREFIX).append
                (Paths.get(wordResult.getBlock().getEntryName()).getFileName());

        // The movie credits
        StringBuilder creditBuilder = new StringBuilder();
        List<String> credits = wordResult.getBlock().getMetadata();
        if(credits != null && !credits.isEmpty()){
            result.append(RESULT_CREDITS_PREFIX);

            for ( String credit: wordResult.getBlock().getMetadata() ) {

                if (! creditBuilder.toString().equals("")) { creditBuilder.append(", "); }

                for (String partialCred : credit.split("\n")){
                    creditBuilder.append(" ").append(partialCred.trim());
                }
            }
        }

        result.append(creditBuilder.toString());

        // Search result
        result.insert(0, wordResult.resultToString() + "\n");

        result.insert(0, RESULT_TITLE);

        System.out.println(result);


    }


    ////////////////////////////////// Parser Utils //////////////////////////////////////////////////////////


    /**
     * parses the file into blocks, using the BlockPrefixPattern,
     * Starts a new block when hits a line that fits the pattern.
     *
     * @param inputFile          RandomAccessFile, if their are problems when reading from it will throw IOException.
     * @param blockPrefixPattern A regex pattern that fits only the first lines of the blocks
     * @return A list of the blocks,
     * the first block is the text before the first regex pattern.
     */
    ArrayList<Block> parseToBlocks(RandomAccessFile inputFile,
                                   Pattern blockPrefixPattern)
            throws IOException {
        ArrayList<Block> blocks = new ArrayList<Block>();


        Predicate<String> isNewScene = blockPrefixPattern.asPredicate();

        long currentPos = 0;
        long startPos = 0;
        long endPos;
        inputFile.seek(startPos);
        String currentLine = inputFile.readLine();

        while (currentLine != null) {
            currentPos = inputFile.getFilePointer();

            if (isNewScene.test(currentLine)) {
                endPos = currentPos - currentLine.length() - 1;

                blocks.add(new Block(inputFile, startPos, endPos));

                startPos = endPos;

            }
            currentLine = inputFile.readLine();
        }
        // get last block
        endPos = currentPos - 1;
        blocks.add(new Block(inputFile, startPos, endPos));
        return blocks;
    }


    /**Gets the Block's RAF Channel and uses Scanner.findWithInHorizon() to return the first found match.
     * @param block the Block that should be searched.
     * @param stringLinePattern the Pattern that fits only the wanted result.
     * @return the first matching String that starts after after the offSet,
     *                  if their is no match will return null.
     */
    String stringByPattern(Block block, Pattern stringLinePattern) throws IOException {
        block.getRAF().seek(block.getStartIndex());

        Scanner blockScanner = new Scanner(block.getRAF().getChannel());
        return blockScanner.findWithinHorizon(stringLinePattern,
                (int) (block.getEndIndex() - block.getStartIndex()));

    }

}



