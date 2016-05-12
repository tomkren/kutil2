package cz.tomkren.kutil.core.masters;

import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.KObjectFactory;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.core.Cmd;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class SimulationMaster {

    private Kutil kutil;

    private boolean isSimulationRunning; // zda běží simulace

    private KObject main; // reference na KObject do kterého se nahrává ze souboru a u kterého se zaznamenává editační historie
    private LinkedList<String> undoBuffer; //buffer editační historie směrem do minulosti
    private LinkedList<String> redoBuffer; //buffer editační historie směrem do budoucnosti



    public SimulationMaster(Kutil kutil) {
        this.kutil = kutil;

        isSimulationRunning = kutil.getConfig().has("autoPlay") && kutil.getConfig().getBoolean("autoPlay");



        undoBuffer = new LinkedList<>(); // todo zvážit, dáat spíš něco jako ArrayDeque
        redoBuffer = new LinkedList<>(); // todo zvážit, dáat spíš něco jako ArrayDeque



        kutil.registerCmd(Cmd.play, this::togglePausePlay);
        kutil.registerCmd(Cmd.undo, this::undo);
        kutil.registerCmd(Cmd.redo, this::redo);
        kutil.registerFullCmd(Cmd.add,  this::add);
    }

    public KObject getMain() {
        return main;
    }

    public void onGo() {
        if (isSimulationRunning) {
            saveStateToUndoBuffer();
        }
    }


    public boolean isSimulationRunning() {
        return isSimulationRunning;
    }

    public void togglePausePlay() {
        isSimulationRunning = !isSimulationRunning;
        if (isSimulationRunning) {
            saveStateToUndoBuffer();
        }
    }


    public void undo() {
        if (main != null && (!undoBuffer.isEmpty())) {

            String lastState = undoBuffer.getFirst();
            undoBuffer.removeFirst();
            redoBuffer.addFirst(main.toXml().toString());

            loadToMain(lastState);
        }
    }

    public void redo() {
        if (main != null && (!redoBuffer.isEmpty())) {

            String nextState = redoBuffer.getFirst();
            redoBuffer.removeFirst();
            undoBuffer.addFirst( main.toXml().toString() );

            loadToMain(nextState);
        }
    }

    public String add(List<String> args) {

        if (args.size() != 2) {
            return "ERROR: add cmd must have 2 args (<parentId> <json>)!";
        }

        String parentId = args.get(0);
        try {
            JSONObject json = new JSONObject(args.get(1));
            return KObjectFactory.addNewKObject(parentId, json, kutil);
        } catch (JSONException e) {
            return "ERROR in cmd add (JSON error), msg: "+e.getMessage();
        }

    }


    public void setMain(KObject o) {
        main = o;
    }

    public void saveStateToUndoBuffer() {
        if (main != null) {
            undoBuffer.addFirst(main.toXml().toString());
            redoBuffer.clear();
        }
    }

    private void loadToMain(String xmlString) {
        KObject parent = main.parent();

        main.delete();

        KObject o = KObjectFactory.newKObject(xmlString, kutil);
        parent.add(o);
        o.setParent(parent);

        Set<String> affectedIds = o.getIds();

        kutil.getScheduler().forEachTime(t -> t.resolveCopying_2(affectedIds));

        isSimulationRunning = false;
    }






}
