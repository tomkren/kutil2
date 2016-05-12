package cz.tomkren.kutil.core.masters;

import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.Editor;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.frame.Frame;

/** Created by tom on 23.7.2015. */

public class SelectionMaster {

    private final Kutil kutil;

    private KObject selected;      // označený KObjekt - projevuje se červeným okrajem
    private Frame selectedFrame;   // označený Frame   - projevuje se zeleným okrajem

    public SelectionMaster(Kutil kutil) {
        this.kutil = kutil;

        selected = null;
        selectedFrame = null;

        kutil.registerCmd(Cmd.enter, this::enter);
        kutil.registerCmd(Cmd.delete, this::delete);
        kutil.registerCmd(Cmd.leave, this::leave);
        kutil.registerCmd(Cmd.copy, this::putSelectedToClipboard);
        kutil.registerCmd(Cmd.cut,  this::cut);
        kutil.registerCmd(Cmd.showXML, this::showXML);

    }

    public KObject getSelected() {
        return selected;
    }

    public Frame getSelectedFrame() {
        return selectedFrame;
    }

    public void setSelected(KObject o) {
        if( selected != null ){
            selected.setHighlighted(false);
        }

        selected = o;
        selected.setHighlighted(true);

        /* TODO ...
        if (selected instanceof Figure) {
            setActualFigure((Figure)selected);
        }

        if( ! ((selected instanceof Inputable) || (selected instanceof Outputable) ) ){
            resetFrom();
        }
        */
    }

    public void setSelectedFrame(Frame f) {
        if (selectedFrame != null) {
            selectedFrame.setHighlightedFrame(false);
        }
        selectedFrame = f;
        selectedFrame.setHighlightedFrame(true);
    }

    public void putSelectedToClipboard() {
        kutil.getClipboardMaster().setClipboard(selected);
    }

    public void cut() {

        kutil.getClipboardMaster().setClipboard(selected);

        delete();

        //throw new TODO();
    }

    public void enter() {
        if (selected == null || selectedFrame == null) return;
        if (selected.getIsGuiStuff()) return;

        kutil.getSimulationMaster().saveStateToUndoBuffer();

        selectedFrame.resetTarget(selected.id());
        selectedFrame.setCam(Int2D.zero());

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String cmd = selected.getOnEnterCmd();
        if(cmd != null) {
            cmd(cmd);
        }
        */
    }

    public void leave() {
        if (selectedFrame == null) return;
        if (selectedFrame.getTarget().parent() == null) return;
        if (selectedFrame.getTarget().getIsGuiStuff()) return;

        kutil.getSimulationMaster().saveStateToUndoBuffer();

        selectedFrame.resetTarget(selectedFrame.getTarget().parent().id());
        selectedFrame.setCam(Int2D.zero());
    }

    public void delete() {
        if (selected == null) {return;}
        if (selected == selectedFrame) {return;}
        if (selected.getIsGuiStuff()) {return;}

        kutil.getSimulationMaster().saveStateToUndoBuffer();

        selected.delete();

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if (selected == actualFigure) {
            changeFigure(actualFigure);
        }
        */

        selected = null;
    }

    public void showXML() {
        new Editor(new Int2D(100,100), selected.toXml().toString());
    }






}
