package com.nihith.iam.constants;

/**
 * Display-labelled enum for CRUD operations performed against IAM resources.
 */
public enum AuthOperation {

    CREATE("Create"),
    MODIFY("Modify"),
    DELETE("Delete");

    private final String displayName;

    AuthOperation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
