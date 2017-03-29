package com.konkerlabs.platform.registry.business.model.enumerations;

import java.util.Locale;

public enum Language {
    PT_BR("languages.pt_BR", "pt", "BR"),
    EN("languages.en", "en", "US");

    private String code;
    private String language;
    private String region;

    Language(String key, String language, String region) {
        this.code = key;
        this.language = language;
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public String getRegion() {
        return region;
    }

    public String getLanguage(){
        return language;
    }

    /**
     * Return a Java Locale Object
     * @return java.util.Locale
     */
    public Locale getLocale(){
        return new Locale(getLanguage(), getRegion());
    }
}
