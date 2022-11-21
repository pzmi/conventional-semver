package dev.zmigrodzki.conventionalsemver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class Main {
    public static void main(String[] args) throws Exception {
        var builder = new FileRepositoryBuilder()
                .readEnvironment()
                .findGitDir();
        try (var repository = builder.build()) {
            var git = new Git(repository);
            var latestTag = git.describe().setAbbrev(0).setTags(true).setTarget("master").call();
            var walk = new RevWalk(repository);
            var from = repository.resolve("refs/heads/master");
            var to = repository.resolve(Constants.R_TAGS + latestTag);

            walk.markStart(walk.parseCommit(from));
            walk.markUninteresting(walk.parseCommit(to));

            for (RevCommit revCommit : walk) {
                var shortMessage = revCommit.getShortMessage();
                var split = shortMessage.split(":", 2);
                if (split.length > 1) {
                    var bump = switch (split[0]) {
                        case "feat!" -> "major";
                        case "feat" -> "minor";
                        default -> "bugfix";
                    };
                    System.out.println(split[0]);
                    System.out.println(bump);
                }

                revCommit.disposeBody();
            }
        }

    }
}