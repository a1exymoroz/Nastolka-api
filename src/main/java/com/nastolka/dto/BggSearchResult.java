package com.nastolka.dto;

public class BggSearchResult {

    private Long bggId;
    private String name;
    private Integer yearPublished;
    private boolean expansion;

    public BggSearchResult() {
    }

    public BggSearchResult(Long bggId, String name, Integer yearPublished, boolean expansion) {
        this.bggId = bggId;
        this.name = name;
        this.yearPublished = yearPublished;
        this.expansion = expansion;
    }

    public Long getBggId() {
        return bggId;
    }

    public void setBggId(Long bggId) {
        this.bggId = bggId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(Integer yearPublished) {
        this.yearPublished = yearPublished;
    }

    public boolean isExpansion() {
        return expansion;
    }

    public void setExpansion(boolean expansion) {
        this.expansion = expansion;
    }
}
