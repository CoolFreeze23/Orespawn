package danger.orespawn.g1;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The static reader of every pose-interface getter a hook reads (the generalisation of the 2026-09-14 landing's {@code
 * declaredAttacking}): the {@code inputs.subject(<X>Pose.class)} casts of the descriptor's source and of the static pose
 * helpers it delegates to ({@code <Other>GeoReplacement.poseRig} / {@code poseDragon} / {@code pose}, followed as
 * before), the interfaces' public getters by reflection, and - for each getter - THE VALUES THE CODE BRANCHES ON, read
 * from the sources with comments stripped:
 * <ul>
 *   <li>an {@code int} getter compared with an integer literal ({@code g() == k}, {@code != k}, {@code > k}, {@code >= k},
 *       {@code < k}, {@code <= k}; the literal on either side; a {@code switch (g())}; or the same on a local the getter
 *       initialises - {@code int a = e.g(); if (a == 3)}) yields the literal set plus the value that takes the other side
 *       ({@code != 0} -> 1, {@code > 0} -> 1, {@code > 1} -> 1 and 2, {@code == 2} -> 2); the rest value 0 is the idle
 *       state, never a clip of its own, and is left out of the set;</li>
 *   <li>a {@code boolean} getter yields {@code true} (its rest is {@code false});</li>
 *   <li>a getter used only arithmetically or as an argument (a head extension in degrees, a scale, a yaw, a health
 *       ratio), a {@code RenderInfo} latch, an RNG or a movement delta is NOT enumerable: it is named with the reason.</li>
 * </ul>
 * A getter is read through a subject: a local or parameter declared with the pose interface's type, or the direct
 * {@code inputs.subject(<X>Pose.class).g()} form; a same-named method on any other receiver is not a pose read. A cast to
 * a type outside {@code danger.orespawn.entity.pose} is reported as such (none today).
 */
final class HookGetterReader {
    static final String POSE_PACKAGE = "danger.orespawn.entity.pose.";
    static final String CLIENT_SOURCE_DIR = "src/main/java/danger/orespawn/entity/client";
    /** The hook's declared pose interface: {@code inputs.subject(<X>.class)} in the descriptor or a helper it delegates to. */
    static final Pattern SUBJECT_CAST = Pattern.compile("subject\\(\\s*([A-Za-z0-9_.]+)\\.class\\s*\\)");
    /** A hook delegating to another descriptor's static pose helper ({@code <Other>GeoReplacement.poseRig(...)}, for one). */
    static final Pattern DELEGATION = Pattern.compile("\\b([A-Z][A-Za-z0-9]*GeoReplacement)\\.(pose[A-Za-z0-9]*)\\s*\\(");
    /** The getter names that read an attacking state on a pose interface (item 31 (11): getAttacking / isAttacking and kin). */
    static final Pattern ATTACKING_GETTER = Pattern.compile("(?i)^(get|is)?(is)?attack(ing)?$");
    private static final Pattern COMPARISON_AFTER = Pattern.compile("^\\s*(==|!=|>=|<=|>|<)\\s*(-?\\d+)\\b");
    private static final Pattern COMPARISON_BEFORE = Pattern.compile("(-?\\d+)\\s*(==|!=|>=|<=|>|<)\\s*$");
    private static final Pattern ALIAS_BEFORE = Pattern.compile("\\b(?:final\\s+)?(int|long|short|byte|boolean)\\s+(\\w+)\\s*=\\s*$");
    private static final Pattern SWITCH_AFTER = Pattern.compile("^\\s*\\)\\s*\\{");
    private static final Pattern CASE = Pattern.compile("\\bcase\\s+(-?\\d+)\\s*(?:->|:)");
    /** The rest value of every int getter on the probe (0) and the highest "other side" value the reader will try. */
    static final int REST = 0;
    private static final int OTHER_SIDE_LIMIT = 16;

    private HookGetterReader() {
    }

    /** One comparison the code makes: the getter (or its alias) against an integer literal, with the line it was read from. */
    record Comparison(String operator, int literal, String where) {
        boolean holds(int value) {
            return switch (this.operator) {
                case "==" -> value == this.literal;
                case "!=" -> value != this.literal;
                case ">" -> value > this.literal;
                case ">=" -> value >= this.literal;
                case "<" -> value < this.literal;
                case "<=" -> value <= this.literal;
                default -> throw new IllegalStateException("operator " + this.operator);
            };
        }
    }

