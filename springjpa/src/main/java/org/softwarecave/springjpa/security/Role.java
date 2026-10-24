package org.softwarecave.springjpa.security;

public enum Role {
    ASSET_READ("SCOPE_asset.read"),
    ASSET_WRITE("SCOPE_asset.write"),
    ASSET_CLASS_READ("SCOPE_assetclass.read"),
    ASSET_CLASS_WRITE("SCOPE_assetclass.write"),
    ACTUATOR_READ("SCOPE_actuator.read"),
    ACTUATOR_WRITE("SCOPE_actuator.write");

    private final String title;

    Role(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
