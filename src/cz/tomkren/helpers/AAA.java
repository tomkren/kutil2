package cz.tomkren.helpers;

import java.util.function.Function;

/** Created by tom on 2.8.2015.*/

// modifikovatelný Triplet stejného typu
public class AAA<A> {

    private A a1;
    private A a2;
    private A a3;

    public AAA(A a1, A a2, A a3) {
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }

    public A _1() {return a1;}
    public A _2() {return a2;}
    public A _3() {return a3;}

    public void set1(A a1) {this.a1 = a1;}
    public void set2(A a2) {this.a2 = a2;}
    public void set3(A a3) {this.a3 = a3;}

    public void update(Function<A,A> f) {
        a1 = f.apply(a1);
        a2 = f.apply(a2);
        a3 = f.apply(a3);
    }

    @Override
    public String toString() {
        return "<" + a1 +","+ a2 +","+ a3 + ">";
    }

}
