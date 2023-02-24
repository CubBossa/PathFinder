grammar SelectionLanguage;

@header {
    package de.cubbossa.pathfinder.antlr;
}

program
   : expression EOF
   ;

expression
   : sel=selector
   | sel=selector conditions
   ;

selector
   : AT IDENTIFIER
   ;

conditions
   : COND_OPEN COND_CLOSE
   | COND_OPEN attributelist COND_CLOSE
   ;

attributelist
   : attributelist COND_DELIMIT attribute
   | attribute
   ;

attribute
   : IDENTIFIER COND_EQUALS value
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
