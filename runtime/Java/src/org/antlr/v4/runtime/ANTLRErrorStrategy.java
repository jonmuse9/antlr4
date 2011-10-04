package org.antlr.v4.runtime;

/** The interface for defining strategies to deal with syntax errors
 *  encountered during a parse by ANTLR-generated parsers and tree parsers.
 *  We distinguish between three different kinds of errors:
 *
 *  	o The parser could not figure out which path to take in the ATN
 *        (none of the available alternatives could possibly match)
 *      o The current input does not match what we were looking for.
 *      o A predicate evaluated to false.
 *
 *  The default implementation of this interface reports errors to any
 *  error listeners of the parser. It also handles single token insertion
 *  and deletion for mismatched elements.
 *
 *  We pass in the parser to each function so that the same strategy
 *  can be shared between multiple parsers running at the same time.
 *  This is just for flexibility, not that we need it for the default system.
 *
 *  TODO: To bail out upon first error, simply rethrow e?
 *
 *  TODO: what to do about lexers
 */
public interface ANTLRErrorStrategy {
	/** Report any kind of RecognitionException. */
	void reportError(BaseRecognizer recognizer,
					 RecognitionException e)
		throws RecognitionException;

	/** When matching elements within alternative, use this method
	 *  to recover. The default implementation uses single token
	 *  insertion and deletion. If you want to change the way ANTLR
	 *  response to mismatched element errors within an alternative,
	 *  implement this method.
	 *
	 *  From the recognizer, we can get the input stream to get
	 *  the current input symbol and we can get the current context.
	 *  That context gives us the current state within the ATN.
	 *  From that state, we can look at its transition to figure out
	 *  what was expected.
	 *
	 *  Because we can recover from a single token deletions by
	 *  "inserting" tokens, we need to specify what that implicitly created
	 *  token is. We use object, because it could be a tree node.
	 */
	Object recoverInline(BaseRecognizer recognizer)
		throws RecognitionException;

	/** Resynchronize the parser by consuming tokens until we find one
	 *  in the resynchronization set--loosely the set of tokens that can follow
	 *  the current rule.
	 */
	void recover(BaseRecognizer recognizer);

	/** Make sure that the current lookahead symbol is consistent with
	 *  what were expecting at this point in the ATN. You can call this
	 *  anytime but ANTLR only generates code to check before loops
	 *  and each iteration.
	 *
	 *  Implements Jim Idle's magic sync mechanism in closures and optional
	 *  subrules. E.g.,
	 *
	 * 		a : sync ( stuff sync )* ;
	 * 		sync : {consume to what can follow sync} ;
	 *
	 *  Previous versions of ANTLR did a poor job of their recovery within
	 *  loops. A single mismatch token or missing token would force the parser
	 *  to bail out of the entire rules surrounding the loop. So, for rule
	 *
	 *  classDef : 'class' ID '{' member* '}'
	 *
	 *  input with an extra token between members would force the parser to
	 *  consume until it found the next class definition rather than the
	 *  next member definition of the current class.
	 *
	 *  This functionality cost a little bit of effort because the parser
	 *  has to compare token set at the start of the loop and add each
	 *  iteration. If for some reason speed is suffering for you, you can
	 *  turn off this functionality by simply overriding this method as
	 *  a blank { }.
	 */
	void sync(BaseRecognizer recognizer);

	/** Notify handler that parser has entered an error state.  The
	 *  parser currently doesn't call this--the handler itself calls this
	 *  in report error methods.  But, for symmetry with endErrorCondition,
	 *  this method is in the interface.
	 */
	void beginErrorCondition(BaseRecognizer recognizer);

	/** Is the parser in the process of recovering from an error? Upon
	 *  a syntax error, the parser enters recovery mode and stays there until
	 *  the next successful match of a token. In this way, we can
	 *  avoid sending out spurious error messages. We only want one error
	 *  message per syntax error
	 */
	boolean inErrorRecoveryMode(BaseRecognizer recognizer);

	/** Reset the error handler. Call this when the parser
	 *  matches a valid token (indicating no longer in recovery mode)
	 *  and from its own reset method.
	 */
	void endErrorCondition(BaseRecognizer recognizer);
}
