package com.f2prateek.dart.internal;

import com.f2prateek.dart.Optional;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class DefaultValue {

    public static final DefaultValue REQUIRED_NO_DEFAULT_ALLOWED = new DefaultValue();
    private final boolean required;
    private final Object value;

    private DefaultValue() {
        this.required = true;
        this.value = null;
    }

    private DefaultValue(String s) {
        this.required = false;
        this.value = s;
    }

    private DefaultValue(int i) {
        this.required = false;
        this.value = i;
    }

    private DefaultValue(long l) {
        this.required = false;
        this.value = l;
    }

    private DefaultValue(boolean b) {
        this.required = false;
        this.value = b;
    }

    public DefaultValue(String[] strings) {
        this.required = false;
        this.value = strings;
    }

    public DefaultValue(Object o) {
        this.required = false;
        this.value = o;
    }

    public static DefaultValue from(TypeMirror type, Optional optional) {
        switch (type.getKind()) {
            case LONG: return new DefaultValue(optional.defaultLong());
            case BOOLEAN: return new DefaultValue(optional.defaultBool());
            case ARRAY: return new DefaultValue(optional.defaultStringArray());
            case INT:
            case DOUBLE:
            case FLOAT:
            case CHAR:
                       return new DefaultValue(optional.defaultInt());
        }
        String s = optional.defaultString();
        if (Optional.NULL.equals(s)) {
            return new DefaultValue((String)null);
        }
        return new DefaultValue("\"" + optional.defaultString() + "\"");
    }

    public boolean isRequired() {
        return required;
    }

    public Object getValue() {
        return value;
    }
}
