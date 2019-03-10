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
package net.bis5.mattermost.pluginupdater;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import net.bis5.mattermost.client4.ApiResponse;
import net.bis5.mattermost.client4.MattermostClient;
import net.bis5.mattermost.client4.hook.IncomingWebhookClient;
import net.bis5.mattermost.model.IncomingWebhookRequest;
import net.bis5.mattermost.model.PluginManifest;
import net.bis5.mattermost.pluginupdater.ExitCodeException.ExitReason;
import net.bis5.mattermost.pluginupdater.model.Plugin;

@Component
public class PluginUpdateRunner implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(PluginUpdateRunner.class.getName());

    @Override
    public void run(String... args) throws ConfigurationException, IOException {
        String pluginConfigFile = "plugin.yml";
        if (args.length >= 1) {
            pluginConfigFile = args[0];
        }
        Path pluginConfigPath = Paths.get(pluginConfigFile);
        Configuration pluginConfig = new FileBasedConfigurationBuilder<>(YAMLConfiguration.class) //
                .configure(new Parameters().hierarchical().setFile(pluginConfigPath.toFile())) //
                .getConfiguration();
        Plugin plugin = Plugin.of(pluginConfig);
        log.info("Plugin:"  + plugin.toString());

        // fetch archive
        log.info("begin #fetchArchive");
        Path archiveFile = fetchArchive(plugin);
        log.info("end #fetchArchive");

        // upload plugin force
        log.info("begin #uploadPlugin");
        uploadPlugin(archiveFile);
        log.info("end #uploadPlugin");

        // create post template
        IncomingWebhookRequest updateNotifyPayload = createNotifyPayload(plugin);

        // notify
        log.info("post notify");
        plugin.getPostWebhooks().stream() //
                .map(IncomingWebhookClient::new) //
                .map(c -> c.postByIncomingWebhook(updateNotifyPayload)) //
                .filter(ApiResponse::hasError) //
                .forEach(r -> log.severe(r.readError().toString()));
    }

    private IncomingWebhookRequest createNotifyPayload(Plugin plugin) {
        String postTemplate = plugin.getUpdatePostTemplate();
        String postBody = postTemplate.replace("${name}", plugin.getName()) //
                .replace("${version}", plugin.getVersion()) //
                .replace("${archive_url}", plugin.getArchiveUrl()) //
                .replace("${release_note_url}", plugin.getReleaseNoteUrl());
        IncomingWebhookRequest postPayload = new IncomingWebhookRequest();
        postPayload.setText(postBody);
        if (plugin.hasPostIcon()) {
            postPayload.setIconUrl(plugin.getPostIcon());
        }
        if (plugin.hasPostUsername()) {
            postPayload.setUsername(plugin.getPostUsername());
        }
        return postPayload;
    }

    private void uploadPlugin(Path archiveFile) {
        CombinedConfiguration config = new CombinedConfiguration();
        config.addConfiguration(new EnvironmentConfiguration());
        config.addConfiguration(new SystemConfiguration());
        String mattermostUrl = config.getString("mattermostUrl");
        if (mattermostUrl == null) {
            mattermostUrl = config.getString("MATTERMOST_URL");
            if (mattermostUrl == null) {
                throw new ExitCodeException(ExitReason.INVALID_SETTING, "mattermostUrl");
            }
        }
        String adminAccessToken = config.getString("accessToken");
        String username = null;
        String password = null;
        if (adminAccessToken == null) {
            adminAccessToken = config.getString("accessToken");
            if (adminAccessToken == null) {
                username = config.getString("username");
                password = config.getString("password");
                if (username == null) {
                    username = config.getString("USERNAME");
                    password = config.getString("PASSWORD");
                    if (username == null) {
                        throw new ExitCodeException(ExitReason.INVALID_SETTING,
                                "accessToken or username&password");
                    }
                }
            }
        }

        try (MattermostClient client = new MattermostClient(mattermostUrl)) {
            if (adminAccessToken != null) {
                client.setAccessToken(adminAccessToken);
            } else {
                client.login(username, password);
            }
            ApiResponse<PluginManifest> uploadResult = client.uploadPlugin(archiveFile, true);
            if (uploadResult.hasError()) {
                log.severe(uploadResult.readError().toString());
                throw new ExitCodeException(ExitReason.FAILURE_PLUGIN_UPLOAD);
            }
        }
    }

    private Path fetchArchive(Plugin plugin) throws IOException {
        Client jaxrsClient = ClientBuilder.newClient();
        WebTarget archiveTarget = jaxrsClient.target(plugin.getArchiveUrl());
        InputStream fileStream = archiveTarget.request().get(InputStream.class);
        Path archiveFile = Files.createTempFile(null, null);
        IOUtils.copy(fileStream, Files.newOutputStream(archiveFile, StandardOpenOption.TRUNCATE_EXISTING));
        archiveFile.toFile().deleteOnExit();
        return archiveFile;
    }

}