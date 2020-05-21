package processing.parsingRules;

import processing.textStructure.Block;
import processing.textStructure.WordResult;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * a class implementing a simple text parsing rule.
 */
public class SimpleParsingRule implements IparsingRule, Serializable {
	public static final long serialVersionUID = 1L;

	public SimpleParsingRule() {

	}



	@Override
	public Block parseRawBlock(RandomAccessFile inputFile, long startPos, long endPos) {

		return new Block(inputFile, startPos, endPos);
	}

	@Override
	public List<Block> parsedFile(RandomAccessFile inputFile) {

		final Pattern p = Pattern.compile(getSplitRegex(), Pattern.DOTALL);
		final Matcher m = p.matcher("");



		List<Block> entryBlocks = new LinkedList<>();
		int rawChunkSize = MAXLINELENGTH * 15;
		byte[] rawBytes = new byte[rawChunkSize];
		try {
			long endOfBlockOffset = 0, curBlockEnd;
			Long lastIndex = inputFile.length();
			for (long i = endOfBlockOffset; i < lastIndex; i += rawChunkSize) {
				inputFile.seek(i);
				int bytesRead = inputFile.read(rawBytes);
				String rawBlock = new String(rawBytes);
				m.reset(rawBlock);

				while (m.find()) {


					if (m.end()-m.start() > 1) {
						entryBlocks.add(parseRawBlock(inputFile, m.start() + i, m.end() + i));
					}
					endOfBlockOffset = m.end();
				}
				i -= (rawChunkSize - endOfBlockOffset);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return entryBlocks;

	}

	@Override
	public void printResult(WordResult wordResult) throws IOException {
		System.out.println("The result: \n" + wordResult.resultToString());
	}

	private String getSplitRegex() {
		return "(.*\\n){1,5}";
	}



}
