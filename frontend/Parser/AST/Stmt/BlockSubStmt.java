package frontend.Parser.AST.Stmt;

import frontend.Parser.AST.Block;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Node;

public class BlockSubStmt extends Node implements BlockItem, Stmt {
    private Block block;

    public BlockSubStmt(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (block != null) {
            sb.append(block.toString());
        }
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
