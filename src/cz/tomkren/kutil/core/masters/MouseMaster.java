package cz.tomkren.kutil.core.masters;

import cz.tomkren.utils.Log;
import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.KObjectFactory;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.frame.Frame;

import java.awt.*;
import java.awt.event.MouseEvent;

/** Created by tom on 23.7.2015. */

public class MouseMaster {

    private Kutil kutil;

    private int mouseX; // x-pozice myši
    private int mouseY; // y-pozice myši


    private KObject onCursor;   // KObject visící na kurzoru

    private Int2D   onCursorClickPos; //relativní pozice kliknutí vůči pozici KObjectu na kurzoru
    private boolean lastTimeCut; //zda bylo naposledy vyjmuto (HAX)

    private Int2D   onDragAbsPos; // absolutní pozice kurzoru při přesouvání
    private Int2D   onDragPos;    // relativní pozice kurzoru při přesouvání

    private boolean brushInsertMode;





    public MouseMaster(Kutil kutil) {
        this.kutil = kutil;

        mouseX = 0;
        mouseY = 0;
        onCursor = null;
        brushInsertMode = false;

        kutil.registerCmd(Cmd.copyToCursor, this::copyToCursor);
        kutil.registerCmd(Cmd.pasteToCursor, this::pasteToCursor);
        kutil.registerCmd(Cmd.changeInsertMode, this::changeInsertMode);
    }


    public void pasteToCursor() {
        copyToCursor(kutil.getClipboardMaster().getClipboard());
    }


    // TODO rename to copySelectedToCursor
    public void copyToCursor() {
        copyToCursor(kutil.getSelectionMaster().getSelected());
    }


    public Int2D getMousePos() {
        return new Int2D(mouseX, mouseY);
    }


    public void onMove(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    /*public void onClick() {
        kutil.getPopupMaster().closePopupIfVisible();
    }*/

    public void clickedRightMouseButton(Frame frame, int mouseX , int mouseY) {

        if (onCursor != null) {
            onCursor = null;
            return;
        }

        kutil.getPopupMaster().togglePopup(frame, mouseX, mouseY);
    }




    /**
     * Metoda umožňující InputMastrovi vykreslit to co je na kurzoru do GUI.
     * @param g Graphics2D kam se bude kreslit
     */
    public void paintScreen(Graphics2D g) {
        try{
            if (lastTimeCut && onCursor != null) {

                Int2D center    = onDragAbsPos.minus(onDragPos);
                Int2D objectPos = onDragPos.plus(onCursorClickPos);

                onCursor.setPos(objectPos);
                onCursor.drawOutside(g, center , 0, 1.0, center); // todo opravdu dobře zoom center?

            }
            else if(!lastTimeCut && onCursor != null) {

                onCursor.setPos(getMousePos());
                onCursor.drawOutside(g, Int2D.zero , 0, 1.0, Int2D.zero); // todo opravdu dobře zoomCenter ?

            }
        }
        catch(NullPointerException e){
            Log.it("Chycena výjimka při vykreslení, nevadí.");
        }
    }



    public void changeInsertMode(){
        brushInsertMode = ! brushInsertMode;
    }

    /**
     * Odpovídá na otázku, zda právě něco visí na kurzoru.
     * @return odpověď na otázku , zda právě něco visí na kurzoru.
     */
    public boolean somethingOnCursor(){
        return onCursor != null;
    }

    /**
     * zkopíruje KObjekt na kurzor.
     * @param o co se má zkopírovat
     */
    public void copyToCursor(KObject o) {
        if (onCursor != null) return;

        KObject copy = o.copy();

        KObjectFactory.insertKObjectToSystem(copy, null, kutil);

        onCursor         = copy;
        onCursorClickPos = Int2D.zero;

        kutil.getSelectionMaster().setSelected(onCursor);

        lastTimeCut = false;
    }



    /**
     * Vyjmout na kurzor.
     * @param o co se má vyjmout
     * @param clickPos pozice kliknutí
     */
    public void cutToCursor( KObject o , Int2D clickPos ){
        if(onCursor != null) return;

        KObject parent = o.parent();
        if (parent == null) {return;}

        kutil.getSimulationMaster().saveStateToUndoBuffer();

        parent.remove(o);
        onCursor = o;
        onCursorClickPos = onCursor.pos().minus(clickPos);

        kutil.getSelectionMaster().setSelected(onCursor);

        lastTimeCut = true;
    }

    /**
     * Vlož z kurzoru do virtuálního světa.
     * @param newParent nový rodičovský KObbject
     * @param clickPos pozice kam bylo kliknuto pro vložení
     */
    public void pasteFromCursor( KObject newParent , Int2D clickPos ){
        if( onCursor == null ) return;

        Int2D pos = clickPos.plus(onCursorClickPos);

        /* TODO !!!
        if( ((Basic)onCursor).getAlign15() ){
            pos = pos.align(15);
        }
        */

        onCursor.setPos(pos);
        onCursor.setParent(newParent);
        newParent.add(onCursor);

        onCursor.setSpeed(Int2D.zero);

        KObject oldOnCursor = onCursor;

        onCursor = null;

        if (brushInsertMode) {
            copyToCursor(oldOnCursor);
        }
    }

    /**
     * Informování o relativní pozici myši při táhnutí
     * @param mousePos pozice myši při táhnutí
     */
    public void onDrag(Int2D mousePos){
        onDragPos = mousePos;
    }

    /**
     * Informování o absolutní pozici myši při táhnutí.
     * @param e MouseEvent způsobený táhnutím myši
     */
    public void onDrag(MouseEvent e){
        onDragAbsPos = new Int2D( e.getX(), e.getY() );
    }



}