    /**
     * One getter the hook reads: its interface, name and return type; the values enumerated (ints, or {@code true}) or
     * the reason it is not enumerable; the comparisons (or uses) it was read from with their source lines.
     */
    record GetterRead(String iface, String getter, String type, boolean enumerable, List<JsonPrimitive> values, String reason,
                      List<String> evidence) {
        String qualified() {
            return this.iface + "." + this.getter + "()";
        }

        boolean isAttacking() {
            return ATTACKING_GETTER.matcher(this.getter).matches();
        }

        JsonObject json() {
            JsonObject out = new JsonObject();
            out.addProperty("interface", this.iface);
            out.addProperty("getter", this.getter);
            out.addProperty("type", this.type);
            out.addProperty("enumerable", this.enumerable);
            JsonArray valueArray = new JsonArray();
            this.values.forEach(valueArray::add);
            out.add("values", valueArray);
            if (!this.enumerable) {
                out.addProperty("not_enumerable", this.reason);
            }
            JsonArray lines = new JsonArray();
            this.evidence.forEach(lines::add);
            out.add("read_at", lines);
            return out;
        }
    }

    /** What the hook declares: the pose interfaces cast (in order), every getter read, and any cast outside the pose package. */
    record Declared(String descriptor, List<String> interfaces, List<GetterRead> getters, List<String> outside) {
        static final Declared NONE = new Declared(null, List.of(), List.of(), List.of());

        List<GetterRead> enumerable() {
            return this.getters.stream().filter(GetterRead::enumerable).toList();
        }

        List<String> attackingGetters() {
            return this.getters.stream().filter(GetterRead::isAttacking).map(GetterRead::qualified).toList();
        }

        boolean readsAttacking() {
            return !attackingGetters().isEmpty();
        }

        JsonObject json() {
            JsonObject out = new JsonObject();
            out.addProperty("descriptor", this.descriptor);
            JsonArray ifaces = new JsonArray();
            this.interfaces.forEach(ifaces::add);
            out.add("declared_pose_interfaces", ifaces);
            JsonArray reads = new JsonArray();
            this.getters.forEach(read -> reads.add(read.json()));
            out.add("getters", reads);
            JsonArray outsideArray = new JsonArray();
            this.outside.forEach(outsideArray::add);
            out.add("outside_pose_interface", outsideArray);
            return out;
        }
    }

    /** The descriptor's source and the sources of the descriptors whose static pose helpers it calls, comments stripped. */
    static LinkedHashMap<String, String> sources(String descriptor, Path repositoryRoot) throws IOException {
        LinkedHashMap<String, String> texts = new LinkedHashMap<>();
        collect(descriptor, repositoryRoot, texts, 0);
        return texts;
    }

    private static void collect(String descriptor, Path repositoryRoot, LinkedHashMap<String, String> texts, int depth) throws IOException {
        if (texts.containsKey(descriptor) || depth > 3) {
            return;
        }
        Path source = repositoryRoot.resolve(CLIENT_SOURCE_DIR).resolve(descriptor + ".java");
        if (!Files.isRegularFile(source)) {
            throw new IllegalStateException(descriptor + ": no source at " + source + " to read the declared pose interfaces from");
        }
        String text = stripComments(Files.readString(source, StandardCharsets.UTF_8));
        texts.put(descriptor, text);
        Matcher delegation = DELEGATION.matcher(text);
        while (delegation.find()) {
            String other = delegation.group(1);
            if (!other.equals(descriptor)) {
                collect(other, repositoryRoot, texts, depth + 1);
            }
        }
    }

