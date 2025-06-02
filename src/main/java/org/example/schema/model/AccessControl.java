package org.example.schema.model;

import java.util.List;

public class AccessControl {
    private List<String> read;
    private List<String> write;

    public List<String> getRead() {
        return read;
    }

    public void setRead(List<String> read) {
        this.read = read;
    }

    public List<String> getWrite() {
        return write;
    }

    public void setWrite(List<String> write) {
        this.write = write;
    }
}
