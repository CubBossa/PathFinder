grammar SelectionSuggestionLanguage;

program
   : expression EOF
   ;

expression
   : AT
   | AT IDENTIFIER
   | AT IDENTIFIER conditions
   ;

conditions
   : COND_OPEN
   | COND_OPEN attributelist
   | COND_OPEN COND_CLOSE
   | COND_OPEN attributelist COND_CLOSE
   ;

attributelist
   : attributelist COND_DELIMIT
   | attributelist COND_DELIMIT attribute
   | attribute
   ;

attribute
   : IDENTIFIER
   | IDENTIFIER COND_EQUALS
   | IDENTIFIER COND_EQUALS value
   ;

value
   : expression
   | QUOTE
   | IDENTIFIER
   | STRING+
   ;

AT: '@';
COND_OPEN: '[';
COND_CLOSE: ']';
COND_DELIMIT: ',';
COND_EQUALS: '=';

QUOTE  : '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ;
IDENTIFIER: [a-zA-Z][a-zA-Z0-9_-]*;
STRING: (~[,=])+?;

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
