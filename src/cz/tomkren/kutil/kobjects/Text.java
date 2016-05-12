package cz.tomkren.kutil.kobjects;

import cz.tomkren.kutil.core.KAttVal;

public class Text implements KAttVal {
    private final String str;
    public Text(String str) {this.str = str;}
    @Override public String toString() {return str;}
}
