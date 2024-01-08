package com.example.util;

final class ExchangeInfo {
    private String protocol;
    private String mainnetHost;
    private String testnetHost;
    private String version;

    public static class ExchangeInfoBuilder {
        private String protocol;
        private String mainnetHost;
        private String testnetHost;
        private String version;

        public ExchangeInfoBuilder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ExchangeInfoBuilder mainnetHost(String mainnetHost) {
            this.mainnetHost = mainnetHost;
            return this;
        }

        public ExchangeInfoBuilder testnetHost(String testnetHost) {
            this.testnetHost = testnetHost;
            return this;
        }

        public ExchangeInfoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public ExchangeInfo build() {
            return new ExchangeInfo(this.protocol, this.mainnetHost, this.testnetHost, this.version);
        }
    }

    ExchangeInfo(String protocol, String mainnetHost, String testnetHost, String version) {
        this.protocol = protocol;
        this.mainnetHost = mainnetHost;
        this.testnetHost = testnetHost;
        this.version = version;
    }

    public static ExchangeInfoBuilder newExchangeInfoBuilder() {
        return new ExchangeInfoBuilder();
    }

    public String protocol() {
        return this.protocol;
    }

    public String mainnetHost() {
        return this.mainnetHost;
    }

    public String testnetHost() {
        return this.testnetHost;
    }

    public String version() {
        return this.version;
    }
}