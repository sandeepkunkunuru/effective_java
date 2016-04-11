package me.tingri.inheritance.overriden_construction;

//ITEM 17: DESIGN AND DOCUMENT FOR INHERITANCE OR ELSE PROHIBIT IT

public class Super {
    // Broken - constructor invokes an overridable method
    public Super() {
        overrideMe();
    }
    public void overrideMe() {
    }
}
