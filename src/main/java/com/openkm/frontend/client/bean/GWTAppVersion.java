package com.openkm.frontend.client.bean;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTAppVersion implements IsSerializable {
    private String major = "";

    private String minor = "";

    private String maintenance = "";

    private String build = "";

    private String extension = "";

    public String getMajor() {
        return major;
    }

    public void setMajor(final String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(final String minor) {
        this.minor = minor;
    }

    public String getMaintenance() {
        return maintenance;
    }

    public void setMaintenance(final String maintenance) {
        this.maintenance = maintenance;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(final String build) {
        this.build = build;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public String getVersion() {
        return major + "." + minor + "." + maintenance;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + maintenance + " (build: " + build
                + ")";
    }
}
