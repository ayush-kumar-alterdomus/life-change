package com.ascend.arc.entity;

/**
 * Status of a user's arc progress lifecycle.
 *
 * <p>Transitions:
 * <ul>
 *   <li>ACTIVE → PAUSED (user pauses voluntarily)</li>
 *   <li>ACTIVE → COMPLETED (all milestones finished)</li>
 *   <li>ACTIVE → ABANDONED (user quits)</li>
 *   <li>PAUSED → ACTIVE (user resumes)</li>
 * </ul>
 */
public enum ArcStatus {

    /** User is actively progressing through the arc. */
    ACTIVE("Active"),

    /** Arc finished — all milestones completed. */
    COMPLETED("Completed"),

    /** User paused voluntarily; can resume later. */
    PAUSED("Paused"),

    /** User quit the arc; cannot resume. */
    ABANDONED("Abandoned");

    private final String displayName;

    ArcStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
