package fr.miage.fsgbd;

import java.util.HashMap;

public class TestPointeur implements Executable<HashMap<String, Integer>> {
    public boolean execute(HashMap<String, Integer> arg1, HashMap<String, Integer> arg2) {
        return arg1.keySet().toArray()[0].toString().length() > arg2.keySet().toArray()[0].toString().length();
    }
}