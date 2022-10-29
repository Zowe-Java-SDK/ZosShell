package com.dto;

public class Output {

    private final String name;
    private final StringBuilder output;

    public Output(String name, StringBuilder output) {
        this.name = name;
        this.output = output;
    }

    public String getName() {
        return name;
    }

    public StringBuilder getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "Output{" +
                "name='" + name + '\'' + '}';
    }

}
