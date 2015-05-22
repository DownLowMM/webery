package com.wizzardo.http.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wizzardo on 21.05.15.
 */
public abstract class MessageSource {
    public abstract String get(String key, Object... args);

    public String getWithDefault(String key, String def, Object... args) {
        String s = get(key, args);
        return s == null ? def : s;
    }

    static class Template {

        static final Pattern ARGS_PATTERN = Pattern.compile("\\{([0-9]+)\\}");

        final String raw;
        Part[] parts;

        public Template(String message) {
            raw = message;
            init();
        }

        public String get(Object... args) {
            StringBuilder sb = new StringBuilder();
            appendTo(sb::append, args);
            return sb.toString();
        }

        public void appendTo(Consumer<String> consumer, Object... args) {
            for (Part part : parts) {
                consumer.accept(part.get(args));
            }
        }

        protected void init() {
            try {
                List<Part> l = new ArrayList<>();

                Matcher matcher = ARGS_PATTERN.matcher(raw);
                int from = 0;
                while (matcher.find()) {
                    if (matcher.start() != from) {
                        String value = raw.substring(from, matcher.start());
                        l.add(args -> value);
                    }

                    int number = Integer.parseInt(matcher.group(1));
                    l.add(args -> args.length <= number ? "null" : String.valueOf(args[number]));

                    from = matcher.end();
                }
                if (from != raw.length()) {
                    String value = raw.substring(from);
                    l.add(args -> value);
                }

                parts = l.toArray(new Part[l.size()]);
            } catch (Exception e) {
                throw new IllegalArgumentException("can't prepare message '" + raw + "'");
            }
        }

        interface Part {
            String get(Object[] args);
        }
    }
}
