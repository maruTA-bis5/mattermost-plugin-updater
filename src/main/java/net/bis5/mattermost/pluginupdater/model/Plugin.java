/*
 * Copyright 2019 Takayuki Maruyama
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bis5.mattermost.pluginupdater.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Plugin {
    public static Plugin of(Configuration config) {
        String name = config.getString("name");
        String version = config.getString("version");
        String archiveUrl = config.getString("archiveUrl");
        String releaseNoteUrl = config.getString("releaseNoteUrl");
        String updatePostTemplate = config.getString("updatePostTemplate");
        String[] postWebhooks = config.getStringArray("postWebhooks");
        String postUsername = config.getString("postUsername");
        String postIcon = config.getString("postIcon");
        return new Plugin(name, version, archiveUrl, releaseNoteUrl, updatePostTemplate, postWebhooks, postUsername,
                postIcon);
    }

    private Plugin(String name, String version, String archiveUrl, String releaseNoteUrl, String updatePostTemplate,
            String[] postWebhooks, String postUsername, String postIcon) {
        this.name = Objects.requireNonNull(name);
        this.version = Objects.requireNonNull(version);
        this.archiveUrl = Objects.requireNonNull(archiveUrl);
        this.releaseNoteUrl = releaseNoteUrl;
        this.updatePostTemplate = Objects.requireNonNull(updatePostTemplate);
        this.postWebhooks = Objects.requireNonNull(postWebhooks);
        this.postUsername = postUsername;
        this.postIcon = postIcon;
    }

    private String name;
    private String version;
    private String archiveUrl;
    private String releaseNoteUrl;
    private String updatePostTemplate;
    private String[] postWebhooks;
    private String postUsername;
    private String postIcon;

    public String getName() {
        return name;
    }

    public boolean hasPostIcon() {
        return postIcon != null;
    }

    public String getPostIcon() {
        return postIcon;
    }

    public boolean hasPostUsername() {
        return postUsername != null;
    }

    public String getPostUsername() {
        return postUsername;
    }

    public Collection<String> getPostWebhooks() {
        return Arrays.asList(postWebhooks);
    }

    public String getUpdatePostTemplate() {
        return updatePostTemplate;
    }

    public String getReleaseNoteUrl() {
        return releaseNoteUrl;
    }

    public String getArchiveUrl() {
        return archiveUrl;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}