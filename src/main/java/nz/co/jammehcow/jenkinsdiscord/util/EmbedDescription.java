package nz.co.jammehcow.jenkinsdiscord.util;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import jenkins.model.JenkinsLocationConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * @author jammehcow
 */

public class EmbedDescription {
    private static final int maxEmbedStringLength = 2048; // The maximum length of an embed description.

    private LinkedList<String> changesList = new LinkedList<>();
    private String prefix;
    private String finalDescription;

    public EmbedDescription(AbstractBuild build, JenkinsLocationConfiguration globalConfig, String prefix) {
        this.prefix = prefix;
        this.changesList.add("\n**Changes:**\n");
        Object[] changes = build.getChangeSet().getItems();
        if (changes.length == 0) {
            this.changesList.add("\n*No changes.*\n");
        } else {
            for (Object o : changes) {
                ChangeLogSet.Entry entry = (ChangeLogSet.Entry) o;
                String commitID = (entry.getParent().getKind().equalsIgnoreCase("svn")) ? entry.getCommitId() : entry.getCommitId().substring(0, 6);

                this.changesList.add("   - ``" + commitID + "`` *" + entry.getMsg() + " - " + entry.getAuthor().getFullName() + "*\n");
            }
        }

        boolean isTruncated = false;
        while (this.getCurrentDescription().length() > maxEmbedStringLength) {
            if (this.changesList.size() > 5) {
                // Dwindle the changes list down to 5 changes.
                while (this.changesList.size() != 5) this.changesList.removeLast();
            } else {
                // Worst case scenario: truncate the description.
                isTruncated = true;
                break;
            }
        }

        this.finalDescription = (isTruncated) ? this.getCurrentDescription().substring(0, maxEmbedStringLength - 1) : this.getCurrentDescription();
    }

    private String getCurrentDescription() {
        StringBuilder description = new StringBuilder();
        description.append(this.prefix);

        // Collate the changes and artifacts into the description.
        for (String changeEntry : this.changesList) description.append(changeEntry);
        return description.toString();
    }

    @Override
    public String toString() {
        return this.finalDescription;
    }
}
