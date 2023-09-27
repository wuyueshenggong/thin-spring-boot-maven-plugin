//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hsq;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.codehaus.plexus.util.cli.CommandLineUtils;

class RunArguments {
    private static final String[] NO_ARGS = new String[0];
    private final Deque<String> args;

    RunArguments(String arguments) {
        this(parseArgs(arguments));
    }

    RunArguments(String[] args) {
        this.args = new LinkedList();
        if (args != null) {
            Stream var10000 = Arrays.stream(args).filter(Objects::nonNull);
            Deque var10001 = this.args;
            var10000.forEach(var10001::add);
        }

    }

    public Deque<String> getArgs() {
        return this.args;
    }

    public String[] asArray() {
        return (String[])this.args.toArray(new String[0]);
    }

    private static String[] parseArgs(String arguments) {
        if (arguments != null && !arguments.trim().isEmpty()) {
            try {
                arguments = arguments.replace('\n', ' ').replace('\t', ' ');
                return CommandLineUtils.translateCommandline(arguments);
            } catch (Exception var2) {
                throw new IllegalArgumentException("Failed to parse arguments [" + arguments + "]", var2);
            }
        } else {
            return NO_ARGS;
        }
    }
}
