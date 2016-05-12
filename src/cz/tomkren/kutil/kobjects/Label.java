package cz.tomkren.kutil.kobjects;

import cz.tomkren.kutil.core.KAtts;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.items.KItem;
import cz.tomkren.kutil.shapes.TextShape;

import java.awt.*;

/** Created by user on 28. 11. 2015.*/

public class Label extends KObject {

    private KItem<String> txt;
    private KItem<Integer> fontSize;

    public Label(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);
        setType("label");

        txt = items().addString(kAtts, "txt", "");
        fontSize = items().addInteger(kAtts, "fontSize", 12);

        setShape(new TextShape(txt.get(),fontSize.get()));
    }





}
