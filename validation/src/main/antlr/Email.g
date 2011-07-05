grammar Email;


options {
	backtrack=true;
	memoize=true;
}

@parser::header {
package net.nicl.jaev.mail;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
}

@lexer::header{ 
package net.nicl.jaev.mail;

} 

@members
{
private static final Logger LOG =
	LoggerFactory.getLogger(EmailParser.class);


public class AddressInfo
{
  protected AddressInfo(String localPart, List<String> domain,
    String phrase, boolean literal)
  {
    this.localPart = localPart;
    this.domain = domain;
    this.phrase = phrase;
    this.literal = literal;
  }

  private final String localPart;

  private final List<String> domain;

  private final String phrase;

  private final boolean literal;

  public String getLocalPart()
  {
    return localPart;
  }

  public List<String> getDomain()
  {
    return domain;
  }

  public String getPhrase()
  {
    return phrase;
  }

  public boolean isLiteral()
  {
    return literal;
  }
}

@Override
public void reportError(RecognitionException recognitionException)
{
	super.reportError(recognitionException);
	if (LOG.isDebugEnabled())
	{
		LOG.debug("reportError: " + recognitionException.getMessage(),
			recognitionException);
	}
}

protected class ParseException extends RuntimeException
{
	private ParseException(RecognitionException recognitionException)
	{
		super(recognitionException);
	}
}
}

CHAR 	:	'\u0000'..'\u007F';
COMMENT	:	'(' ~(')')*')' {$channel=HIDDEN;};

qtext	: ~('"'|'\\');
quotedString
	: '"'(qtext|quotedPair)*'"';
quotedPair
	: '\\'.;

ctext	: ~('('| ')'| '\\');	
comment	: '(' (ctext | quotedPair | comment)* ')'
	;
atom: (~('\u0000'..'\u0009' | '\u000E'..'\u001F' | '(' | ')' | '<' | '>' | '@' | ',' |  ';' | ':' | '\\' | '"' | '.' | '[' | ']'))+;

word 
	: atom 
	| quotedString 
//	| comment 
	; 

phrase	: word+;

localPart
	: word ('.' word)*;
domainLiteral
	: '[' (options{greedy=false;}:.)*']';
subDomain
	: atom
	| domainLiteral {$mailbox::literal=true;};
domain returns [List<String> subdomains]
	@init { $subdomains = new java.util.ArrayList<String>(); }
	: fsd=subDomain {$subdomains.add($fsd.text);} ('.' nsd= subDomain {$subdomains.add($nsd.text);})*
	;

addrSpec: localPart'@'domain {	$mailbox::localPart=$localPart.text; 
				$mailbox::domain=$domain.subdomains;};
routeAddr
	: '<'route? addrSpec '>';
route	: ('@' domain (',''@' domain)*)':';
mailbox returns [AddressInfo info]
	scope {
		String localPart;
		List<String> domain;
		boolean literal;
	}
	@init { $mailbox::literal=false; }
	: addrSpec {$info = new AddressInfo($mailbox::localPart, $mailbox::domain, null, $mailbox::literal);}
	| phrase routeAddr {$info = new AddressInfo($mailbox::localPart, $mailbox::domain, $phrase.text, $mailbox::literal);}
	;

group returns [List<AddressInfo> list]
	@init { $list = new java.util.ArrayList<AddressInfo>(); }
	: phrase':' (fbx = mailbox {$list.add($fbx.info);} (',' nbx=mailbox {$list.add($nbx.info);} )*)? ';' 
	;
address	returns [List<AddressInfo> list]
	: mailbox {$list = new java.util.ArrayList<AddressInfo>(); list.add($mailbox.info);}
	| group {$list = $group.list;}
	;
	catch [RecognitionException re] {
		 throw new ParseException(re);
	}
