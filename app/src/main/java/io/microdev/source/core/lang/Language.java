package io.microdev.source.core.lang;

public class Language {

    private Type type;

    public Language() {
        type = null;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public class Type {

        private String name;

        public Type() {
            name = null;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
