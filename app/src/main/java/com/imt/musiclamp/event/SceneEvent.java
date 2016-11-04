package com.imt.musiclamp.event;

import com.activeandroid.annotation.Column;

public class SceneEvent {
    private String name;
    private int wheelValue;
    private boolean selected;
    private String fragment;
    private boolean readOnly;
    private float degress;
    private float[] values = new float[9];

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWheelValue() {
        return wheelValue;
    }

    public void setWheelValue(int wheelValue) {
        this.wheelValue = wheelValue;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public float getDegress() {
        return degress;
    }

    public void setDegress(float degress) {
        this.degress = degress;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }

    public void setValues(float value, int i) {
        values[i] = value;
    }
}
