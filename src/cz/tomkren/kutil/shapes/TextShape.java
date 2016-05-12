package cz.tomkren.kutil.shapes;

import cz.tomkren.kutil.items.Int2D;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/** Created by user on 28. 11. 2015.*/

public class TextShape extends RectangleShape {

    private String txt;
    private int fontSize;


    public TextShape(String txt, int fontSize) {
        super(computeSize(txt, fontSize), Color.white);
        this.txt = txt;
        this.fontSize = fontSize;
    }

    @Override
    public void draw(Graphics2D g, boolean isSelected, String info, Int2D pos, Int2D center, double rot, boolean isRotable, double zoom, Int2D zoomCenter) {
        super.draw(g, isSelected, info, pos, center, rot, isRotable, zoom, zoomCenter);

        Font zoomFont = new Font(Font.MONOSPACED, Font.PLAIN, (int)(fontSize*zoom) );

        int dx = center.getX() + pos.getX();
        int dy = center.getY() + pos.getY();

        g.setFont(zoomFont);
        g.setColor(Color.black);
        g.drawString(txt, xZoomIt(dx), yZoomIt(dy+fontSize));
    }


    private static Int2D computeSize(String txt, int fontSize) {
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        int txtWidth = (int) ( font.getStringBounds(txt, new FontRenderContext(new AffineTransform(),false,false)).getWidth() );
        return new Int2D(txtWidth,fontSize);
    }



}
