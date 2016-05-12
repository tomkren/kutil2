package cz.tomkren.kutil.items;


public class XmlText implements Xml {

    private String text;

    public XmlText(String text){
        this.text = text;
    }

    @Override
    public String toString() {
        return text + '\n';
    }

    @Override
    public String toPrettyJson(int ods) {
        return XmlElement.ods(ods) + text + "\n";
    }
}
