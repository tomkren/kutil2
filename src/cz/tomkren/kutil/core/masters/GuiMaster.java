package cz.tomkren.kutil.core.masters;


import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.kobjects.frame.Frame;

/** Created by tom on 23.7.2015. */

public class GuiMaster {

    private Kutil kutil;
    private boolean showGUI;
    private boolean showInfo; // zda ukazovat u objektů stručné info

    public GuiMaster(Kutil kutil, boolean showGUI) {
        this.kutil = kutil;
        this.showGUI  = showGUI;
        this.showInfo = false;

        kutil.registerCmd(Cmd.showInfo, this::toggleShowInfo);
        kutil.registerFullCmd(Cmd.changeFrameTarget, this::changeFrameTarget);
    }

    public boolean showGUI() {
        return showGUI;
    }

    public boolean showInfo() {
        return showInfo;
    }

    public void toggleShowInfo() {
        showInfo = !showInfo;
    }

    public String changeFrameTarget(String frameId, String newTargetId) {
        KObject frame = kutil.getIdDB().get(frameId);

        if (frame != null && frame instanceof Frame) {
            // radši zkontrolujeme i newTarget
            if (kutil.getIdDB().get(newTargetId) != null) {
                ((Frame) frame).resetTarget(newTargetId);
                return "Target of frame "+frameId+" changed to "+newTargetId;
            } else {
                return "ERROR: "+newTargetId+" is not a correct KObject id.";
            }
        } else {
            return "ERROR: "+frameId+" is not a correct frame id.";
        }
    }


}
