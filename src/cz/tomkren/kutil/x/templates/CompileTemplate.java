package cz.tomkren.kutil.x.templates;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Created by tom on 18.8.2015. */

public class CompileTemplate<T> {

    private String templatePath;
    private BiFunction<T,T,T> actFun;
    private BiFunction<T,T,T> titleFun;
    private Function<T,T> listFun;
    private Function<T,T> itemFun;
    private Function<T,T> italicsFun;
    private Function<T,T> lineFun;
    private Function<T,T> paragraphFun;

    public CompileTemplate(
            String templatePath,
            BiFunction<T,T,T> actFun,
            BiFunction<T,T,T> titleFun,
            Function<T,T> listFun,
            Function<T,T> itemFun,
            Function<T,T> italicsFun,
            Function<T,T> lineFun,
            Function<T,T> paragraphFun
    ){
        this.templatePath = templatePath;
        this.actFun = actFun;
        this.titleFun = titleFun;
        this.listFun = listFun;
        this.itemFun = itemFun;
        this.italicsFun = italicsFun;
        this.lineFun = lineFun;
        this.paragraphFun = paragraphFun;
    }

    public String getPath() {return templatePath;}

    public T actTitle   (T i, T s) {return actFun.apply(i,s);}
    public T sceneTitle (T i, T s) {return titleFun.apply(i,s);}
    public T list       (T s) {return listFun.apply(s);}
    public T listItem   (T s) {return itemFun.apply(s);}
    public T italics    (T s) {return italicsFun.apply(s);}
    public T newLine    (T s) {return lineFun.apply(s);}
    public T paragraph  (T s) {return paragraphFun.apply(s);}


}
