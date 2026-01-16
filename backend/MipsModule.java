package backend;

import backend.Data.MipsData;
import backend.Instruction.MipsInstr;

import java.util.ArrayList;

public class MipsModule {

    private ArrayList<MipsData> mipsDatas;

    private ArrayList<MipsInstr> mipsInstrs;

    public MipsModule() {
        this.mipsDatas = new ArrayList<>();
        this.mipsInstrs = new ArrayList<>();
    }

    public void addData(MipsData mipsData) {
        this.mipsDatas.add(mipsData);
    }

    public void addInstruction(MipsInstr mipsInstr) {
        this.mipsInstrs.add(mipsInstr);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (MipsData mipsData : mipsDatas) {
            sb.append(mipsData);
            sb.append("\n");
        }
        sb.append("\n\n");
        sb.append(".text\n");
        for (MipsInstr mipsInstr : mipsInstrs) {
            sb.append(mipsInstr);
            sb.append("\n");
        }
        return sb.toString();
    }
}
