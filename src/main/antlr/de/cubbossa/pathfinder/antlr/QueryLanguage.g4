grammar QueryLanguage;

@header {
    package de.cubbossa.pathfinder.antlr;
}

program
   : expression EOF
   ;

expression
   : term
   | NOT lhs=expression
   | OPEN lhs=expression CLOSE
   | lhs=expression op=AND rhs=expression
   | lhs=expression op=OR rhs=expression
   ;

term
   : IDENTIFIER attributeblock
   | IDENTIFIER
   ;

attributeblock
   : ATTR_OPEN ATTR_CLOSE
   | ATTR_OPEN attributelist ATTR_CLOSE
   ;

attributelist
   : attributelist ATTR_DELIMIT attribute
   | attribute
   ;

attribute
   : IDENTIFIER comparator value
   ;

value
   : STRING
   | BOOL
   | INTEGER
   | WORD
   | IDENTIFIER
   ;

comparator
   : COND_EQ | COND_NOT | COND_GE | COND_GT | COND_LE | COND_LT
   ;


COND_NOT: '!=';
COND_EQ: '=';
COND_GT: '>';
COND_GE: '>=';
COND_LT: '<';
COND_LE: '<=';

NOT: '!';
AND: '&';
OR: '|';

OPEN: '(';
CLOSE: ')';

ATTR_OPEN: '[';
ATTR_CLOSE: ']';
ATTR_DELIMIT: ',';

IDENTIFIER: [a-zA-Z][a-zA-Z0-9_-]*;
BOOL: ('0'|'1')('b'|'B') | 'true' | 'false';
WORD: [a-zA-Z0-9]+;
INTEGER: [0-9]+;
STRING  : '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ;

fragment
HEX_DIGIT  : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;
fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

WHITESPACE: [ \r\n\t]+ -> skip;