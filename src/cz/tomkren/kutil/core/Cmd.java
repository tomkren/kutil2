package cz.tomkren.kutil.core;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Tato třída je výčet hodnot jednotlivých příkazů, které mohou být zadány např. konzolí
 * nebo tlačítkem.
 * @author Tomáš Křen
 */

// TODO předělat na enum
public interface Cmd extends Function<List<String>,String> {

    String apply(List<String> args);

    default String apply0() {
        return apply(Collections.emptyList());
    }

    // Mouse
    String copyToCursor = "copyToCursor";
    String pasteToCursor = "pasteToCursor";
    String changeInsertMode = "changeInsertMode";

    // Selection
    String enter = "enter";
    String leave = "leave";
    String delete = "delete";
    String copy = "copy";
    String cut = "cut";
    String showXML = "showXML";

    // Simulation
    String play = "play";
    String undo = "undo";
    String redo = "redo";
    String add  = "add";

    // Player
    String player = "player";

    // GUI
    String showInfo = "showInfo";
    String changeFrameTarget = "changeFrameTarget";



    /* todo
    String sendTo               = "sendTo";
    String changeFrameTarget    = "changeFrameTarget";
    String console              = "console";
    String changeXML            = "changeXML";
    String rename               = "rename";
    String load                 = "load";
    String save                 = "save";
    String xml                  = "xml";
    String bgcolor              = "bgcolor";
    String selectedFrameTarger  = "selectedFrameTarger";
    */



}
