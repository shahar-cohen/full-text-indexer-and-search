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
public class STtvSeriesParsingRule implements IparsingRule, Serializable {

    public static final long serialVersionUID = 1L;
    private static final String END_LINE = "\n";

	/////////////////////////////             Constants                            /////////////////////////

    // Episode Name
    private final String EPISODE_NAME_ANCHOR = "STAR TREK: THE NEXT GENERATION";
    private final String TITLE_SEGMENT_REGEX = EPISODE_NAME_ANCHOR + "([\\s]+)(.*)";
    private final Pattern TITLE_SEGMENT_PATTERN = Pattern.compile(TITLE_SEGMENT_REGEX);
    private final String NO_TITLE_PLACE_HOLDER = "NO TITLE";

    //Scene Name
    private final String SCENE_NAME = "SceneName";
    private final String SCENE_NUMBER = "SceneNumber";
    /** the header starts with the scene number, zero or more spaces then the scene title */
    private final String SCENE_TITLE_REGEX =
            "(?<" + SCENE_NUMBER + ">^[0-9]+)([ ]*)(?<" + SCENE_NAME + ">.*)";
    private final Pattern SCENE_TITLE_PATTERN = Pattern.compile(SCENE_TITLE_REGEX);

    // Creator Credits
    private final String CREDIT_REGEX = "(?i)(CREDIT_REASON_PLACE_HOLDER)(by:?)([\\r\\n\\s]*)(.*)";
    private final String[] CREDIT_TYPE = {"Teleplay ", "Story ", "Directed ", ""};

    // Participating Actors
    private final String ACTOR = "actor";

    private final Pattern ADDRESSED_ACTOR_PATTERN = Pattern.compile("(^[\\t]{4})(\\()(to )(?<" + ACTOR+">.*)");
    private final Pattern SPEAKING_ACTOR_PATTERN = Pattern.compile("(^[\\t]{5})(?<" + ACTOR + ">.*)");

    // Result printing constants
    private final String RESULT_TITLE = "The result: \n";
    private final String RESULT_SCENE_TITLE = "was found in scene number %s titled \"%s\".";
    private final int RESULT_SCENE_TITLE_LINE = 0;
    private final String RESULT_ACTORS_PREFIX = "The actors that participated in the scene were: ";
    private final String CREDIT_REASON_PLACE_HOLDER = "CREDIT_REASON_PLACE_HOLDER";
    private final String RESULT_EPISODE_TITLE_PREFIX = "\nin the episode ";
    private final String RESULT_CREDITS_PREFIX = "That was"; // ( That was "created by.." )
//todo utils for all similarity's including Constants

    // todo generalize a bit more while testing
    // todo make the regex more precised ( "by" prefix catches more then it should)


//////////////////////////////// Parsing To Blocks ///////////////////////////////////////////////////////////



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
        ArrayList<Block> blockList = this.parseToBlocks(inputFile, SCENE_TITLE_PATTERN);


        //inject the episode title and the credits into all of the blocks
        Block creditBlock = blockList.remove(0);
        ArrayList<String> entryMetaData = new ArrayList<>();

        // Find Episode Name
        String titleSegment = this.stringByPattern( creditBlock, TITLE_SEGMENT_PATTERN);

        if (titleSegment != null){
            titleSegment = titleSegment.replace(EPISODE_NAME_ANCHOR, "");// trim the anchor
            titleSegment = titleSegment.trim(); // trim white space

            entryMetaData.add(titleSegment);
        }else{
            entryMetaData.add(NO_TITLE_PLACE_HOLDER);
        }

        // Find the Credits
        for ( String credit:CREDIT_TYPE) {
            Pattern creditPattern = Pattern.compile( CREDIT_REGEX.replace(CREDIT_REASON_PLACE_HOLDER,
                                                                                                credit) );

            String match = this.stringByPattern( creditBlock, creditPattern );
            if (match != null){
                entryMetaData.add(match);
            }
        }
        // inject them to the blocks
        for (Block block:blockList) {
            block.setMetadata(entryMetaData);
        }

