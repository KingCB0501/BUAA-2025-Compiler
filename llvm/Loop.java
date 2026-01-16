package llvm;

import llvm.UserClass.BasicBlock;

/**
 * Stmt :== 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class Loop {
    private BasicBlock initBlock;   // for_stmt_1
    private BasicBlock condBlock;    // cond
    private BasicBlock followBlock;   // for_stmt_2
    private BasicBlock loopBodyBlock;
    private BasicBlock endBlock;    // forStmt之后的Block

    public Loop(BasicBlock initBlock, BasicBlock condBlock, BasicBlock followBlock, BasicBlock loopBodyBlock, BasicBlock endBlock) {
        this.initBlock = initBlock;
        this.condBlock = condBlock;
        this.followBlock = followBlock;
        this.loopBodyBlock = loopBodyBlock;
        this.endBlock = endBlock;
    }

    public BasicBlock getInitBlock() {
        return initBlock;
    }

    public BasicBlock getCondBlock() {
        return condBlock;
    }

    public BasicBlock getFollowBlock() {
        return followBlock;
    }

    public BasicBlock getLoopBodyBlock() {
        return loopBodyBlock;
    }

    public BasicBlock getEndBlock() {
        return endBlock;
    }

}
