package cz.tomkren.kutil.core.masters;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.Procedure;
import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.KObjectFactory;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/** Created by tom on 23.7.2015. */

public class CmdMaster {

    private Kutil kutil;
    private Map<String,Cmd> cmds;
    private ConcurrentLinkedQueue<String> cmdQueue;

    public CmdMaster(Kutil kutil) {
        this.kutil = kutil;
        cmds = new HashMap<>();
        cmdQueue = new ConcurrentLinkedQueue<>();
    }


    public void offerCmd(String cmd) {
        cmdQueue.offer(cmd);
    }

    public void executeHeadCmd() {
        if (!cmdQueue.isEmpty()) {
            String cmd = cmdQueue.poll();
            cmd(cmd);
        }
    }


    public Cmd getCmd(String cmdName) {
        return cmds.get(cmdName);
    }

    public void registerFullCmd(String cmdName, Cmd cmdCode) {
        cmds.put(cmdName, cmdCode);
        Log.it(cmdName+" REGISTERED");
    }

    public void registerCmd(String cmdName, Consumer<List<String>> cmdCode) {
        registerFullCmd(cmdName, args -> {
            cmdCode.accept(args);
            return "Command '" + cmdName + Joiner.on(" ").join(args) + "' performed.";
        });
    }

    public void registerCmd(String cmdName, Procedure cmdCode) {
        registerCmd(cmdName, args -> cmdCode.invoke());
    }

    public void registerFullCmd(String cmdName, BiFunction<String,String,String> cmdCode) {
        registerFullCmd(cmdName, args -> cmdCode.apply(args.get(0),args.get(1)));
    }



    /*public Cmd compileCmd(String cmd) {

        String[] simpleCmds = cmd.split(";");

        Cmd acc = args -> "";

        for (String simpleCmd : simpleCmds) {

        }

        throw new TODO();
    }*/

    public Cmd compileSimpleCmd(String cmd) {

        String[] parts = cmd.trim().split("\\s+");
        String cmdName = parts[0];


        Cmd cmdCode = cmds.get(cmdName);

        if (cmdCode == null) {return null;}

        List<String> args = parts.length > 0 ? Arrays.asList(parts).subList(1,parts.length) : Collections.emptyList();

        return fakeArgs -> cmdCode.apply(args);
    }



    public List<String> cmd(String cmd) {

        if (cmd == null) {
            return Collections.emptyList();
        }

        if (cmd.substring(0,4).equals("json")) {
            return Collections.singletonList(jsonCmd(cmd.substring(4).trim()));
        }

        String[] cmds = cmd.split(";");

        List<String> ret = new ArrayList<>();

        for (String simpleCmd : cmds) {
            ret.add(simpleCmd(simpleCmd));
        }

        Log.list(ret); // TODO dočasné

        return ret;
    }

    private String jsonCmd(String jsonCmd) {

        try {
            JSONObject jsonObj = new JSONObject(jsonCmd);

            // TODO zatím explicitně add, nutno zobecnit -------------------------------------------------------------    TODO !!!

            if (jsonObj.getString("cmd").equals("add")) {

                String parentId = jsonObj.getString("parentId");

                Object o = jsonObj.get("object");

                if (o instanceof JSONObject) {
                    return KObjectFactory.addNewKObject(parentId, (JSONObject) o, kutil);
                } else if(o instanceof JSONArray) {
                    return KObjectFactory.addNewKObjects(parentId, (JSONArray) o, kutil);
                } else {
                    return "ERROR in CmdMaster.jsonCmd(..): object to add must be JSONObject or JSONArray.";
                }


            }

            return "ERROR in CmdMaster.jsonCmd(..): Unknown jsonCmd.";

        } catch (JSONException e) {
            return "ERROR in CmdMaster.jsonCmd(..), JSONException msg: "+ e.getMessage();
        }


    }

    private String simpleCmd(String cmd) {

        String[] parts = cmd.trim().split("\\s+");
        String cmdName = parts[0];

        Function<List<String>,String> cmdCode = cmds.get(cmdName);

        if (cmdCode == null) {
            return "ERROR : Unknown command '"+ cmdName +"' !!!";
        } else {
            List<String> args = Arrays.asList(parts).subList(1, parts.length);
            return cmdCode.apply(args);
        }

    }



        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        if( Cmd.sendTo.equals(cmdName) ){
            if( parts.length < 3 ) return "U sendTo musí být id a příkaz.";
            KObject o = Global.idDB().get( parts[1] );
            if( o != null ){
                return o.cmd( cmd.split("\\s+" , 3)[2] );
            }
            return parts[1] + " není platné id.";
        }
        if( Cmd.changeXML.equals(cmdName) ){
            if( parts.length != 3 ) return Cmd.changeXML+" potřebuje 2 argumenty.";
            changeXML( parts[1] , parts[2] , true );
            return "XML změněno.";
        }
        if( Cmd.console.equals(cmdName) ){
            openConsole();
            return "Konzole otevřena.";
        }
        if( Cmd.rename.equals(cmdName) ){
            if( parts.length != 3 ) return Cmd.rename+" potřebuje 2 argumenty.";
            return ( rename( parts[1], parts[2] )
                    ? "Povedlo se přejmenovat."
                    : "Nepovedlo se přejmenovat." );
        }
        if( Cmd.load.equals(cmdName) ){
            return load();
        }
        if( Cmd.save.equals(cmdName) ){
            return save();
        }
        if( Cmd.xml.equals(cmdName) ){
            if( parts.length < 2 ) return "U "+Cmd.xml + " musí být argument (vkládané xml).";
            String xml = cmd.split("\\s+" , 2)[1] ;
            fromXmlToCursor(xml);
            return "XML (snad) vloženo jako KObject.";
        }
        if( Cmd.bgcolor.equals(cmdName) ){
            if( parts.length < 3 ) return "U "+Cmd.bgcolor + " musí být 2 argumenty: [id] [color r g b]";
            String[] ps = cmd.split("\\s+" , 3);
            return changeBgcolor( ps[1] , ps[2] );
        }
        if( Cmd.selectedFrameTarger.equals(cmdName) ){
            if( parts.length < 2 ) return "U "+Cmd.selectedFrameTarger + " musí být 1 argumenty: [id]";
            return changeActualFrameTarget(parts[1]);
        }
        */




}
