package dk.apaq.rest.patch;

import java.util.ArrayList;
import java.util.List;

public class DummyEntity {
    private String text;
    private int number;
    private String[] array;
    private List<String> list = new ArrayList<>();
    private DummyEntity child;

    public DummyEntity() { }

    public DummyEntity(String text, int number, String[] array, List<String> list) {
        this.text = text;
        this.number = number;
        this.array = array;
        this.list = list;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String[] getArray() {
        return array;
    }

    public void setArray(String[] array) {
        this.array = array;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public DummyEntity getChild() {
        return child;
    }

    public void setChild(DummyEntity child) {
        this.child = child;
    }
}
