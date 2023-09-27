//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class EnvVariables {
    private final Map<String, String> variables;

    EnvVariables(Map<String, String> variables) {
        this.variables = parseEnvVariables(variables);
    }

    private static Map<String, String> parseEnvVariables(Map<String, String> args) {
        if (args != null && !args.isEmpty()) {
            Map<String, String> result = new LinkedHashMap();
            Iterator var2 = args.entrySet().iterator();

            while(var2.hasNext()) {
                Entry<String, String> e = (Entry)var2.next();
                if (e.getKey() != null) {
                    result.put(e.getKey(), getValue((String)e.getValue()));
                }
            }

            return result;
        } else {
            return Collections.emptyMap();
        }
    }

    private static String getValue(String value) {
        return value != null ? value : "";
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(this.variables);
    }

    public String[] asArray() {
        List<String> args = new ArrayList(this.variables.size());
        Iterator var2 = this.variables.entrySet().iterator();

        while(var2.hasNext()) {
            Entry<String, String> arg = (Entry)var2.next();
            args.add((String)arg.getKey() + "=" + (String)arg.getValue());
        }

        return (String[])args.toArray(new String[0]);
    }
}
