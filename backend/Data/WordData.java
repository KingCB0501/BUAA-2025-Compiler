package backend.Data;

import java.util.ArrayList;

public class WordData extends MipsData {
    private ArrayList<Integer> inits;

    public WordData(String name, ArrayList<Integer> init) {
        super(name);
        this.inits = init;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(": .word ");
        sb.append(inits.get(0));
        for (int i = 1; i < inits.size(); i++) {
            sb.append(", ");
            sb.append(inits.get(i));
        }
        return sb.toString();
    }
}
