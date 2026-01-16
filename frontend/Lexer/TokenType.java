package frontend.Lexer;

/**
 * 单词名称	类别码	单词名称	类别码	单词名称	类别码	单词名称	类别码
 * Ident	IDENFR	else	ELSETK	*	MULT	;	SEMICN
 * IntConst	INTCON	!	NOT	/	DIV	,	COMMA
 * StringConst	STRCON	&&	AND	%	MOD	(	LPARENT
 * const	CONSTTK	||	OR	<	LSS	)	RPARENT
 * int	INTTK	for	FORTK	<=	LEQ	[	LBRACK
 * static	STATICTK	return	RETURNTK	>	GRE	]	RBRACK
 * break	BREAKTK	void	VOIDTK	>=	GEQ	{	LBRACE
 * continue	CONTINUETK	+	PLUS	==	EQL	}	RBRACE
 * if	IFTK	-	MINU	!=	NEQ	=	ASSIGN
 * main	MAINTK	printf	PRINTFTK
 */
public enum TokenType {
    IDENFR,       // Ident
    INTCON,       // IntConst
    STRCON,       // StringConst
    CONSTTK,      // const
    INTTK,        // int
    STATICTK,     // static
    BREAKTK,      // break
    CONTINUETK,   // continue
    IFTK,         // if
    MAINTK,       // main
    ELSETK,       // else
    NOT,          // !
    AND,          // &&
    OR,           // ||
    FORTK,        // for
    RETURNTK,     // return
    VOIDTK,       // void
    PLUS,         // +
    MINU,         // -
    PRINTFTK,     // printf
    MULT,         // *
    DIV,          // /
    MOD,          // %
    LSS,          // <
    LEQ,          // <=
    GRE,          // >
    GEQ,          // >=
    EQL,          // ==
    NEQ,          // !=
    SEMICN,       // ;
    COMMA,        // ,
    LPARENT,      // (
    RPARENT,      // )
    LBRACK,       // [
    RBRACK,       // ]
    LBRACE,       // {
    RBRACE,       // }
    ASSIGN,       // =
    EOF,
}
