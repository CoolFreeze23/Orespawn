package danger.orespawn.bench;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Phase G slice (d): the repository root, its HEAD and the working tree's state, read from the
 * {@code .git} directory without running git (the client runs from {@code run/}, one level below
 * the root). Every failure yields {@code "unknown"} -- the report records what it could read, never
 * guesses.
 *
 * <p>{@link #workingTree} (refuter B, 2026-09-06): {@code git_head} alone cannot tell a dirty tree.
 * The index ({@code .git/index}, versions 2 and 3) records every tracked file's stat at the time it
 * was staged or last refreshed; comparing the recorded mtime seconds and size with the file's own
 * is what {@code git status} does before it hashes anything. What this does NOT see: untracked
 * files, and changes already staged (the index then matches the tree but not HEAD) -- the runbook
 * asks the owner to commit before a run so that {@code git_head} names the measured code.</p>
 */
public final class BenchGit {

    public static final String UNKNOWN = "unknown";
    /** The working-tree field's value when nothing beyond HEAD could be read. */
    public static final String WORKING_TREE_HEAD_ONLY = "unknown — head only";

    private static final int INDEX_SIGNATURE = 0x44495243; // "DIRC"
    private static final int INDEX_ENTRY_FIXED_BYTES = 62;
    private static final int FLAG_ASSUME_VALID = 0x8000;
    private static final int FLAG_EXTENDED = 0x4000;
    private static final int EXTENDED_FLAG_SKIP_WORKTREE = 0x4000;
    private static final int MODE_TYPE_MASK = 0xF000;
    private static final int MODE_REGULAR_FILE = 0x8000;
    private static final int EXAMPLES = 5;

    private BenchGit() {
    }

    /** The nearest ancestor of the working directory that holds a {@code .git} entry, else the working directory. */
    public static Path repositoryRoot() {
        Path dir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (Path p = dir; p != null; p = p.getParent()) {
            if (Files.exists(p.resolve(".git"))) {
                return p;
            }
        }
        return dir;
    }

    /** The git directory of {@code root} ({@code .git}, or where a worktree's {@code .git} file points), or {@code null}. */
    private static Path gitDir(Path root) throws IOException {
        Path gitDir = root.resolve(".git");
        if (Files.isRegularFile(gitDir)) {
            // A worktree: ".git" is a file "gitdir: <path>".
            String pointer = Files.readString(gitDir, StandardCharsets.UTF_8).trim();
            if (!pointer.startsWith("gitdir:")) {
                return null;
            }
            gitDir = root.resolve(pointer.substring("gitdir:".length()).trim()).normalize();
        }
        return Files.isDirectory(gitDir) ? gitDir : null;
    }

    /** HEAD's commit hash (symbolic refs and packed refs resolved), or {@link #UNKNOWN}. */
    public static String head(Path root) {
        try {
            Path gitDir = gitDir(root);
            if (gitDir == null) {
                return UNKNOWN;
            }
            Path headFile = gitDir.resolve("HEAD");
            if (!Files.isRegularFile(headFile)) {
                return UNKNOWN;
            }
            String head = Files.readString(headFile, StandardCharsets.UTF_8).trim();
            if (!head.startsWith("ref:")) {
                return head.isEmpty() ? UNKNOWN : head;
            }
            String ref = head.substring("ref:".length()).trim();
            Path refFile = gitDir.resolve(ref);
            if (Files.isRegularFile(refFile)) {
                String hash = Files.readString(refFile, StandardCharsets.UTF_8).trim();
                return hash.isEmpty() ? UNKNOWN : hash;
            }
            // A worktree's common dir, or a packed ref.
            Path commonDir = gitDir;
            Path commonPointer = gitDir.resolve("commondir");
            if (Files.isRegularFile(commonPointer)) {
                commonDir = gitDir.resolve(Files.readString(commonPointer, StandardCharsets.UTF_8).trim()).normalize();
                Path commonRef = commonDir.resolve(ref);
                if (Files.isRegularFile(commonRef)) {
                    String hash = Files.readString(commonRef, StandardCharsets.UTF_8).trim();
                    return hash.isEmpty() ? UNKNOWN : hash;
                }
            }
            Path packed = commonDir.resolve("packed-refs");
            if (Files.isRegularFile(packed)) {
                List<String> lines = Files.readAllLines(packed, StandardCharsets.UTF_8);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (trimmed.endsWith(" " + ref)) {
                        return trimmed.substring(0, trimmed.indexOf(' '));
                    }
                }
            }
            return UNKNOWN;
        } catch (IOException | RuntimeException failed) {
            return UNKNOWN;
        }
    }

    /**
     * The working tree against the index, by stat: {@code "clean by index stat: ..."},
     * {@code "DIRTY by index stat: ..."} (with examples), or {@link #WORKING_TREE_HEAD_ONLY} plus
     * the reason when the index cannot be read (no repository, no index, index version 4 --
     * prefix-compressed names -- or a malformed file). Compares mtime seconds and size only (git's
     * own nanoseconds are not recorded on every platform), skips symlinks, gitlinks and entries
     * flagged assume-valid or skip-worktree. Never runs git.
     */
    public static String workingTree(Path root) {
        try {
            Path gitDir = gitDir(root);
            if (gitDir == null) {
                return WORKING_TREE_HEAD_ONLY + " (no .git directory)";
            }
            Path indexFile = gitDir.resolve("index");
            if (!Files.isRegularFile(indexFile)) {
                return WORKING_TREE_HEAD_ONLY + " (no index)";
            }
            byte[] bytes = Files.readAllBytes(indexFile);
            ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
            if (bytes.length < 12 || buf.getInt() != INDEX_SIGNATURE) {
                return WORKING_TREE_HEAD_ONLY + " (not an index)";
            }
            int version = buf.getInt();
            if (version != 2 && version != 3) {
                return WORKING_TREE_HEAD_ONLY + " (index version " + version + "; versions 2 and 3 are read)";
            }
            int entries = buf.getInt();
            int checked = 0;
            int differing = 0;
            int missing = 0;
            List<String> examples = new ArrayList<>();
            for (int i = 0; i < entries; i++) {
                int start = buf.position();
                if (bytes.length - start < INDEX_ENTRY_FIXED_BYTES + 1) {
                    return WORKING_TREE_HEAD_ONLY + " (truncated index)";
                }
                buf.getInt(); // ctime seconds
                buf.getInt(); // ctime nanoseconds
                long mtimeSeconds = buf.getInt() & 0xFFFFFFFFL;
                buf.getInt(); // mtime nanoseconds (not compared, see above)
                buf.getInt(); // dev
                buf.getInt(); // ino
                int mode = buf.getInt();
                buf.getInt(); // uid
                buf.getInt(); // gid
                int size = buf.getInt();
                buf.position(buf.position() + 20); // the object name
                int flags = buf.getShort() & 0xFFFF;
                boolean skipWorktree = false;
                if ((flags & FLAG_EXTENDED) != 0) {
                    int extended = buf.getShort() & 0xFFFF;
                    skipWorktree = (extended & EXTENDED_FLAG_SKIP_WORKTREE) != 0;
                }
                int nameStart = buf.position();
                int nameEnd = nameStart;
                while (nameEnd < bytes.length && bytes[nameEnd] != 0) {
                    nameEnd++;
                }
                if (nameEnd >= bytes.length) {
                    return WORKING_TREE_HEAD_ONLY + " (truncated index)";
                }
                String name = new String(bytes, nameStart, nameEnd - nameStart, StandardCharsets.UTF_8);
                // v2/v3: 1-8 NUL bytes pad the entry to a multiple of eight.
                int padded = ((nameEnd - start) + 8) & ~7;
                buf.position(start + padded);
                if ((mode & MODE_TYPE_MASK) != MODE_REGULAR_FILE || (flags & FLAG_ASSUME_VALID) != 0 || skipWorktree) {
                    continue;
                }
                checked++;
                Path file = root.resolve(name);
                if (!Files.isRegularFile(file)) {
                    missing++;
                    if (examples.size() < EXAMPLES) {
                        examples.add(name + " (missing)");
                    }
                    continue;
                }
                FileTime modified = Files.getLastModifiedTime(file);
                boolean same = modified.to(TimeUnit.SECONDS) == mtimeSeconds && (int) Files.size(file) == size;
                if (!same) {
                    differing++;
                    if (examples.size() < EXAMPLES) {
                        examples.add(name);
                    }
                }
            }
            if (differing == 0 && missing == 0) {
                return String.format(Locale.ROOT,
                        "clean by index stat: %d tracked files match the index (mtime seconds and size); untracked files and staged changes are not seen",
                        checked);
            }
            return String.format(Locale.ROOT,
                    "DIRTY by index stat: %d of %d tracked files differ from the index (mtime seconds or size), %d missing; e.g. %s",
                    differing, checked, missing, String.join(", ", examples));
        } catch (IOException | RuntimeException failed) {
            return WORKING_TREE_HEAD_ONLY + " (" + failed.getClass().getSimpleName() + ")";
        }
    }
}
