package ch.frostnova.module1.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Example application properties
 */
@Component
@ConfigurationProperties("app.test")
public class ExampleProperties {

    private int a;
    private double b;
    private boolean c;
    private String d;
    private Set<String> e;
    private List<String> f;

    private int x = 5;
    private double y = 6.7;
    private String z = "Aloha";

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public boolean isC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public Set<String> getE() {
        return e;
    }

    public void setE(Set<String> e) {
        this.e = e;
    }

    public List<String> getF() {
        return f;
    }

    public void setF(List<String> f) {
        this.f = f;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getZ() {
        return z;
    }

    public void setZ(String z) {
        this.z = z;
    }
}
