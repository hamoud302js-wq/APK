package com.example.studyplanner.models;

import java.io.Serializable;

/**
 * A task category such as study, work or personal. Stored in the {@code categories} table.
 */
public class Category implements Serializable {

    /** Default category ids created on first launch. */
    public static final long DEFAULT_STUDY = 1L;
    public static final long DEFAULT_WORK = 2L;
    public static final long DEFAULT_PERSONAL = 3L;
    public static final long DEFAULT_HEALTH = 4L;
    public static final long DEFAULT_OTHER = 5L;

    private long id;
    private String name;
    private String color;
    private String icon;
    private int taskCount;

    public Category() {
    }

    public Category(String name, String color, String icon) {
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public Category(long id, String name, String color, String icon) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }
}
