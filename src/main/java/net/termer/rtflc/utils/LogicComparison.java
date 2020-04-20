package net.termer.rtflc.utils;

/**
 * Enumeration for defining logical comparisons
 * @author termer
 * @since 1.0
 */
public enum LogicComparison {
	EQUAL,
	AND,
	OR,
	GREATER,
	LESS;
	
	/**
	 * Returns the LogicComparison corresponding to a comparison character.
	 * Valid characters are: =, &, |, >, < 
	 * @param ch The character to check
	 * @return The LogicComparison corresponding to the provided character, or null if character has no LogicComparison associated with it
	 * @since 1.0
	 */
	public static LogicComparison byChar(char ch) {
		LogicComparison comp = null;
		
		switch(ch) {
		case '=':
			comp = EQUAL;
			break;
		case '&':
			comp = AND;
			break;
		case '|':
			comp = OR;
			break;
		case '>':
			comp = GREATER;
			break;
		case '<':
			comp = LESS;
			break;
		}
		
		return comp;
	}
	/**
	 * Returns the character corresponding to a LogicComparison
	 * @param c The LogicComparison to be converted to a character
	 * @return The character corresponding to a LogicComparison
	 * @since 1.0
	 */
	public static char toChar(LogicComparison c) {
		char ch = '=';
		
		switch(c) {
		case EQUAL:
			ch = '=';
			break;
		case AND:
			ch = '&';
			break;
		case OR:
			ch = '|';
			break;
		case GREATER:
			ch = '>';
			break;
		case LESS:
			ch = '<';
			break;
		}
		
		return ch;
	}
}