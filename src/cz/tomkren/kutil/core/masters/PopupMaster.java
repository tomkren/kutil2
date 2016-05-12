package cz.tomkren.kutil.core.masters;

import cz.tomkren.utils.AA;
import cz.tomkren.utils.Log;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.frame.Frame;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

/** Created by tom on 23.7.2015 */



public class PopupMaster implements ActionListener {

    private CmdMaster cmdMaster;
    private List<AA<String>> items;
    private JFrame popupFrame;  // pomocný frame při vytváření popup okna (HAX)

    public PopupMaster(Kutil kutil) {
        cmdMaster = kutil.getCmdMaster();
        popupFrame = null;

        JSONArray jsonItems = kutil.getConfig().getJSONArray("popupMenu");
        int numItems = jsonItems.length();
        items = new ArrayList<>(numItems);

        for (int i=0; i<numItems; i++) {

            Object jsonItem = jsonItems.get(i);

            if (jsonItem instanceof String) {

                String cmdName = (String) jsonItem;
                items.add(new AA<>(cmdName,cmdName));

            } else if (jsonItem instanceof JSONObject) {

                JSONObject objItem = (JSONObject) jsonItem;
                if (objItem.has("name") && objItem.has("cmd")) {
                    items.add(new AA<>(objItem.getString("name"), objItem.getString("cmd")));
                } else {
                    Log.err("POPUP-MENU-ERROR --- " + jsonItem + " does not have 'name' or 'cmd' key !!!");
                }

            } else {
                Log.err("POPUP-MENU-ERROR --- " + jsonItem + " is not supported form of popup menu description !!!");
            }
        }
    }

    public void closePopupIfVisible() {
        if (popupFrame != null) {
            popupFrame.dispose();
        }
    }

    public void togglePopup(Frame frame, int mouseX , int mouseY) {

        if (popupFrame != null) {
            popupFrame.dispose();
        }

        popupFrame = new JFrame("popup");
        popupFrame.setLocation(0, 0);
        popupFrame.setVisible(true);

        JPopupMenu popup = new JPopupMenu();

        for (AA<String> item : items) {
            addItemToPopup(popup, item._1(), item._2());
        }

        Point p = frame.getJPanel().getLocationOnScreen();
        Int2D rightClickLocation = new Int2D(p.x+mouseX, p.y+mouseY);
        popup.show(popupFrame, rightClickLocation.getX(), rightClickLocation.getY());
    }

    private void addItemToPopup(JPopupMenu popup, String itemName, String cmdName) {
        JMenuItem menuItem = new JMenuItem(itemName);
        menuItem.addActionListener(this);
        menuItem.setActionCommand(cmdName);
        popup.add(menuItem);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<String> cmdOutput = cmdMaster.cmd(e.getActionCommand());
        Log.list(cmdOutput);

        if (popupFrame != null) {
            popupFrame.dispose();
        }
    }


    /* TODO další itemy
        else if( "console".equals(cmd) ){
            openConsole();
        }
        else if( "show Kisp".equals(cmd) ){
            openEditor( selected.toKisp() );
        }
        else if( "add in and out".equals(cmd) ){
            ((Function)selected).addInAndOut();
        }
        else if( "glue bricks".equals(cmd) ){
            selected.getBasic().glueBricks();
        }
    */

    /* TODO function and brick specific stuff ...
        if( selected instanceof Function ){
            addItemToPopup("add in and out", popup);
        }

        if( selected.getBasic().getIsBrick()  ){
            boolean hasBrick = false;
            for( KObject o : selected.parent().inside() ){
                if( o.getBasic().getIsBrick() ){
                    hasBrick = true;
                    break;
                }
            }
            if(hasBrick) { addItemToPopup("glue bricks", popup); }
        }
    */


}
