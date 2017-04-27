package com.bradcypert.textico.models;

abstract class ContactTrait {
    protected String name;
    protected String picUri;
    protected boolean isStarred;
}

public class Contact extends ContactTrait {

    public static class ContactBuilder extends ContactTrait {
        public ContactBuilder(){}

        public ContactBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ContactBuilder setPicUri(String picUri) {
            this.picUri = picUri;
            return this;
        }

        public ContactBuilder setStarred(boolean starred) {
            this.isStarred = starred;
            return this;
        }

        public Contact build() {
            return new Contact(name, picUri, isStarred);
        }
    }


    private Contact(String name, String picUri, boolean isStarred) {
        this.name = name;
        this.picUri = picUri;
        this.isStarred = isStarred;
    }

    public String getName() {
        return this.name;
    }

    public String getPicUri() {
        return this.picUri;
    }
}
