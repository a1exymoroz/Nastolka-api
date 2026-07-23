package com.nastolka.dto;

public class ExpansionResponse {

    private Long id;
    private Long gameId;
    private Long bggId;
    private String name;
    private String description;
    private String photo;
    private String bggUrl;

    public ExpansionResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBggUrl() {
        return bggUrl;
    }

    public void setBggUrl(String bggUrl) {
        this.bggUrl = bggUrl;
    }

    public static class Builder {
        private Long id;
        private Long gameId;
        private Long bggId;
        private String name;
        private String description;
        private String photo;
        private String bggUrl;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder gameId(Long gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder bggId(Long bggId) {
            this.bggId = bggId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder photo(String photo) {
            this.photo = photo;
            return this;
        }

        public Builder bggUrl(String bggUrl) {
            this.bggUrl = bggUrl;
            return this;
        }

        public ExpansionResponse build() {
            ExpansionResponse response = new ExpansionResponse();
            response.id = id;
            response.gameId = gameId;
            response.bggId = bggId;
            response.name = name;
            response.description = description;
            response.photo = photo;
            response.bggUrl = bggUrl;
            return response;
        }
    }
}