        return blockList;

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
     * in the episode *Episode
     * that was                     ( Optional might be a few  )
     * CreditReason by CreditedCreatorsName
     *
     * @param wordResult The WordResult to be printed
     * @throws IOException
     */
    @Override
    public void printResult(WordResult wordResult) throws IOException {

        String[] sceneBlock = wordResult.getBlock().toString().split("\n");
        StringBuilder result = new StringBuilder();

        Matcher m;

        // The SceneTitle
        m = SCENE_TITLE_PATTERN.matcher(sceneBlock[RESULT_SCENE_TITLE_LINE]);
        if (m.matches()) {
            //todo assuming both or neither will be matched
            result.append(String.format(RESULT_SCENE_TITLE, m.group(SCENE_NUMBER), m.group(SCENE_NAME)));
        }

        // The participating actors
        StringBuilder participatingActors = new StringBuilder();
        for (String line:sceneBlock) {

            m = ADDRESSED_ACTOR_PATTERN.matcher(line);
            if ( m.find() ){
                participatingActors.append( m.group(ACTOR).replace(")", ", ") );
                continue;
            }

            m = SPEAKING_ACTOR_PATTERN.matcher(line);
            if( m.find() ){
                participatingActors.append(m.group(ACTOR) + ", ");
                continue;
            }

        }
        if ( participatingActors != null && participatingActors.length() > 0 ){

            result.append(RESULT_ACTORS_PREFIX);
            result.append(participatingActors);
            // replace the last ", " with "."
            result.delete(result.length() - 2, result.length() - 1);
            result.append('.');
        }

        List<String> entryMeta = wordResult.getBlock().getMetadata();

        // The episode name
        result.append(RESULT_EPISODE_TITLE_PREFIX + entryMeta.get(0) );

        // The episode credits
        if (entryMeta.size() > 0){
            result.append(RESULT_CREDITS_PREFIX);
            for(int i= 1;i < entryMeta.size();i++){
                result.append(entryMeta.get(i));
            }

        // Search result
        result.insert(0, wordResult.resultToString() + "\n");

        result.insert(0, RESULT_TITLE);

        System.out.println(result);

        }
        System.out.println(result);

    }

////////////////////////////////// Parser Utils //////////////////////////////////////////////////////////


    /** parses the file into blocks, using the BlockPrefixPattern,
     * Starts a new block when hits a line that fits the pattern.
     *
     * @param inputFile RandomAccessFile, if their are problems when reading from it will throw IOException.
     * @param blockPrefixPattern A regex pattern that fits only the first lines of the blocks
     * @return A list of the blocks,
     *              the first block is the text before the first regex pattern.
     */
    ArrayList<Block> parseToBlocks(RandomAccessFile inputFile,
                                   Pattern blockPrefixPattern )
            throws IOException {
        ArrayList<Block> blocks = new ArrayList<Block>();



        Predicate<String> isNewScene = blockPrefixPattern.asPredicate ();

        long currentPos = 0;
        long startPos = 0;
        long endPos;
        inputFile.seek(startPos);
        String currentLine = inputFile.readLine();

        while ( currentLine != null ) {
            currentPos = inputFile.getFilePointer();

            if (isNewScene.test(currentLine)) {
                endPos = currentPos - currentLine.length() -1;

                blocks.add( new Block(inputFile, startPos, endPos));

                startPos = endPos;

            }
            currentLine = inputFile.readLine();
        }
        // get last block
        endPos = currentPos - 1;
        blocks.add( new Block(inputFile, startPos, endPos));
        return blocks;
    }



    /**Gets the Block's RAF Channel and uses Scanner.findWithInHorizon() to return the first found match.
     * @param block the Block that should be searched.
     * @param stringLinePattern the Pattern that fits only the wanted result.
     * @return the first matching String that starts after after the offSet,
     *                  if their is no match will return null.
     */
    String stringByPattern(Block block, Pattern stringLinePattern) throws IOException{
        block.getRAF().seek( block.getStartIndex() );
        Scanner blockScanner = new Scanner(block.getRAF().getChannel());
        return blockScanner.findWithinHorizon( stringLinePattern,
                (int) ( block.getEndIndex() - block.getStartIndex() ) );

    }

/*
Generilizing to utils and breaking down to more modular pieces


Parsing is done the same way in both.

Getting the Entry Meta is done the same way in both ( just with different Patterns )


Given a result, we need the
 */
}





