    /** Comments blanked character for character, so every offset still maps to its line. */
    static String stripComments(String text) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            if (text.startsWith("/*", i)) {
                int end = text.indexOf("*/", i + 2);
                end = end < 0 ? text.length() : end + 2;
                for (int j = i; j < end; j++) {
                    out.append(text.charAt(j) == '\n' ? '\n' : ' ');
                }
                i = end;
            } else if (text.startsWith("//", i)) {
                int end = text.indexOf('\n', i);
                end = end < 0 ? text.length() : end;
                for (int j = i; j < end; j++) {
                    out.append(' ');
                }
                i = end;
            } else if (text.charAt(i) == '"') {
                // a string literal: kept as blanks of the same length (a getter name inside a string is not a read)
                int j = i + 1;
                while (j < text.length() && text.charAt(j) != '"') {
                    j += text.charAt(j) == '\\' ? 2 : 1;
                }
                j = Math.min(j + 1, text.length());
                out.append('"');
                for (int k = i + 1; k < j - 1; k++) {
                    out.append(text.charAt(k) == '\n' ? '\n' : ' ');
                }
                if (j - 1 > i) {
                    out.append('"');
                }
                i = j;
            } else {
                out.append(text.charAt(i));
                i++;
            }
        }
        return out.toString();
    }

    static Declared read(String descriptor, Path repositoryRoot) throws IOException {
        LinkedHashMap<String, String> texts = sources(descriptor, repositoryRoot);
        LinkedHashSet<String> casts = new LinkedHashSet<>();
        for (String text : texts.values()) {
            Matcher cast = SUBJECT_CAST.matcher(text);
            while (cast.find()) {
                casts.add(cast.group(1));
            }
        }
        List<String> interfaces = new ArrayList<>();
        List<String> outside = new ArrayList<>();
        Map<String, Class<?>> types = new LinkedHashMap<>();
        for (String cast : casts) {
            String name = cast.substring(cast.lastIndexOf('.') + 1);
            Class<?> type;
            try {
                type = Class.forName(POSE_PACKAGE + name, false, HookGetterReader.class.getClassLoader());
            } catch (ClassNotFoundException notAPoseInterface) {
                type = null;
            }
            if (type == null || !type.isInterface()) {
                outside.add(cast);
                continue;
            }
            if (!types.containsKey(name)) {
                interfaces.add(name);
                types.put(name, type);
            }
        }
        List<GetterRead> reads = new ArrayList<>();
        for (Map.Entry<String, Class<?>> iface : types.entrySet()) {
            TreeMap<String, java.lang.reflect.Method> getters = new TreeMap<>();
            for (java.lang.reflect.Method method : iface.getValue().getMethods()) {
                if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                    getters.put(method.getName(), method);
                }
            }
            for (Map.Entry<String, java.lang.reflect.Method> getter : getters.entrySet()) {
                GetterRead read = readGetter(iface.getKey(), getter.getKey(), getter.getValue().getReturnType(), texts);
                if (read != null) {
                    reads.add(read);
                }
            }
        }
        return new Declared(descriptor, interfaces, reads, outside);
    }

    /** One getter over every source: null when the code never calls it through a subject. */
    private static GetterRead readGetter(String iface, String getter, Class<?> type, LinkedHashMap<String, String> texts) {
        List<String> evidence = new ArrayList<>();
        List<Comparison> comparisons = new ArrayList<>();
        List<String> otherUses = new ArrayList<>();
        Pattern occurrence = Pattern.compile("(?:\\b(\\w+)|subject\\(\\s*[A-Za-z0-9_.]+\\.class\\s*\\))\\s*\\.\\s*" + getter + "\\s*\\(\\s*\\)");
        for (Map.Entry<String, String> source : texts.entrySet()) {
            String text = source.getValue();
            TreeSet<String> subjects = subjectNames(text, iface);
            Matcher call = occurrence.matcher(text);
            while (call.find()) {
                String receiver = call.group(1);
                if (receiver != null && !subjects.contains(receiver)) {
                    continue;  // the same method name on another receiver: not a pose read
                }
                String where = source.getKey() + ":" + lineOf(text, call.start());
                evidence.add(where);
                String after = text.substring(call.end(), Math.min(text.length(), call.end() + 64));
                String before = text.substring(Math.max(0, call.start() - 64), call.start());
                Matcher comparison = COMPARISON_AFTER.matcher(after);
                if (comparison.find()) {
                    comparisons.add(new Comparison(comparison.group(1), Integer.parseInt(comparison.group(2)), where));
                    continue;
                }
                comparison = COMPARISON_BEFORE.matcher(before);
                if (comparison.find()) {
                    comparisons.add(new Comparison(mirror(comparison.group(2)), Integer.parseInt(comparison.group(1)), where));
                    continue;
                }
                int braceAt = before.lastIndexOf("switch");
                if (braceAt >= 0 && before.substring(braceAt).matches("switch\\s*\\(\\s*$") && SWITCH_AFTER.matcher(after).find()) {
                    comparisons.addAll(cases(text, call.end(), where));
                    continue;
                }
                Matcher alias = ALIAS_BEFORE.matcher(before);
                if (alias.find()) {
                    String local = alias.group(2);
                    List<Comparison> onAlias = aliasComparisons(text, local, source.getKey());
                    if (!onAlias.isEmpty()) {
                        comparisons.addAll(onAlias);
                        continue;
                    }
                }
                otherUses.add(where + ": " + lineText(text, call.start()));
            }
        }
        if (evidence.isEmpty()) {
            return null;
        }
        String typeName = type.getSimpleName();
        if (type == boolean.class) {
            return new GetterRead(iface, getter, typeName, true, List.of(new JsonPrimitive(true)),
                    "", evidence);
        }
        if (type == int.class || type == long.class || type == short.class || type == byte.class) {
            if (comparisons.isEmpty()) {
                return new GetterRead(iface, getter, typeName, false, List.of(),
                        "an int read used only arithmetically or passed as an argument, never compared with a literal (" + String.join("; ", otherUses) + ")",
                        evidence);
            }
            List<JsonPrimitive> values = new ArrayList<>();
            for (int value : enumerate(comparisons)) {
                values.add(new JsonPrimitive(value));
            }
            List<String> lines = new ArrayList<>();
            for (Comparison comparison : comparisons) {
                lines.add(comparison.where() + " " + comparison.operator() + " " + comparison.literal());
            }
            return new GetterRead(iface, getter, typeName, true, values, "", lines);
        }
        String reason;
        if (typeName.equals("RenderInfo")) {
            reason = "a RenderInfo latch (the per-entity scratch the pose reads and writes), not a value the code branches on";
        } else if (typeName.equals("RandomSource")) {
            reason = "the entity RNG (the seeded source the pose rolls), not a value the code branches on";
        } else if (typeName.equals("Vec3")) {
            reason = "a movement delta (the entity's velocity), not an enumerable state";
        } else if (getter.equals("getX") || getter.equals("getZ") || getter.equals("xOld") || getter.equals("zOld")) {
            reason = "a position read for the movement delta (lspeed = |xOld - x, zOld - z|; 0 on a probe that does not move), not an enumerable state";
        } else if (getter.toLowerCase(Locale.ROOT).contains("yrot")) {
            reason = "a yaw (the turn rate the flying head follows), not an enumerable state";
        } else if (getter.toLowerCase(Locale.ROOT).contains("health")) {
            reason = "a health read used arithmetically (the health ratio hf = health / max health, 1 on the probe), not an enumerable state";
        } else if (getter.toLowerCase(Locale.ROOT).contains("scale")) {
            reason = "a scale used arithmetically (every rhythm and amplitude divided or multiplied by it), not an enumerable state";
        } else {
            reason = "a " + typeName + " read used arithmetically, not an enumerable state";
        }
        return new GetterRead(iface, getter, typeName, false, List.of(), reason, evidence);
    }

    /** The names declared with the pose interface's type in a source: locals ({@code XPose entity = ...}) and parameters ({@code XPose entity,}). */
    private static TreeSet<String> subjectNames(String text, String iface) {
        TreeSet<String> names = new TreeSet<>();
        Matcher declaration = Pattern.compile("(?<![\\w.])" + iface + "\\s+(\\w+)\\s*(?=[=,)])").matcher(text);
        while (declaration.find()) {
            names.add(declaration.group(1));
        }
        return names;
    }

    /** Comparisons of a local the getter initialised ({@code int a = e.g();}: {@code a == 3}, {@code 3 == a}, {@code switch (a)}). */
    private static List<Comparison> aliasComparisons(String text, String local, String sourceName) {
        List<Comparison> out = new ArrayList<>();
        Matcher after = Pattern.compile("\\b" + local + "\\s*(==|!=|>=|<=|>|<)\\s*(-?\\d+)\\b").matcher(text);
        while (after.find()) {
            out.add(new Comparison(after.group(1), Integer.parseInt(after.group(2)), sourceName + ":" + lineOf(text, after.start()) + " (" + local + ")"));
        }
        Matcher before = Pattern.compile("(-?\\d+)\\s*(==|!=|>=|<=|>|<)\\s*" + local + "\\b").matcher(text);
        while (before.find()) {
            out.add(new Comparison(mirror(before.group(2)), Integer.parseInt(before.group(1)), sourceName + ":" + lineOf(text, before.start()) + " (" + local + ")"));
        }
        Matcher aSwitch = Pattern.compile("\\bswitch\\s*\\(\\s*" + local + "\\s*\\)\\s*\\{").matcher(text);
        while (aSwitch.find()) {
            out.addAll(cases(text, aSwitch.end() - 1, sourceName + ":" + lineOf(text, aSwitch.start()) + " (switch " + local + ")"));
        }
        return out;
    }

    /** The {@code case <int>} labels of the switch block opening at or after {@code from}. */
    private static List<Comparison> cases(String text, int from, String where) {
        int open = text.indexOf('{', from);
        if (open < 0) {
            return List.of();
        }
        int depth = 0;
        int close = open;
        for (; close < text.length(); close++) {
            char c = text.charAt(close);
            if (c == '{') {
                depth++;
            } else if (c == '}' && --depth == 0) {
                break;
            }
        }
        List<Comparison> out = new ArrayList<>();
        Matcher label = CASE.matcher(text.substring(open, Math.min(close + 1, text.length())));
        while (label.find()) {
            out.add(new Comparison("==", Integer.parseInt(label.group(1)), where));
        }
        return out;
    }

    private static String mirror(String operator) {
        return switch (operator) {
            case ">" -> "<";
            case "<" -> ">";
            case ">=" -> "<=";
            case "<=" -> ">=";
            default -> operator;
        };
    }

    /**
     * The values the code branches on (the rule of item 32 (2)): for each comparison, the literal (unless it is the rest
     * value) and, where neither the literal nor the rest value lands on one side of it, the smallest non-negative value
     * that does; the rest value itself is never in the set.
     */
    static TreeSet<Integer> enumerate(List<Comparison> comparisons) {
        TreeSet<Integer> values = new TreeSet<>();
        for (Comparison comparison : comparisons) {
            TreeSet<Integer> set = new TreeSet<>();
            if (comparison.literal() != REST) {
                set.add(comparison.literal());
            }
            for (boolean side : new boolean[]{true, false}) {
                boolean covered = comparison.holds(REST) == side || set.stream().anyMatch(v -> comparison.holds(v) == side);
                if (covered) {
                    continue;
                }
                for (int v = 1; v <= OTHER_SIDE_LIMIT; v++) {
                    if (v != REST && comparison.holds(v) == side) {
                        set.add(v);
                        break;
                    }
                }
            }
            values.addAll(set);
        }
        values.remove(REST);
        return values;
    }

    static int lineOf(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private static String lineText(String text, int offset) {
        int start = text.lastIndexOf('\n', offset) + 1;
        int end = text.indexOf('\n', offset);
        return text.substring(start, end < 0 ? text.length() : end).trim();
    }

    /** {@code getActivity} -> {@code activity}, {@code isInSittingPose} -> {@code in_sitting_pose}: the unnamed state's word. */
    static String bareName(String getter) {
        String name = getter;
        if (name.startsWith("get") && name.length() > 3) {
            name = name.substring(3);
        } else if (name.startsWith("is") && name.length() > 2) {
            name = name.substring(2);
        }
        StringBuilder snake = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && (Character.isLowerCase(name.charAt(i - 1)) || (i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1))))) {
                    snake.append('_');
                }
                snake.append(Character.toLowerCase(c));
            } else {
                snake.append(c);
            }
        }
        return snake.toString().replaceAll("^_+", "");
    }

    /** {@code true} as {@code 1}, an int as itself: the unnamed state's value token. */
    static String valueToken(JsonPrimitive value) {
        if (value.isBoolean()) {
            return value.getAsBoolean() ? "1" : "0";
        }
        return Integer.toString(value.getAsInt());
    }
}
