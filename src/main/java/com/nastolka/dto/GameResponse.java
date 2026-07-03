package com.nastolka.dto;

public class GameResponse {

    private Long id;
    private String name;
    private String description;
    private String photo;

    public GameResponse() {
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

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private String photo;

        public Builder id(Long id) {
            this.id = id;
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

        public GameResponse build() {
            GameResponse response = new GameResponse();
            response.id = id;
            response.name = name;
            response.description = description;
            response.photo = photo;
            return response;
        }
    }
}
