package com.nastolka.dto;

public class GamePlayCountResponse {

    private Long gameId;
    private String gameName;
    private long playCount;

    public GamePlayCountResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public long getPlayCount() {
        return playCount;
    }

    public void setPlayCount(long playCount) {
        this.playCount = playCount;
    }

    public static class Builder {
        private Long gameId;
        private String gameName;
        private long playCount;

        public Builder gameId(Long gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder playCount(long playCount) {
            this.playCount = playCount;
            return this;
        }

        public GamePlayCountResponse build() {
            GamePlayCountResponse response = new GamePlayCountResponse();
            response.gameId = gameId;
            response.gameName = gameName;
            response.playCount = playCount;
            return response;
        }
    }
}
