package cz.tomkren.kutil.core.masters;

import cz.tomkren.utils.Log;
import cz.tomkren.kutil.core.Cmd;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Created by tom on 23.7.2015 */

public class KeyboardMaster {

    private JSONObject jsonKeyBindings;
    private Map<String,Cmd> keyBindings;

    public KeyboardMaster(Kutil kutil) {

        jsonKeyBindings = kutil.getConfig().getJSONObject("keyBindings");
        keyBindings = new HashMap<>();

        for (String keyName : getKeyNames()) {

            String wholeCmd = jsonKeyBindings.getString(keyName);


            //Cmd cmd = kutil.getCmdMaster().getCmd(wholeCmd);
            Cmd cmd = kutil.getCmdMaster().compileSimpleCmd(wholeCmd);

            if (cmd == null) {
                Log.err("KEY-BINDING-ERROR --- "+keyName+" : "+wholeCmd+" --- UNKNOWN cmdName '"+wholeCmd+"' !!!");
            } else {
                keyBindings.put(keyName, cmd);
            }
        }
    }


    public Set<String> getKeyNames() {
        return jsonKeyBindings.keySet();
    }

    public void keyboardEvent(String str) {

        Cmd cmd = keyBindings.get(str);

        if (cmd == null) {

            Log.it("ERROR: neodchycená klávesa !!!");
            Log.it( "keyBindings.containsKey("+str+") = "+ keyBindings.containsKey(str) );
            Log.collection(getKeyNames());

        } else {

            String cmdResultMsg = cmd.apply0();

            if (cmdResultMsg != null) {
                Log.it(cmdResultMsg);
            }

        }
    }


        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        else if( "SPACE"                .equals(str) ){ openConsole();     }
        else if( "F2"                   .equals(str) ||
                "control R"            .equals(str) ){ renameDialog();    }
        else if( "LEFT"                 .equals(str) ){ figureCmd(Figure.FigureCmd.left);  }
        else if( "RIGHT"                .equals(str) ){ figureCmd(Figure.FigureCmd.right); }
        else if( "UP"                   .equals(str) ){ figureCmd(Figure.FigureCmd.up);    }
        else if( "DOWN"                 .equals(str) ){ figureCmd(Figure.FigureCmd.down);  }
        else if( "control LEFT"         .equals(str) ){ allFigureCmd(Figure.FigureCmd.left);  }
        else if( "control RIGHT"        .equals(str) ){ allFigureCmd(Figure.FigureCmd.right); }
        else if( "control UP"           .equals(str) ){ allFigureCmd(Figure.FigureCmd.up);    }
        else if( "control DOWN"         .equals(str) ){ allFigureCmd(Figure.FigureCmd.down);  }
        else if( "shift LEFT"           .equals(str) ){ figureCmd(Figure.FigureCmd.shiftLeft);  }
        else if( "shift RIGHT"          .equals(str) ){ figureCmd(Figure.FigureCmd.shiftRight); }
        else if( "shift UP"             .equals(str) ){ figureCmd(Figure.FigureCmd.shiftUp);    }
        else if( "shift DOWN"           .equals(str) ){ figureCmd(Figure.FigureCmd.shiftDown);  }
        else if( "control shift LEFT"   .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftLeft);  }
        else if( "control shift RIGHT"  .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftRight); }
        else if( "control shift UP"     .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftUp);    }
        else if( "control shift DOWN"   .equals(str) ){ allFigureCmd(Figure.FigureCmd.shiftDown);  }
        else if( "B"                    .equals(str) ){ changeFigure();  }

        //KeyEvent.VK_
        */



}
