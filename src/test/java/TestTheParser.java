import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

public class TestTheParser {
	
	private TheParser parser;
	private Vector<TheToken> tokens;

	@Test
	public void testParserInputFour() throws IOException{
		System.out.println("testing One Line Bodies =====================================================================");
		File file = new File("src/main/resources/inputOneLineBodies.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}

	@Test
	public void testParserInputThree() throws IOException {
		System.out.println("testing statements ==========================================================================");
		File file = new File("src/main/resources/inputStatements.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputTwo() throws IOException {
		System.out.println("testing assignments =========================================================================");
		File file = new File("src/main/resources/inputAssignment.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputOne() throws IOException {
		System.out.println("testing method declaration ==================================================================");
		File file = new File("src/main/resources/inputMethods.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
	@Test
	public void testParserInputZero() throws IOException {
		System.out.println("testing class declaration ===================================================================");
		File file = new File("src/main/resources/inputClass.txt");
		TheLexer lexer = new TheLexer(file);
		lexer.run();
		Vector<TheToken> tokens = lexer.getTokens();
		parser = new TheParser(tokens);
		assertDoesNotThrow(() -> parser.run());
	}
	
}